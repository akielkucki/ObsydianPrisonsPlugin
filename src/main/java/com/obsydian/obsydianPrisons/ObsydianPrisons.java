package com.obsydian.obsydianprisons;

import com.obsydian.obsydianprisons.economy.Vault;
import com.obsydian.obsydianprisons.mine.MineRegionManager;
import com.obsydian.obsydianprisons.persistence.DatabaseManager;
import com.obsydian.obsydianprisons.pickaxe.listener.EnchantMenuListener;
import com.obsydian.obsydianprisons.pickaxe.listener.JoinPickaxeListener;
import com.obsydian.obsydianprisons.pickaxe.listener.MultiToolListener;
import com.obsydian.obsydianprisons.pickaxe.listener.PickaxeBreakListener;
import com.obsydian.obsydianprisons.player.PlayerDataCache;
import com.obsydian.obsydianprisons.player.PlayerDataFlushTask;
import com.obsydian.obsydianprisons.player.PlayerDataManager;
import com.obsydian.obsydianprisons.player.PlayerJoinListener;
import com.obsydian.obsydianprisons.player.command.EnderChestCommand;
import com.obsydian.obsydianprisons.player.command.FakeSpecsCommand;
import com.obsydian.obsydianprisons.player.command.StartCommand;
import com.obsydian.obsydianprisons.selling.SellCommand;
import com.obsydian.obsydianprisons.selling.SellConfig;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public final class ObsydianPrisons extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(ObsydianPrisons.class);

    private static ObsydianPrisons instance;
    public static ObsydianPrisons getInstance() {
        return instance;
    }

    private SellConfig configManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private MineRegionManager mineRegionManager;
    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onEnable() {
        instance = this;
        if (!Vault.setup(this)) {
            log.error("Vault is not enabled, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        mineRegionManager = new MineRegionManager();
        //Server Specific
        loadMineDirs();

        databaseManager = new DatabaseManager(this);
        openDBConnection();

        configManager = new SellConfig(this);
        configManager.setupConfig();
        configManager.loadFromDisk();
        playerDataManager = new PlayerDataManager(PlayerDataCache.instance);

        // Event registry
        getServer().getPluginManager().registerEvents(new MultiToolListener(),this);
        getServer().getPluginManager().registerEvents(new JoinPickaxeListener(),this);
        getServer().getPluginManager().registerEvents(new PickaxeBreakListener(this, mineRegionManager),this);
        getServer().getPluginManager().registerEvents(new EnchantMenuListener(this),this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this),this);

        // Command registry
        getCommand("sell").setExecutor(new SellCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());
        getCommand("servinf").setExecutor(new FakeSpecsCommand(this));
        getCommand("start").setExecutor(new StartCommand());

        // run tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new PlayerDataFlushTask(this),0, 20 * 60); // Every 60 seconds
    }

    @Override
    public void onDisable() {
        if (databaseManager == null) return;
        databaseManager.close();
    }

    public SellConfig getConfigManager() {
        return configManager;
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    public MineRegionManager getMineRegionManager() {
        return mineRegionManager;
    }
    private Path resolvePluginsDir() {
        Path pluginsDirectory = getDataFolder()
                .toPath()
                .toAbsolutePath()
                .getParent();

        if (pluginsDirectory == null) {
            throw new IllegalStateException(
                    "Could not determine the plugins directory"
            );
        }
        return pluginsDirectory;
    }
    private void loadMineDirs() {
        Path pluginsDirectory = resolvePluginsDir();
        Path plotMinesDirectory = pluginsDirectory
                .resolve("mango-plotmines")
                .resolve("data");

        Path cataMinesDirectory = pluginsDirectory
                .resolve("CataMines")
                .resolve("mines");

        mineRegionManager.loadDirectories(List.of(plotMinesDirectory,cataMinesDirectory));

    }
    private void openDBConnection() {
        databaseManager.openConnection()
                .whenComplete((ignored, error) -> {
                    if (error == null) {
                        log.info("Database connection established");
                        return;
                    }
                    log.error("Failed to establish database connection, fix database errors or contact the owner before enabling", error);

                    getServer().getScheduler().runTask(
                            this,
                            () -> getServer()
                                    .getPluginManager()
                                    .disablePlugin(this)
                    );
                });
    }
}
