package com.obsydian.obsydianprisons.pickaxe.models;

import com.obsydian.obsydianprisons.pickaxe.PickaxeEnchantment;
import com.obsydian.obsydianprisons.pickaxe.PickaxeKeys;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class Pickaxe {
    private final ItemStack item;

    public Pickaxe(ItemStack item) {
        this.item = item;
    }
    public Optional<PickaxeEnchantment> getEnchantment(PickaxeEnchantment enchantment) {
        return enchantment.level(item) > 0
                ? Optional.of(enchantment)
                : Optional.empty();
    }
    public int getEnchantmentLevel(PickaxeEnchantment enchantment) {
        return enchantment.level(item);
    }

    public ItemStack getItem() {
        return item;
    }
    public static boolean isPickaxe(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        var pdc = item.getPersistentDataContainer();
        return pdc.has(PickaxeKeys.PICKAXE);
    }

}
