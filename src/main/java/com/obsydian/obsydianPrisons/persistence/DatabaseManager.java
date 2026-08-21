package com.obsydian.obsydianprisons.persistence;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.player.PlayerData;
import com.obsydian.obsydianprisons.player.PlayerDataCache;
import com.obsydian.obsydianprisons.player.WarpCache;
import com.obsydian.obsydianprisons.player.models.Warp;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            Thread.ofPlatform()
                    .name("prisons-database-", 0)
                    .factory());
    private ConnectionSource source;
    private Dao<PlayerData, UUID> playerDao;
    private Dao<Warp, String> warpDao;
    private CompletableFuture<Void> ready;
    private final ObsydianPrisons plugin;

    public DatabaseManager(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    private void migratePlayerDataTable() throws Exception {
        Set<String> columns = new HashSet<>();

        try (GenericRawResults<String[]> rows =
                     playerDao.queryRaw("PRAGMA table_info(player_data)")) {
            for (String[] row : rows) {
                columns.add(row[1]);
            }
        }

        if (!columns.contains("multiplier")) {
            playerDao.executeRaw("""
                ALTER TABLE player_data
                ADD COLUMN multiplier REAL NOT NULL DEFAULT 1.0
                """);
        }

        if (!columns.contains("settings")) {
            playerDao.executeRaw("""
                ALTER TABLE player_data
                ADD COLUMN settings TEXT NOT NULL
                DEFAULT '{"autoSellEnabled":false}'
                """);
        }
    }
    public synchronized CompletableFuture<Void> openConnection() {
        if (ready != null) {
            return ready;
        }
        ready = CompletableFuture.runAsync(() -> {
            try {

                Path databaseFolder = plugin.getDataFolder().toPath();
                Files.createDirectories(databaseFolder);
                Path databaseFile = databaseFolder.resolve("player_data.db");

                Class.forName("org.sqlite.JDBC");

                source = new JdbcConnectionSource("jdbc:sqlite:" + databaseFile.toAbsolutePath());

                playerDao = DaoManager.createDao(source, PlayerData.class);
                TableUtils.createTableIfNotExists(source, PlayerData.class);

//                migratePlayerDataTable();

                List<PlayerData> players = playerDao.queryForAll();

                for (PlayerData playerData : players) {
                    PlayerDataCache.instance.put(
                            playerData.getUuid(),
                            playerData
                    );
                }

                warpDao = DaoManager.createDao(source, Warp.class);
                TableUtils.createTableIfNotExists(source, Warp.class);
                WarpCache.instance.clear();

                for (Warp warp : warpDao.queryForAll()) {
                    WarpCache.instance.addWarp(warp.getWarpName(), warp);
                }
            } catch (Exception e) {
                throw new CompletionException(
                        "Could not initialize the database",
                        e
                );
            }

        }, executor);
        return ready;

    }

    private PlayerData getOrCreatePlayerData(UUID uuid) throws SQLException {
        PlayerData data = playerDao.queryForId(uuid);

        if (data == null) {
            data = new PlayerData(uuid);
            playerDao.create(data);
        }
        PlayerDataCache.instance.put(uuid, data);
        return data;
    }
    private Warp requireWarp(String warpName) throws SQLException {
        Warp warp = warpDao.queryForId(warpName);

        if (warp == null) {
            throw new IllegalArgumentException(String.format("Warp %s does not exist", warpName));
        }
        return warp;
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return submit(() -> getOrCreatePlayerData(uuid));
    }


    public CompletableFuture<Void> setTokens(UUID uuid, long tokens) {
        return submit(()-> {
            PlayerData data = getOrCreatePlayerData(uuid);
            data.setTokens(tokens);
            playerDao.update(data);
            return null;
        });
    }
    public CompletableFuture<Long> getTokens(UUID uuid) {
        return getPlayerData(uuid).thenApply(PlayerData::getTokens);
    }
    public CompletableFuture<Void> createWarp(Warp warp) {
        return submit(() -> {
            if (warpDao.idExists(warp.getWarpName())) {
                throw new IllegalArgumentException(String.format("Warp %s already exists", warp.getWarpName()));
            }

            warpDao.create(warp);
            WarpCache.instance.addWarp(warp.getWarpName(), warp);
            return null;
        });
    }
    public CompletableFuture<Void> deleteWarp(String warpName) {
        return submit(() -> {
            Warp warp = requireWarp(warpName);
            warpDao.delete(warp);
            WarpCache.instance.removeWarp(warpName);
            return null;
        });
    }
    //helper methods
    private <T> CompletableFuture<T> submit(
            DatabaseOperation<T> operation
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.execute();
            } catch (SQLException exception) {
                throw new CompletionException(exception);
            }
        }, executor);
    }

    public CompletableFuture<Void> flush() {
        PlayerDataCache playerDataCache = PlayerDataCache.instance;
        Set<UUID> dirtyPlayers = playerDataCache.getDirtyPlayersSnapshot();

        if (dirtyPlayers.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return submit(()-> {
            for (UUID uuid : dirtyPlayers) {
                PlayerData data = playerDataCache.get(uuid);
                if (data == null) continue;

                playerDao.createOrUpdate(data);
                playerDataCache.markClean(uuid);
            }
            return null;
        });
    }

    public CompletableFuture<Void> updatePlayer(PlayerData data) {
        PlayerDataCache playerDataCache = PlayerDataCache.instance;

        return submit(()-> {
            playerDao.createOrUpdate(data);
            playerDataCache.markClean(data.getUuid());
            return null;
        });
    }


    @FunctionalInterface
    private interface DatabaseOperation<T> {
        T execute() throws SQLException;
    }
    // close on disabling
    public void close() {
        if (ready == null) {
            executor.shutdown();
            return;
        }

        try {
            ready.handle((ignored, error) -> null)
                    .thenRunAsync(() -> {
                        if (source == null) return;

                        try {
                            source.close();
                        } catch (Exception exception) {
                            plugin.getLogger().severe(
                                    "Could not close database: "
                                            + exception.getMessage()
                            );
                        }
                    }, executor)
                    .join();
        } finally {
            executor.shutdown();
        }
    }
}
