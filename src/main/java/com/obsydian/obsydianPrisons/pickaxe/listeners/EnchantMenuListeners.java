package com.obsydian.obsydianPrisons.pickaxe.listeners;

import com.obsydian.obsydianPrisons.pickaxe.lib.EnchantsGui;
import com.obsydian.obsydianPrisons.pickaxe.lib.Keys;
import dev.triumphteam.gui.paper.Gui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantMenuListeners implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) return;
        if (e.getItem() == null || e.getItem().getType().isAir()) return;
        ItemStack heldItem = e.getItem();
        var pdc = heldItem.getPersistentDataContainer();
        if (!pdc.has(Keys.PICKAXE) || pdc.isEmpty()) return;

        Gui gui = new EnchantsGui().create(heldItem);
        gui.open(e.getPlayer());
    }
}
