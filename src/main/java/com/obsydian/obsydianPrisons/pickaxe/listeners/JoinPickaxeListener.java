package com.obsydian.obsydianPrisons.pickaxe.listeners;

import com.obsydian.obsydianPrisons.pickaxe.lib.PickaxeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinPickaxeListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.getInventory().addItem(PickaxeUtils.createPickaxeItem(p));
    }
}
