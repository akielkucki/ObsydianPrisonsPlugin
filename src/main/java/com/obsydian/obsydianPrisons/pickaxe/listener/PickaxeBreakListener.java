package com.obsydian.obsydianprisons.pickaxe.listener;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.mine.MineRegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PickaxeBreakListener implements Listener {
    private final ObsydianPrisons plugin;
    private final MineRegionManager mineRegionManager;
    ThreadLocalRandom rand = ThreadLocalRandom.current();

    public PickaxeBreakListener(ObsydianPrisons plugin, MineRegionManager mineRegionManager) {
        this.plugin = plugin;
        this.mineRegionManager = mineRegionManager;
    }
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        // implement dynamic particle selection
        Location flameLocation = loc.clone().add(0.5, 0.35, 0.5);

        loc.getWorld().spawnParticle(
                Particle.FLAME,
                flameLocation,
                5,      // particle count
                0.2,    // horizontal spread
                0.15,   // vertical spread
                0.2,    // horizontal spread
                0.01    // particle speed
        );

    }

    @EventHandler
    public void addDropsToInventory(BlockDropItemEvent e) {
        Player p = e.getPlayer();
        if (!mineRegionManager.isMineBlock(e.getBlock())) return;
        //TODO: Fix json mine check not working
        List<ItemStack> drops = e.getItems().stream().map(item -> {
            ItemStack tool = p.getInventory().getItemInMainHand();
            ItemStack itemStack = item.getItemStack();
            if (item.getItemStack().getType().isSolid()) {
                if (tool.getEnchantmentLevel(Enchantment.FORTUNE) > 0) {
                    int level = tool.getEnchantmentLevel(Enchantment.FORTUNE);
                    itemStack.setAmount(itemStack.getAmount() + (int) (rand.nextFloat() * (level + 1)));
                }
            }
            return itemStack;
        }).toList();
        p.getInventory().addItem(drops.toArray(new ItemStack[0]));

        int gemFortuneLevel = 3;

        double chance = Math.min(
                0.1,
                0.05 + gemFortuneLevel * 0.0005
        );

        if (rand.nextDouble()*2.0 < chance) {
            int minimum = 10 + gemFortuneLevel;
            int maximum = 20 + gemFortuneLevel * 2;

            int tokens = rand.nextInt(minimum, maximum + 1);

            p.sendMessage(Component.text(
                    "+" + tokens + " GEMS",
                    TextColor.color(0x18CB00)
            ).decorate(TextDecoration.BOLD));
            plugin.getPlayerDataManager().addTokens(p.getUniqueId(), tokens);
        }
        if (p.getInventory().firstEmpty() == -1) {
            if (rand.nextInt(1,10) < 2) {
                TextDisplay display = createTextDisplay(Component.text("Inventory Full! /sell to sell your items")
                                .color(TextColor.color(0xFF4D51)),
                        e.getBlock().getLocation(),
                        p,
                        ObsydianPrisons.getInstance());
                deleteAfter(20, display);
                p.sendMessage(Component.text("Inventory Full! /sell to sell your items").color(TextColor.color(0xFF000C)));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);

            }
        }

        e.setCancelled(true);
    }
    private TextDisplay createTextDisplay(Component text, Location location, Player viewer, JavaPlugin plugin) {
        TextDisplay dp = location.getWorld().spawn(
                location,
                TextDisplay.class,
                display -> {
                    display.text(text);
                    display.setBillboard(Display.Billboard.CENTER);
                    display.setSeeThrough(true);

                    // Must be set before showing the display.
                    display.setVisibleByDefault(false);
                    display.setPersistent(false);
                }
        );

        viewer.showEntity(plugin, dp);
        return dp;
    }
    private void deleteAfter(long ticks, TextDisplay display) {
        ObsydianPrisons plugin = ObsydianPrisons.getInstance();
        plugin
                .getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(plugin, display::remove,ticks);
    }
}
