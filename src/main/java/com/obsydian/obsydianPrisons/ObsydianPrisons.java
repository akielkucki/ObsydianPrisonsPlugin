package com.obsydian.obsydianprisons;

import com.obsydian.obsydianprisons.economy.Vault;
import com.obsydian.obsydianprisons.mine.MineRegionManager;
import com.obsydian.obsydianprisons.persistence.DatabaseManager;
import com.obsydian.obsydianprisons.pickaxe.listener.*;
import com.obsydian.obsydianprisons.player.PlayerDataCache;
import com.obsydian.obsydianprisons.player.PlayerDataFlushTask;
import com.obsydian.obsydianprisons.player.PlayerDataManager;
import com.obsydian.obsydianprisons.player.listener.ChatListener;
import com.obsydian.obsydianprisons.player.listener.PlayerConnectionsListener;
import com.obsydian.obsydianprisons.player.command.*;
import com.obsydian.obsydianprisons.player.listener.RefreshServerListEvent;
import com.obsydian.obsydianprisons.player.placeholders.ObsydianExpansion;
import com.obsydian.obsydianprisons.selling.AutoSellService;
import com.obsydian.obsydianprisons.selling.SellConfig;
import com.obsydian.obsydianprisons.selling.SellService;
import org.bukkit.Bukkit;
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
    private AutoSellService autoSellService;
    private SellService sellService;
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

        autoSellService = new AutoSellService(this);
        sellService = new SellService(this, configManager);

        // PAPI
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ObsydianExpansion(this).register();
        }
        // Event registry
        getServer().getPluginManager().registerEvents(new MultiToolListener(),this);
        getServer().getPluginManager().registerEvents(new JoinPickaxeListener(),this);
        getServer().getPluginManager().registerEvents(new PickaxeBreakListener(this, mineRegionManager),this);
        getServer().getPluginManager().registerEvents(new EnchantMenuListener(this),this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionsListener(this),this);
        getServer().getPluginManager().registerEvents(new MineEvents(this),this);
        getServer().getPluginManager().registerEvents(new RefreshServerListEvent(),this);
        getServer().getPluginManager().registerEvents(new ChatListener(this),this);

        // Command registry
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("autosell").setExecutor(new AutoSellCommand(this));
        getCommand("enderchest").setExecutor(new EnderChestCommand());
        getCommand("servinf").setExecutor(new FakeSpecsCommand(this));
        getCommand("start").setExecutor(new StartCommand());
        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("warp").setTabCompleter(new WarpCommand());
        getCommand("setwarp").setExecutor(new SetWarpCommand(this));
        getCommand("delwarp").setExecutor(new DeleteWarpCommand(this));
        getCommand("setmultiplier").setExecutor(new SetMultiplierCommand(this));

        // run tasks
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new PlayerDataFlushTask(this),0, 20 * 60); // Every 60 seconds
    }

    @Override
    public void onDisable() {
        if (databaseManager == null) return;
        databaseManager.flush();
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
    public AutoSellService getAutoSellService() {
        return autoSellService;
    }
    public SellService getSellService() {
        return sellService;
    }
}
