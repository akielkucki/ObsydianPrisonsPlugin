package com.obsydian.obsydianPrisons.pickaxe.listeners;

import com.obsydian.obsydianPrisons.pickaxe.lib.Keys;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiToolListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(MultiToolListener.class);

    @EventHandler
    public void onInteractWithBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack heldItem = event.getItem();
        Block block = event.getClickedBlock();

        if (heldItem == null || block == null) return;

        if (!heldItem.getPersistentDataContainer().has(Keys.PICKAXE)) {
            return;
        }

        Material preferredType = getPreferredToolType(block);

        if (preferredType == null || heldItem.getType() == preferredType) {
            return;
        }

        heldItem.setType(preferredType);
        player.getInventory().setItemInMainHand(heldItem);

        log.info(
                "Set preferred tool for {} to {}",
                player.getName(),
                preferredType
        );
    }

    private @Nullable Material getPreferredToolType(Block block) {
        Material blockType = block.getType();

        if (Tag.MINEABLE_PICKAXE.isTagged(blockType)) {
            return Material.DIAMOND_PICKAXE;
        }

        if (Tag.MINEABLE_SHOVEL.isTagged(blockType)) {
            return Material.DIAMOND_SHOVEL;
        }

        if (Tag.MINEABLE_AXE.isTagged(blockType)) {
            return Material.DIAMOND_AXE;
        }

        if (Tag.MINEABLE_HOE.isTagged(blockType)) {
            return Material.DIAMOND_HOE;
        }

        return null;
    }
}
