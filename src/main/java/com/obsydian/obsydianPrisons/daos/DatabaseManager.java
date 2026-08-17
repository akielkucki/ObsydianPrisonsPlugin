package com.obsydian.obsydianPrisons.daos;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.obsydian.obsydianPrisons.models.PlayerData;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
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
    private CompletableFuture<Void> ready;
    private final JavaPlugin plugin;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> openConnection() {
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

            } catch (Exception e) {
                throw new CompletionException(
                        "Could not initialize the database",
                        e
                );
            }

        });
        return ready;

    }

    private PlayerData getOrCreatePlayerData(UUID uuid) throws SQLException {
        PlayerData data = playerDao.queryForId(uuid);

        if (data == null) {
            data = new PlayerData(uuid);
            playerDao.create(data);
        }

        return data;
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return submit(() -> getOrCreatePlayerData(uuid));
    }

    public CompletableFuture<Void> addTokens(UUID uuid, long tokens) {
        return submit(()-> {
            PlayerData data = getOrCreatePlayerData(uuid);
            data.setTokens(data.getTokens() + tokens);
            playerDao.update(data);
            return null;
        });
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
