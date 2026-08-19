package com.obsydian.obsydianprisons.pickaxe.gui;

import com.obsydian.obsydianprisons.pickaxe.PickaxeEnchantment;
import com.obsydian.obsydianprisons.player.PlayerDataManager;
import dev.triumphteam.gui.click.ClickContext;
import dev.triumphteam.gui.click.GuiClick;
import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import dev.triumphteam.nova.MutableState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EnchantsGui {

    private static final int[] ENCHANT_COLUMNS = {2, 3, 4, 6, 7, 8};
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private final PlayerDataManager playerDataManager;

    public EnchantsGui(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public Gui create(Player viewer, ItemStack pickaxe) {
        long tokenBalance = tokenBalance(viewer);

        return Gui.of(6)
                .title(text("Pickaxe Enchantments", NamedTextColor.DARK_PURPLE))
                .component(component -> {

                    final var maximumPreview = component.remember(-1);

                    component.render(container -> {
                        var filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                                .name(Component.empty())
                                .asGuiItem();

                        for (int row = 1; row <= 6; row++) {
                            for (int column = 1; column <= 9; column++) {
                                container.setItem(row, column, filler);
                            }
                        }

                        var accent = ItemBuilder.from(Material.PURPLE_STAINED_GLASS_PANE)
                                .name(Component.empty())
                                .glow()
                                .asGuiItem();

                        for (int column = 2; column <= 8; column++) {
                            container.setItem(1, column, accent);
                            container.setItem(6, column, accent);
                        }

                        PickaxeEnchantment[] enchantments = PickaxeEnchantment.values();
                        for (int index = 0; index < enchantments.length; index++) {
                            PickaxeEnchantment enchantment = enchantments[index];
                            container.setItem(
                                    3,
                                    ENCHANT_COLUMNS[index],
                                    enchantItem(pickaxe, enchantment, tokenBalance, index, maximumPreview)
                            );
                        }

                        container.setItem(5, 4, balanceItem(tokenBalance));
                        container.setItem(5, 5, pickaxeItem(pickaxe));
                        container.setItem(5, 6, closeItem());
                    });

                })
                .build();
    }

    private GuiItem<Player, ItemStack> enchantItem(
            ItemStack pickaxe,
            PickaxeEnchantment enchantment,
            long tokenBalance,
            int index, MutableState<@NotNull Integer> maximumPreview
    ) {
        int currentLevel = enchantment.level(pickaxe);
        boolean maxed = currentLevel >= enchantment.maxLevel();
        boolean previewMaximum = maximumPreview.get() == index;

        int previewLevels = previewMaximum ? levelsToBuy(enchantment,currentLevel,tokenBalance,true) : 1;
        long displayedPrice = maxed || previewLevels == 0 ? 0 : totalPrice(enchantment, currentLevel, previewLevels);
        long nextPrice = maxed ? 0 : enchantment.priceForLevel(currentLevel + 1);
        long sellPrice = enchantment.priceForLevel(currentLevel) / 2;

        List<Component> lore = new ArrayList<>();
        lore.add(text(enchantment.description(), NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(text("Purchase Enchant", NamedTextColor.WHITE)
                .decorate(TextDecoration.BOLD));
        lore.add(line(
                "Current Level: ",
                NUMBER_FORMAT.format(currentLevel) + "/" + NUMBER_FORMAT.format(enchantment.maxLevel())
        ));

        if (maxed) {
            lore.add(text("  MAXIMUM LEVEL REACHED", NamedTextColor.GOLD));
        } else if (previewMaximum) {
            lore.add(line(
                    "Max Price (" + NUMBER_FORMAT.format(previewLevels) + " levels): ",
                    NUMBER_FORMAT.format(displayedPrice) + " tokens"
            ));
        } else {
            lore.add(line(
                    "Next Price: ",
                    NUMBER_FORMAT.format(displayedPrice) + " tokens"
            ));
        }
        if (currentLevel > 0) {
            lore.add(line("Sell Price: ", NUMBER_FORMAT.format(sellPrice) + " tokens"));
        }
        lore.add(line("Token Balance: ", NUMBER_FORMAT.format(tokenBalance)));
        lore.add(Component.empty());

        if (!maxed) {
            lore.add(text("Left-Click", NamedTextColor.GREEN)
                    .append(text(" to purchase one level", NamedTextColor.GRAY)));
            lore.add(text("Shift-Left-Click", NamedTextColor.GREEN)
                    .append(text(" to purchase max levels", NamedTextColor.GRAY)));
        }
        if (currentLevel > 0) {
            lore.add(text("Right-Click", NamedTextColor.RED)
                    .append(text(" to sell one level", NamedTextColor.GRAY)));
        }

        return ItemBuilder.from(enchantment.icon())
                .name(text(enchantment.displayName(), NamedTextColor.LIGHT_PURPLE)
                        .decorate(TextDecoration.BOLD))
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .glow(currentLevel > 0)
                .asGuiItem((player, context) -> handleEnchantClick(
                        player,
                        context,
                        pickaxe,
                        enchantment,
                        index,
                        maximumPreview
                ));
    }

    private GuiItem<Player, ItemStack> balanceItem(long tokenBalance) {
        return ItemBuilder.from(Material.NETHER_STAR)
                .name(text("Your Currency", NamedTextColor.LIGHT_PURPLE)
                        .decorate(TextDecoration.BOLD))
                .lore(List.of(
                        line("Token Balance: ", NUMBER_FORMAT.format(tokenBalance)),
                        Component.empty(),
                        text("Mine blocks to earn more tokens.", NamedTextColor.GRAY)
                ))
                .glow()
                .asGuiItem();
    }

    private dev.triumphteam.gui.element.GuiItem<Player, ItemStack> pickaxeItem(ItemStack pickaxe) {
        return ItemBuilder.from(pickaxe.clone())
                .name(text("Your Pickaxe", NamedTextColor.AQUA)
                        .decorate(TextDecoration.BOLD))
                .lore(List.of(
                        text("All purchased levels are stored", NamedTextColor.GRAY),
                        text("directly on this pickaxe.", NamedTextColor.GRAY)
                ))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem();
    }

    private dev.triumphteam.gui.element.GuiItem<Player, ItemStack> closeItem() {
        return ItemBuilder.from(Material.BARRIER)
                .name(text("Close Menu", NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD))
                .lore(List.of(text("Click to return to mining.", NamedTextColor.GRAY)))
                .asGuiItem((player, context) -> context.guiView().close());
    }

    private void handleEnchantClick(
            Player player,
            ClickContext context,
            ItemStack pickaxe,
            PickaxeEnchantment enchantment,
            int enchantmentIndex,
            MutableState<Integer> maximumPreview
    ) {
        GuiClick click = context.guiClick();

        if (click != GuiClick.LEFT
                && click != GuiClick.SHIFT_LEFT
                && click != GuiClick.RIGHT) return;

        try {
            if (click == GuiClick.RIGHT) {
                sellLevel(player, pickaxe, enchantment);
                return;
            }

            if (click == GuiClick.SHIFT_LEFT
                    && maximumPreview.get() != enchantmentIndex) {
                maximumPreview.update(previous -> enchantmentIndex);
                return;
            }

            purchaseLevels(
                    player,
                    pickaxe,
                    enchantment,
                    click == GuiClick.SHIFT_LEFT
            );
        } catch (IllegalStateException exception) {
            failure(
                    player,
                    "Your player data is still loading. Please try again."
            );
        }
    }

    private void purchaseLevels(
            Player player,
            ItemStack pickaxe,
            PickaxeEnchantment enchantment,
            boolean purchaseMaximum
    ) {
        int currentLevel = enchantment.level(pickaxe);
        if (currentLevel >= enchantment.maxLevel()) {
            failure(player, enchantment.displayName() + " is already maxed.");
            return;
        }

        long balance = playerDataManager.getTokens(player.getUniqueId());
        int levelsToBuy = levelsToBuy(
                enchantment,
                currentLevel,
                balance,
                purchaseMaximum
        );

        if (levelsToBuy == 0) {
            failure(player, "You do not have enough tokens for this level.");
            return;
        }

        long price = totalPrice(enchantment, currentLevel, levelsToBuy);
        playerDataManager.removeTokens(player.getUniqueId(), price);
        enchantment.setLevel(pickaxe, currentLevel + levelsToBuy);

        player.sendMessage(
                text("Purchased ", NamedTextColor.GRAY)
                        .append(text(NUMBER_FORMAT.format(levelsToBuy), NamedTextColor.GREEN))
                        .append(text(levelsToBuy == 1 ? " level of " : " levels of ", NamedTextColor.GRAY))
                        .append(text(enchantment.displayName(), NamedTextColor.LIGHT_PURPLE))
                        .append(text(" for " + NUMBER_FORMAT.format(price) + " tokens.", NamedTextColor.GRAY))
        );
        finishTransaction(player, pickaxe);
    }

    private void sellLevel(
            Player player,
            ItemStack pickaxe,
            PickaxeEnchantment enchantment
    ) {
        int currentLevel = enchantment.level(pickaxe);
        if (currentLevel == 0) {
            failure(player, "You do not have any levels to sell.");
            return;
        }

        long sellPrice = enchantment.priceForLevel(currentLevel) / 2;
        playerDataManager.addTokens(player.getUniqueId(), sellPrice);
        enchantment.setLevel(pickaxe, currentLevel - 1);

        player.sendMessage(
                text("Sold ", NamedTextColor.GRAY)
                        .append(text("1", NamedTextColor.GREEN))
                        .append(text(" level of ", NamedTextColor.GRAY))
                        .append(text(enchantment.displayName(), NamedTextColor.LIGHT_PURPLE))
                        .append(text(" for " + NUMBER_FORMAT.format(sellPrice) + " tokens.", NamedTextColor.GRAY))
        );
        finishTransaction(player, pickaxe);
    }

    private void finishTransaction(Player player, ItemStack pickaxe) {
        player.playSound(
                player.getLocation(),
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                0.8f,
                1.35f
        );
        player.updateInventory();
        create(player, pickaxe).open(player);
    }

    private int levelsToBuy(
            PickaxeEnchantment enchantment,
            int currentLevel,
            long balance,
            boolean purchaseMaximum
    ) {
        int levels = 0;
        long runningPrice = 0;

        for (int level = currentLevel + 1; level <= enchantment.maxLevel(); level++) {
            long nextPrice = enchantment.priceForLevel(level);
            if (nextPrice > balance - runningPrice) {
                break;
            }
            runningPrice += nextPrice;
            levels++;

            if (!purchaseMaximum) {
                break;
            }
        }

        return levels;
    }

    private long totalPrice(
            PickaxeEnchantment enchantment,
            int currentLevel,
            int levelsToBuy
    ) {
        long total = 0;
        for (int level = currentLevel + 1; level <= currentLevel + levelsToBuy; level++) {
            total = Math.addExact(total, enchantment.priceForLevel(level));
        }
        return total;
    }

    private long tokenBalance(Player player) {
        try {
            return playerDataManager.getTokens(player.getUniqueId());
        } catch (IllegalStateException exception) {
            return 0;
        }
    }

    private void failure(Player player, String message) {
        player.sendMessage(text(message, NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.7f);
    }

    private static Component line(String label, String value) {
        return text("  • " + label, NamedTextColor.LIGHT_PURPLE)
                .append(text(value, NamedTextColor.WHITE));
    }

    private static Component text(String value, NamedTextColor color) {
        return Component.text(value, color)
                .decoration(TextDecoration.ITALIC, false);
    }
}
