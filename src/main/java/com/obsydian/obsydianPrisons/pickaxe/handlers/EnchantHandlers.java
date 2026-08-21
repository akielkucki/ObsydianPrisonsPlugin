package com.obsydian.obsydianprisons.pickaxe.handlers;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.mine.MineRegionManager;
import com.obsydian.obsydianprisons.pickaxe.PickaxeEnchantment;
import com.obsydian.obsydianprisons.pickaxe.models.Pickaxe;
import com.obsydian.obsydianprisons.selling.SellConfig;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantHandlers {
    private static final Logger log = LoggerFactory.getLogger(EnchantHandlers.class);
    private final Set<UUID> automatedBreaks = new HashSet<>();
    private final MineRegionManager mineRegionManager;
    private final ThreadLocalRandom rand = ThreadLocalRandom.current();
    private final ObsydianPrisons plugin;
    private final SellConfig configManager;

    public EnchantHandlers(MineRegionManager mineRegionManager) {
        this.mineRegionManager = mineRegionManager;
        this.plugin = ObsydianPrisons.getInstance();
        this.configManager = plugin.getConfigManager();
    }

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

            world.spawnParticle(Particle.EXPLOSION, location, 2, 0.2, 0.2, 0.2, 0.01);
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

            for (int x = -BLAST_RADIUS; x <= BLAST_RADIUS; x++) {
                for (int y = -BLAST_RADIUS; y <= BLAST_RADIUS; y++) {
                    for (int z = -BLAST_RADIUS; z <= BLAST_RADIUS; z++) {
                        int dSquared = (x * x) + (y * y) + (z * z);
                        if (dSquared > radiusSquared) {
                            continue;
                        }
                        Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                        if (block.getType().isAir() || block.getType() == Material.BEDROCK) continue;
                        if (!mineRegionManager.isMineBlock(block)) continue;
                        player.breakBlock(block);

                    }
                }
            }
        } finally {
            automatedBreaks.remove(player.getUniqueId());
        }

    }
    private static final int JACKHAMMER_RADIUS = 1; // 3×3
    private static final int JACKHAMMER_DEPTH = 7;

    private static final double JACKHAMMER_CHANCE_PER_LEVEL = 0.00002;
    private static final double JACKHAMMER_MAX_CHANCE = 0.02;
    public void handleJackhammerEnchant(
            Pickaxe pickaxe,
            Location location,
            Player player
    ) {
        int level = pickaxe.getEnchantmentLevel(
                PickaxeEnchantment.JACKHAMMER
        );

        if (level <= 0 || automatedBreaks.contains(player.getUniqueId())) {
            return;
        }

        double chance = Math.min(
                JACKHAMMER_MAX_CHANCE,
                level * JACKHAMMER_CHANCE_PER_LEVEL
        );

        if (rand.nextDouble() >= chance) {
            return;
        }

        World world = location.getWorld();

        if (world == null) {
            return;
        }

        automatedBreaks.add(player.getUniqueId());

        try {
            Vector direction = player.getEyeLocation().getDirection();

            int[] forward = getDominantDirection(direction);

            int forwardX = forward[0];
            int forwardY = forward[1];
            int forwardZ = forward[2];

            /*
             * Select two perpendicular axes for the 3×3 cross-section.
             */
            int[] right;
            int[] up;

            if (forwardX != 0) {
                right = new int[]{0, 0, 1};
                up = new int[]{0, 1, 0};
            } else if (forwardY != 0) {
                right = new int[]{1, 0, 0};
                up = new int[]{0, 0, 1};
            } else {
                right = new int[]{1, 0, 0};
                up = new int[]{0, 1, 0};
            }

            int originX = location.getBlockX();
            int originY = location.getBlockY();
            int originZ = location.getBlockZ();

            for (int depth = 0; depth < JACKHAMMER_DEPTH; depth++) {
                int centerX = originX + forwardX * depth;
                int centerY = originY + forwardY * depth;
                int centerZ = originZ + forwardZ * depth;

                for (int horizontal = -JACKHAMMER_RADIUS;
                     horizontal <= JACKHAMMER_RADIUS;
                     horizontal++) {

                    for (int vertical = -JACKHAMMER_RADIUS;
                         vertical <= JACKHAMMER_RADIUS;
                         vertical++) {

                        int x = centerX
                                + right[0] * horizontal
                                + up[0] * vertical;

                        int y = centerY
                                + right[1] * horizontal
                                + up[1] * vertical;

                        int z = centerZ
                                + right[2] * horizontal
                                + up[2] * vertical;

                        Block block = world.getBlockAt(x, y, z);

                        if (block.getType().isAir()) {
                            continue;
                        }

                        if (!mineRegionManager.isMineBlock(block)) {
                            continue;
                        }

                        player.breakBlock(block);
                        world.playSound(block.getLocation(), Sound.BLOCK_STONE_HIT, 1, 1);
                    }
                }
            }
        } finally {
            automatedBreaks.remove(player.getUniqueId());
        }
    }
    private int[] getDominantDirection(Vector direction) {
        double absoluteX = Math.abs(direction.getX());
        double absoluteY = Math.abs(direction.getY());
        double absoluteZ = Math.abs(direction.getZ());

        if (absoluteX >= absoluteY && absoluteX >= absoluteZ) {
            return new int[]{
                    direction.getX() >= 0 ? 1 : -1,
                    0,
                    0
            };
        }

        if (absoluteY >= absoluteX && absoluteY >= absoluteZ) {
            return new int[]{
                    0,
                    direction.getY() >= 0 ? 1 : -1,
                    0
            };
        }

        return new int[]{
                0,
                0,
                direction.getZ() >= 0 ? 1 : -1
        };
    }
    public void handleFortune(Item item, Pickaxe pickaxe, ItemStack tool, ItemStack itemStack) {
        if (item.getItemStack().getType().isSolid()) {
            if (pickaxe.getEnchantmentLevel(PickaxeEnchantment.FORTUNE) > 0) {
                int level = tool.getEnchantmentLevel(Enchantment.FORTUNE);
                itemStack.setAmount(itemStack.getAmount() + (int) (rand.nextFloat() * (level + 1)));
            }
        }
    }
    public int handleGemFortune(Pickaxe pickaxe, Material blockBroken) {
        int level = pickaxe.getEnchantmentLevel(
                PickaxeEnchantment.GEM_FORTUNE
        );


        double blockMultiplier = getBlockValue(blockBroken);

        if (blockMultiplier <= 0) {
            return 0;
        }

        double chance = Math.min(
                0.02,
                0.01 + level * 0.0005 * blockMultiplier
        );

        if (rand.nextDouble() >= chance) {
            return 0;
        }

        /*
          Converts the 0.5–5.0 block multiplier into 0.875–2.0.

          Block multiplier 0.5 -> reward multiplier 0.875
          Block multiplier 1.0 -> reward multiplier 1.0
          Block multiplier 3.0 -> reward multiplier 1.5
          Block multiplier 5.0 -> reward multiplier 2.0
         */
        double rewardMultiplier =
                1.0 + (blockMultiplier - 1.0) * 0.25;

        int minimum = (int) Math.round(
                (10 + level) * rewardMultiplier
        );

        int maximum = (int) Math.round(
                (20 + level * 2) * rewardMultiplier
        );

        // The upper bound is exclusive, so add one.
        return rand.nextInt(minimum, maximum + 1);
    }
    private double getBlockValue(Material material) {
        double worth = configManager.getWorth(material);
        if (worth <= 0) {
            return 0;
        }

        double referenceWorth = 10.0; // Worth of an ordinary mine block
        double multiplier = Math.pow(worth / referenceWorth, 0.25);

        // Prevent extremely cheap or expensive blocks from breaking balance
        return Math.max(0.5, Math.min(multiplier, 5.0));
    }

}
