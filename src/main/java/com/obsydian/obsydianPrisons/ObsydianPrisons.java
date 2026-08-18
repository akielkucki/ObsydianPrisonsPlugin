package com.obsydian.obsydianPrisons;

import com.obsydian.obsydianPrisons.daos.DatabaseManager;
import com.obsydian.obsydianPrisons.daos.PlayerDataCache;
import com.obsydian.obsydianPrisons.globaltasks.CleanDirtySync;
import com.obsydian.obsydianPrisons.managers.PlayerDataManager;
import com.obsydian.obsydianPrisons.mines.MineRegionManager;
import com.obsydian.obsydianPrisons.pickaxe.listeners.EnchantMenuListeners;
import com.obsydian.obsydianPrisons.pickaxe.listeners.JoinPickaxeListener;
import com.obsydian.obsydianPrisons.pickaxe.listeners.MultiToolListener;
import com.obsydian.obsydianPrisons.pickaxe.listeners.PickaxeBreakListeners;
import com.obsydian.obsydianPrisons.sell.cfg.ConfigManager;
import com.obsydian.obsydianPrisons.sell.commands.SellCommand;
import com.obsydian.obsydianPrisons.server.commands.EnderChestCommand;
import com.obsydian.obsydianPrisons.server.listeners.ServerJoinListener;
import com.obsydian.obsydianPrisons.utils.Vault;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class ObsydianPrisons extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(ObsydianPrisons.class);

    private static ObsydianPrisons instance;
    public static ObsydianPrisons getInstance() {
        return instance;
    }

    private ConfigManager configManager;
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

        configManager = new ConfigManager(this);
        configManager.setupConfig();
        configManager.loadFromDisk();
        playerDataManager = new PlayerDataManager(PlayerDataCache.instance);

        // Event registry
        getServer().getPluginManager().registerEvents(new MultiToolListener(),this);
        getServer().getPluginManager().registerEvents(new JoinPickaxeListener(),this);
        getServer().getPluginManager().registerEvents(new PickaxeBreakListeners(this, mineRegionManager),this);
        getServer().getPluginManager().registerEvents(new EnchantMenuListeners(),this);
        getServer().getPluginManager().registerEvents(new ServerJoinListener(this),this);

        // Command registry
        getCommand("sell").setExecutor(new SellCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());

        // run tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new CleanDirtySync(this),0, 20 * 60); // Every 60 seconds
    }

    @Override
    public void onDisable() {
        if (databaseManager == null) return;
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

        mineRegionManager.loadDirectory(plotMinesDirectory);
        mineRegionManager.loadDirectory(cataMinesDirectory);
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
