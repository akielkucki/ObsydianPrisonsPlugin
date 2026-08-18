package com.obsydian.obsydianprisons.pickaxe;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum PickaxeEnchantment {
    FORTUNE(
            "Fortune",
            Material.EMERALD,
            "Increases the amount of blocks you receive.",
            100,
            50,
            Enchantment.FORTUNE,
            null
    ),
    GEM_FORTUNE(
            "Gem Fortune",
            Material.AMETHYST_SHARD,
            "Increases the amount of gems found while mining.",
            250,
            125,
            null,
            PickaxeKeys.GEM_FORTUNE
    ),
    BLAST(
            "Blast",
            Material.TNT,
            "Increases the chance to blast nearby mine blocks.",
            4,
            19200,
            null,
            PickaxeKeys.BLAST
    ),
    JACKHAMMER(
            "Jackhammer",
            Material.PISTON,
            "Increases the chance to break a cuboid mine column.",
            100,
            150,
            null,
            PickaxeKeys.JACKHAMMER
    ),
    EFFICIENCY(
            "Efficiency",
            Material.GOLDEN_PICKAXE,
            "Increases how quickly your pickaxe breaks blocks.",
            250,
            75,
            Enchantment.EFFICIENCY,
            null
    ),
    MINESTREAK(
            "Minestreak",
            Material.BLAZE_POWDER,
            "Increases the chance to activate a streak while continuously mining.",
            250,
            200,
            null,
            PickaxeKeys.MINESTREAK
    );

    private final String displayName;
    private final Material icon;
    private final String description;
    private final int maxLevel;
    private final long baseCost;
    private final Enchantment bukkitEnchantment;
    private final NamespacedKey persistentKey;

    PickaxeEnchantment(
            String displayName,
            Material icon,
            String description,
            int maxLevel,
            long baseCost,
            Enchantment bukkitEnchantment,
            NamespacedKey persistentKey
    ) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.bukkitEnchantment = bukkitEnchantment;
        this.persistentKey = persistentKey;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public String description() {
        return description;
    }
    public NamespacedKey persistentKey() {
        return persistentKey;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public int level(ItemStack item) {
        if (bukkitEnchantment != null) {
            return item.getEnchantmentLevel(bukkitEnchantment);
        }

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(
                persistentKey,
                PersistentDataType.INTEGER,
                0
        );
    }

    public long priceForLevel(int level) {
        return Math.multiplyExact(baseCost, level);
    }

    public void setLevel(ItemStack item, int level) {
        if (level < 0 || level > maxLevel) {
            throw new IllegalArgumentException("Invalid level " + level + " for " + displayName);
        }

        if (bukkitEnchantment != null) {
            item.addUnsafeEnchantment(bukkitEnchantment, level);
            return;
        }

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                persistentKey,
                PersistentDataType.INTEGER,
                level
        );
        item.setItemMeta(meta);
    }

}
