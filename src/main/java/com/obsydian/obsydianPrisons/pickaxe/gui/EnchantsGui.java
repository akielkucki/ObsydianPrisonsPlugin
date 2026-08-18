package com.obsydian.obsydianprisons.pickaxe.gui;

import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EnchantsGui {

    public EnchantsGui() {}

    public Gui create(ItemStack pick) {
        return Gui.of(6)
                .title(Component.text("Your Available Enchants").color(NamedTextColor.GREEN))
                .statelessComponent(container -> container.setItem(
                        1,
                        2,
                        ItemBuilder.from(Material.DIAMOND)
                                .name(Component.text("Fortune", NamedTextColor.GREEN))
                                .lore(List.of(
                                        Component.text("Current Level: 0", NamedTextColor.GRAY),
                                        Component.text("Next Level: 1", NamedTextColor.GRAY)
                                ))
                                .asGuiItem((player, context) ->
                                        addEnchant(pick, Enchantment.FORTUNE)
                                )
                ))

                .build();
    }

    private void addEnchant(ItemStack item, Enchantment enchantment) {
        item.addUnsafeEnchantment(enchantment, item.getEnchantmentLevel(enchantment) + 1);
    }

}
