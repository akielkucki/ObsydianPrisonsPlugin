package com.obsydian.obsydianPrisons;

import com.obsydian.obsydianPrisons.daos.DatabaseManager;
import com.obsydian.obsydianPrisons.daos.PlayerDataCache;
import com.obsydian.obsydianPrisons.globaltasks.CleanDirtySync;
import com.obsydian.obsydianPrisons.managers.PlayerDataManager;
import com.obsydian.obsydianPrisons.pickaxe.listeners.EnchantMenuListeners;
import com.obsydian.obsydianPrisons.pickaxe.listeners.JoinPickaxeListener;
import com.obsydian.obsydianPrisons.pickaxe.listeners.MultiToolListener;
import com.obsydian.obsydianPrisons.pickaxe.listeners.PickaxeBreakListeners;
import com.obsydian.obsydianPrisons.sell.cfg.ConfigManager;
import com.obsydian.obsydianPrisons.sell.commands.SellCommand;
import com.obsydian.obsydianPrisons.server.listeners.ServerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ObsydianPrisons extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(ObsydianPrisons.class);

    private static ObsydianPrisons instance;
    public static ObsydianPrisons getInstance() {
        return instance;
    }

    ConfigManager configManager;
    DatabaseManager databaseManager;
    PlayerDataManager playerDataManager;

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void onEnable() {
        instance = this;

        databaseManager = new DatabaseManager(this);
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

        configManager = new ConfigManager(this);
        configManager.setupConfig();
        configManager.loadFromDisk();
        playerDataManager = new PlayerDataManager(PlayerDataCache.instance);

        // Event registry
        getServer().getPluginManager().registerEvents(new MultiToolListener(),this);
        getServer().getPluginManager().registerEvents(new JoinPickaxeListener(),this);
        getServer().getPluginManager().registerEvents(new PickaxeBreakListeners(this),this);
        getServer().getPluginManager().registerEvents(new EnchantMenuListeners(),this);
        getServer().getPluginManager().registerEvents(new ServerJoinListener(this),this);

        // Command registry
        getCommand("sell").setExecutor(new SellCommand());

        // run tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CleanDirtySync(this),0, 20 * 60); // Every 60 seconds
    }

    @Override
    public void onDisable() {
        databaseManager.close();
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
