package com.obsydian.obsydianprisons.player.listener;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerConnectionsListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(PlayerConnectionsListener.class);
    private final ObsydianPrisons plugin;
    public PlayerConnectionsListener(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage("Welcome to Obsydian Prisons!");
        p.sendMessage("Use /help for a list of commands");
        plugin.getDatabaseManager().getPlayerData(p.getUniqueId()).thenAccept(playerData -> {
            log.info("Loaded player data for {}", p.getName());

            if (!playerData.getSettings().isAutoSellEnabled()
                    || !p.hasPermission("obsydianprisons.autosell")) {
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (p.isOnline()) {
                    plugin.getAutoSellService().enable(p);
                }
            });
        });
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        plugin.getAutoSellService().disable(p.getUniqueId());
        plugin.getPlayerDataManager().removePlayerData(p.getUniqueId());
    }


}
