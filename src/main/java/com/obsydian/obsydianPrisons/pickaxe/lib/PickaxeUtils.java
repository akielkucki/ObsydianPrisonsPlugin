package com.obsydian.obsydianPrisons.pickaxe.lib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PickaxeUtils {
    public static ItemStack createPickaxeItem(Player user) {
        ItemStack item = new ItemStack(org.bukkit.Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(String.format("%s's Pickaxe", user.getName()))
                .color(TextColor.color(0x00FF00)));
        meta.setUnbreakable(true);
       meta.addEnchant(Enchantment.EFFICIENCY, 155, true);
       var pdc = meta.getPersistentDataContainer();
       pdc.set(Keys.PICKAXE, PersistentDataType.STRING, user.getUniqueId().toString());
        item.setItemMeta(meta);
        return item;
    }
}
