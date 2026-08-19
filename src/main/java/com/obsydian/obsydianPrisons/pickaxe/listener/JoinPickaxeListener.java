package com.obsydian.obsydianprisons.pickaxe.listener;

import com.obsydian.obsydianprisons.pickaxe.PickaxeUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinPickaxeListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.hasPlayedBefore()) return;
        p.getInventory().addItem(PickaxeUtils.createPickaxeItem(p));

    }
}
