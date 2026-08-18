package com.obsydian.obsydianprisons.pickaxe.listener;

import com.obsydian.obsydianprisons.pickaxe.PickaxeKeys;
import com.obsydian.obsydianprisons.pickaxe.gui.EnchantsGui;
import dev.triumphteam.gui.paper.Gui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantMenuListener implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) return;
        if (e.getItem() == null || e.getItem().getType().isAir()) return;
        ItemStack heldItem = e.getItem();
        var pdc = heldItem.getPersistentDataContainer();
        if (!pdc.has(PickaxeKeys.PICKAXE) || pdc.isEmpty()) return;

        Gui gui = new EnchantsGui().create(heldItem);
        gui.open(e.getPlayer());
    }
}
