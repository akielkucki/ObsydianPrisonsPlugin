package com.obsydian.obsydianprisons.pickaxe;

import com.obsydian.obsydianprisons.pickaxe.models.Pickaxe;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PickaxeUtils {
    public static ItemStack createPickaxeItem(Player user) {
        ItemStack item = new ItemStack(org.bukkit.Material.DIAMOND_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(String.format("%s's Pickaxe", user.getName()))
                .color(TextColor.color(0x00FF00)));
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.EFFICIENCY, 10, true);
        var pdc = meta.getPersistentDataContainer();
        pdc.set(PickaxeKeys.PICKAXE, PersistentDataType.STRING, user.getUniqueId().toString());
        pdc.set(PickaxeKeys.TIMESTAMP, PersistentDataType.LONG, System.currentTimeMillis());
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        updatePickaxeItem(item);
        return item;
    }
    private static final Key UNIFORM_FONT = Key.key("minecraft:uniform");

    private static final int NAME_WIDTH = Arrays.stream(PickaxeEnchantment.values())
            .mapToInt(enchantment -> enchantment.displayName().length())
            .max()
            .orElse(0);

    private static final int LEVEL_WIDTH = Arrays.stream(PickaxeEnchantment.values())
            .mapToInt(enchantment -> String.valueOf(enchantment.maxLevel()).length())
            .max()
            .orElse(1);
    public static void updatePickaxeItem(ItemStack item) {
        if (!Pickaxe.isPickaxe(item)) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        Pickaxe pickaxe = new Pickaxe(item);

        List<Component> lore = new ArrayList<>();

        lore.add(Component.text("[ᴇɴᴄʜᴀɴᴛᴍᴇɴᴛѕ]", TextColor.color(0x9A00FF))
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.space());

        for (PickaxeEnchantment enchantment : PickaxeEnchantment.values()) {
            pickaxe.getEnchantment(enchantment).ifPresent(storedEnchantment -> {
                int level = pickaxe.getEnchantmentLevel(storedEnchantment);
                if (level <= 0) return;

                String currentLevel = String.format(Locale.ROOT, "%d", level);

                if (level < enchantment.maxLevel())
                    lore.add(Component.text()
                        .append(Component.text(" " + enchantment.displayName() + " ", TextColor.color(0x7C7C7C)))
                        .append(Component.text("[", TextColor.color(0x9A00FF)))
                        .append(Component.text(currentLevel, TextColor.color(0xFFFFFF)))
                        .append(Component.text("/", TextColor.color(0x888888)))
                        .append(Component.text(enchantment.maxLevel(), TextColor.color(0xFFFFFF)))
                        .append(Component.text("]", TextColor.color(0x9A00FF)))
                        .decoration(TextDecoration.ITALIC, false)
                        .build());
                else
                    lore.add(Component.text()
                            .append(Component.text(" " + enchantment.displayName(), TextColor.color(0x7C7C7C)))
                            .append(Component.text("[", TextColor.color(0x9A00FF)))
                            .append(Component.text("MAX", TextColor.color(0xFFF900)))
                            .append(Component.text("]", TextColor.color(0x9A00FF)))
                            .decoration(TextDecoration.ITALIC, false)
                            .build());
            });

        }
        lore.add(Component.space());
        lore.add(Component.text("[Doesn't drop on death]  ", TextColor.color(0xFFF900)).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
    }
    private static String longestEnchantmentName = "";
    public static String getLongestEnchantmentName() {
        if (!longestEnchantmentName.isEmpty()) return longestEnchantmentName;
        for (PickaxeEnchantment enchantment : PickaxeEnchantment.values()) {
            longestEnchantmentName = enchantment.displayName().length() > longestEnchantmentName.length() ? enchantment.displayName() : longestEnchantmentName;
        }
        return longestEnchantmentName;
    }

}
