package com.obsydian.obsydianprisons.player;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerJoinListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(PlayerJoinListener.class);
    private final ObsydianPrisons plugin;
    public PlayerJoinListener(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage("Welcome to Obsydian Prisons!");
        p.sendMessage("Use /help for a list of commands");
        plugin.getDatabaseManager().getPlayerData(p.getUniqueId()).thenRun(()-> log.info("Loaded player data for {}", p.getName()));
    }
}
