package com.obsydian.obsydianprisons.pickaxe.listener;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.mine.MineRegionManager;
import com.obsydian.obsydianprisons.pickaxe.PickaxeEnchantment;
import com.obsydian.obsydianprisons.pickaxe.PickaxeKeys;
import com.obsydian.obsydianprisons.pickaxe.models.Pickaxe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
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
        var pdc = p.getInventory().getItemInMainHand().getPersistentDataContainer();
        if (!pdc.has(PickaxeKeys.PICKAXE) || pdc.isEmpty()) return;
        if (!mineRegionManager.isMineBlock(e.getBlock())) return;
        ItemStack tool = p.getInventory().getItemInMainHand();

        Pickaxe pickaxe = new Pickaxe(tool);
        Block block = e.getBlock();
        //TODO: Fix json mine check not working
        List<ItemStack> drops = e.getItems().stream().map(item -> {
            ItemStack itemStack = item.getItemStack();
            handleFortune(item, pickaxe, tool, itemStack);

            return itemStack;
        }).toList();
        handleBlastEnchant(pickaxe, block.getLocation(), p);
        int tokens = handleGemFortune(pickaxe);
        if (tokens > 0) {
            p.sendMessage(Component.text("You found ").color(TextColor.color(0x22FF0C)).decorate(TextDecoration.BOLD)
                    .append(Component.text(tokens + " tokens!").color(TextColor.color(0x22FF0C))).decorate(TextDecoration.BOLD));
            plugin.getPlayerDataManager().addTokens(p.getUniqueId(), tokens);
        }

        p.getInventory().addItem(drops.toArray(new ItemStack[0]));


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
    private final Set<UUID> automatedBreaks = new HashSet<>();
    public void handleBlastEnchant(Pickaxe pickaxe, Location location, Player player) {
        if (pickaxe.getEnchantmentLevel(PickaxeEnchantment.BLAST) == 0) return;
        if (automatedBreaks.contains(player.getUniqueId())) return; //avoid recursion
        try {
            automatedBreaks.add(player.getUniqueId());
            final int BLAST_RADIUS = pickaxe.getEnchantmentLevel(PickaxeEnchantment.BLAST);

            World world = location.getWorld();
            int centerX = location.getBlockX();
            int centerY = location.getBlockY();
            int centerZ = location.getBlockZ();
            int radiusSquared = BLAST_RADIUS * BLAST_RADIUS;

            world.spawnParticle(Particle.EXPLOSION, location, 10, 0.2, 0.2, 0.2, 0.01);
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

            for (int x = -BLAST_RADIUS; x <= BLAST_RADIUS; x++) {
                for (int y = -BLAST_RADIUS; y <= BLAST_RADIUS; y++) {
                    for (int z = -BLAST_RADIUS; z <= BLAST_RADIUS; z++) {
                        int dSquared = (x * x) + (y * y) + (z * z);
                        if (dSquared > radiusSquared) {
                            continue;
                        }
                        Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                        if (block.getType().isAir()) continue;
                        if (!mineRegionManager.isMineBlock(block)) continue;
                        player.breakBlock(block);

                    }
                }
            }
        } finally {
            automatedBreaks.remove(player.getUniqueId());
        }

    }
    private void handleFortune(Item item, Pickaxe pickaxe, ItemStack tool, ItemStack itemStack) {
        if (item.getItemStack().getType().isSolid()) {
            if (pickaxe.getEnchantmentLevel(PickaxeEnchantment.FORTUNE) > 0) {
                int level = tool.getEnchantmentLevel(Enchantment.FORTUNE);
                itemStack.setAmount(itemStack.getAmount() + (int) (rand.nextFloat() * (level + 1)));
            }
        }
    }
    private int handleGemFortune(Pickaxe pickaxe) {

            int gemFortuneLevel = pickaxe.getEnchantmentLevel(PickaxeEnchantment.GEM_FORTUNE);

            double chance = Math.min(
                    0.06,
                    0.05 + gemFortuneLevel * 0.0005
            );

            if (rand.nextDouble()*5.0 < chance) {
                int minimum = 10 + gemFortuneLevel;
                int maximum = 20 + gemFortuneLevel * 2;


                return rand.nextInt(minimum, maximum + 1);
            } else return 0;

    }

}
