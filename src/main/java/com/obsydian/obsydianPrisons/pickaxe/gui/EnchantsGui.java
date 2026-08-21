package com.obsydian.obsydianprisons.pickaxe.gui;

import com.obsydian.obsydianprisons.pickaxe.PickaxeEnchantment;
import com.obsydian.obsydianprisons.pickaxe.PickaxeUtils;
import com.obsydian.obsydianprisons.player.PlayerDataManager;
import dev.triumphteam.gui.click.ClickContext;
import dev.triumphteam.gui.click.GuiClick;
import dev.triumphteam.gui.container.GuiContainer;
import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import dev.triumphteam.nova.MutableState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class EnchantsGui {
    private static final int GUI_ROWS = 6, ENCHANT_ROW = 3, CONTROL_ROW = 5, NO_MAXIMUM_PREVIEW = -1;
    private static final int[] ENCHANT_COLUMNS = {2, 3, 4, 6, 7, 8};
    private static final long COLOR_CHANGE_COOLDOWN_MILLIS = 2_000L;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private final PlayerDataManager playerDataManager;
    private final Map<UUID, Long> colorChangeCooldowns = new HashMap<>();

    public EnchantsGui(PlayerDataManager playerDataManager) { this.playerDataManager = playerDataManager; }

    public Gui create(Player viewer, ItemStack pickaxe) {
        return Gui.of(GUI_ROWS).title(text("Pickaxe Enchantments", NamedTextColor.DARK_PURPLE))
                .component(component -> {
                    MutableState<MenuState> menuState = component.remember(new MenuState(loadTokenBalance(viewer), NO_MAXIMUM_PREVIEW));
                    component.render(container -> renderMenu(container, pickaxe, menuState));
                }).build();
    }

    private void renderMenu(GuiContainer<Player, ItemStack> container, ItemStack pickaxe, MutableState<MenuState> menuState) {
        MenuState state = menuState.get();
        fillBackground(container);
        addAccents(container);
        addEnchantments(container, pickaxe, state, menuState);
        addControls(container, pickaxe, state.tokenBalance());
    }

    private void fillBackground(GuiContainer<Player, ItemStack> container) {
        GuiItem<Player, ItemStack> filler = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name(Component.empty()).asGuiItem();
        for (int row = 1; row <= GUI_ROWS; row++) for (int column = 1; column <= 9; column++) container.setItem(row, column, filler);
    }

    private void addAccents(GuiContainer<Player, ItemStack> container) {
        GuiItem<Player, ItemStack> accent = ItemBuilder.from(Material.PURPLE_STAINED_GLASS_PANE).name(Component.empty()).glow().asGuiItem();
        for (int column = 2; column <= 8; column++) {
            container.setItem(1, column, accent);
            container.setItem(GUI_ROWS, column, accent);
        }
    }

    private void addEnchantments(GuiContainer<Player, ItemStack> container, ItemStack pickaxe, MenuState state,
                                 MutableState<MenuState> menuState) {
        PickaxeEnchantment[] enchantments = PickaxeEnchantment.values();
        if (enchantments.length > ENCHANT_COLUMNS.length) throw new IllegalStateException("Not enough GUI slots for every pickaxe enchantment");
        for (int index = 0; index < enchantments.length; index++)
            container.setItem(ENCHANT_ROW, ENCHANT_COLUMNS[index], enchantItem(pickaxe, enchantments[index], index, state, menuState));
    }

    private void addControls(GuiContainer<Player, ItemStack> container, ItemStack pickaxe, long tokenBalance) {
        container.setItem(CONTROL_ROW, 4, balanceItem(tokenBalance));
        container.setItem(CONTROL_ROW, 5, pickaxeItem(pickaxe));
        container.setItem(CONTROL_ROW, 6, closeItem());
    }

    private GuiItem<Player, ItemStack> enchantItem(ItemStack pickaxe, PickaxeEnchantment enchantment, int enchantmentIndex,
                                                   MenuState state, MutableState<MenuState> menuState) {
        EnchantmentView view = createEnchantmentView(pickaxe, enchantment, enchantmentIndex, state);
        return ItemBuilder.from(enchantment.icon()).name(text(enchantment.displayName(), NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .lore(enchantmentLore(enchantment, view, state.tokenBalance())).flags(ItemFlag.HIDE_ATTRIBUTES)
                .glow(view.currentLevel() > 0).asGuiItem((player, context) ->
                        handleEnchantClick(player, context, pickaxe, enchantment, enchantmentIndex, menuState));
    }

    private EnchantmentView createEnchantmentView(ItemStack pickaxe, PickaxeEnchantment enchantment, int enchantmentIndex, MenuState state) {
        int currentLevel = enchantment.level(pickaxe), previewLevels;
        boolean maxed = currentLevel >= enchantment.maxLevel(), previewingMaximum = state.isPreviewing(enchantmentIndex);
        previewLevels = previewingMaximum ? levelsToBuy(enchantment, currentLevel, state.tokenBalance(), true) : 1;
        long displayedPrice = maxed || previewLevels == 0 ? 0 : totalPrice(enchantment, currentLevel, previewLevels);
        long sellPrice = currentLevel == 0 ? 0 : enchantment.priceForLevel(currentLevel) / 2;
        return new EnchantmentView(currentLevel, maxed, previewingMaximum, previewLevels, displayedPrice, sellPrice);
    }

    private List<Component> enchantmentLore(PickaxeEnchantment enchantment, EnchantmentView view, long tokenBalance) {
        List<Component> lore = new ArrayList<>();
        lore.add(text(enchantment.description(), NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, true));
        lore.add(Component.empty());
        lore.add(text("Purchase Enchant", NamedTextColor.WHITE).decorate(TextDecoration.BOLD));
        lore.add(line("Current Level: ", format(view.currentLevel()) + "/" + format(enchantment.maxLevel())));
        addPriceLore(lore, view);
        lore.add(line("Gems Balance: ", format(tokenBalance)));
        lore.add(Component.empty());
        addActionLore(lore, view);
        return lore;
    }

    private void addPriceLore(List<Component> lore, EnchantmentView view) {
        if (view.maxed()) lore.add(text("  MAXIMUM LEVEL REACHED", NamedTextColor.GOLD));
        else if (view.previewingMaximum() && view.previewLevels() == 0) lore.add(text("  NOT ENOUGH GEMS", NamedTextColor.RED));
        else if (view.previewingMaximum())
            lore.add(line("Max Price (" + format(view.previewLevels()) + " levels): ", format(view.displayedPrice()) + " gems"));
        else lore.add(line("Next Price: ", format(view.displayedPrice()) + " gems"));
        if (view.currentLevel() > 0) lore.add(line("Sell Price: ", format(view.sellPrice()) + " gems"));
    }

    private void addActionLore(List<Component> lore, EnchantmentView view) {
        if (!view.maxed()) {
            lore.add(text("Left-Click", NamedTextColor.GREEN).append(text(" to purchase one level", NamedTextColor.GRAY)));
            lore.add(text("Shift-Left-Click", NamedTextColor.GREEN).append(text(" to preview/purchase max levels", NamedTextColor.GRAY)));
        }
        if (view.currentLevel() > 0)
            lore.add(text("Right-Click", NamedTextColor.RED).append(text(" to sell one level", NamedTextColor.GRAY)));
    }

    private GuiItem<Player, ItemStack> balanceItem(long tokenBalance) {
        return ItemBuilder.from(Material.NETHER_STAR).name(text("Your Currency", NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .lore(List.of(line("Gems Balance: ", format(tokenBalance)), Component.empty(),
                        text("Mine blocks to earn more gems.", NamedTextColor.GRAY))).glow().asGuiItem();
    }

    private GuiItem<Player, ItemStack> pickaxeItem(ItemStack pickaxe) {
        return ItemBuilder.from(pickaxe.clone()).name(text("Your Pickaxe", NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                .lore(List.of(text("All purchased levels are stored", NamedTextColor.GRAY),
                        text("directly on this pickaxe.", NamedTextColor.GRAY))).flags(ItemFlag.HIDE_ATTRIBUTES)
                .asGuiItem((player, context) -> changePickaxeColor(player, pickaxe));
    }

    private void changePickaxeColor(Player player, ItemStack pickaxe) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (colorChangeCooldowns.getOrDefault(playerId, 0L) > now) {
            player.sendMessage(text("Woah there, slow down buckaroo.", TextColor.color(0xFF3B30)));
            return;
        }
        int colorValue = ThreadLocalRandom.current().nextInt(0x1000000);
        TextColor color = TextColor.color(colorValue);
        ItemStack updatedPickaxe = pickaxe.clone();
        var meta = updatedPickaxe.getItemMeta();
        meta.displayName(Component.text(player.getName() + "'s Pickaxe", color).decoration(TextDecoration.ITALIC, false));
        updatedPickaxe.setItemMeta(meta);
        pickaxe.copyDataFrom(updatedPickaxe, dataComponentType -> true);
        player.sendMessage(text("Set your current pickaxe color to " + String.format("#%06X", colorValue), color));
        colorChangeCooldowns.put(playerId, now + COLOR_CHANGE_COOLDOWN_MILLIS);
    }

    private GuiItem<Player, ItemStack> closeItem() {
        return ItemBuilder.from(Material.BARRIER).name(text("Close Menu", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .lore(List.of(text("Click to return to mining.", NamedTextColor.GRAY)))
                .asGuiItem((player, context) -> context.guiView().close());
    }

    private void handleEnchantClick(Player player, ClickContext context, ItemStack pickaxe, PickaxeEnchantment enchantment,
                                    int enchantmentIndex, MutableState<MenuState> menuState) {
        GuiClick click = context.guiClick();
        if (!isTransactionClick(click)) return;
        try {
            if (click == GuiClick.RIGHT) {
                sellLevel(player, pickaxe, enchantment).ifPresent(balance -> finishTransaction(player, pickaxe, menuState, balance));
                return;
            }
            if (shouldPreviewMaximum(click, enchantmentIndex, menuState.get())) {
                menuState.update(state -> state.withMaximumPreview(enchantmentIndex));
                return;
            }
            purchaseLevels(player, pickaxe, enchantment, click == GuiClick.SHIFT_LEFT)
                    .ifPresent(balance -> finishTransaction(player, pickaxe, menuState, balance));
        } catch (IllegalStateException exception) {
            failure(player, "Your player data is still loading. Please try again.");
        }
    }

    private boolean isTransactionClick(GuiClick click) {
        return click == GuiClick.LEFT || click == GuiClick.SHIFT_LEFT || click == GuiClick.RIGHT;
    }

    private boolean shouldPreviewMaximum(GuiClick click, int enchantmentIndex, MenuState state) {
        return click == GuiClick.SHIFT_LEFT && !state.isPreviewing(enchantmentIndex);
    }

    private OptionalLong purchaseLevels(Player player, ItemStack pickaxe, PickaxeEnchantment enchantment, boolean purchaseMaximum) {
        int currentLevel = enchantment.level(pickaxe);
        if (currentLevel >= enchantment.maxLevel()) {
            failure(player, enchantment.displayName() + " is already maxed.");
            return OptionalLong.empty();
        }
        long balance = playerDataManager.getTokens(player.getUniqueId());
        int levelsToPurchase = levelsToBuy(enchantment, currentLevel, balance, purchaseMaximum);
        if (levelsToPurchase == 0) {
            failure(player, "You do not have enough gems for this level.");
            return OptionalLong.empty();
        }
        long price = totalPrice(enchantment, currentLevel, levelsToPurchase);
        long updatedBalance = playerDataManager.removeTokens(player.getUniqueId(), price);
        enchantment.setLevel(pickaxe, currentLevel + levelsToPurchase);
        sendPurchaseMessage(player, enchantment, levelsToPurchase, price);
        return OptionalLong.of(updatedBalance);
    }

    private void sendPurchaseMessage(Player player, PickaxeEnchantment enchantment, int levelsPurchased, long price) {
        player.sendMessage(text("Purchased ", NamedTextColor.GRAY).append(text(format(levelsPurchased), NamedTextColor.GREEN))
                .append(text(levelsPurchased == 1 ? " level of " : " levels of ", NamedTextColor.GRAY))
                .append(text(enchantment.displayName(), NamedTextColor.LIGHT_PURPLE))
                .append(text(" for " + format(price) + " gems.", NamedTextColor.GRAY)));
    }

    private OptionalLong sellLevel(Player player, ItemStack pickaxe, PickaxeEnchantment enchantment) {
        int currentLevel = enchantment.level(pickaxe);
        if (currentLevel == 0) {
            failure(player, "You do not have any levels to sell.");
            return OptionalLong.empty();
        }
        long sellPrice = enchantment.priceForLevel(currentLevel) / 2;
        long updatedBalance = playerDataManager.addTokens(player.getUniqueId(), sellPrice);
        enchantment.setLevel(pickaxe, currentLevel - 1);
        sendSaleMessage(player, enchantment, sellPrice);
        return OptionalLong.of(updatedBalance);
    }

    private void sendSaleMessage(Player player, PickaxeEnchantment enchantment, long sellPrice) {
        player.sendMessage(text("Sold ", NamedTextColor.GRAY).append(text("1", NamedTextColor.GREEN))
                .append(text(" level of ", NamedTextColor.GRAY)).append(text(enchantment.displayName(), NamedTextColor.LIGHT_PURPLE))
                .append(text(" for " + format(sellPrice) + " gems.", NamedTextColor.GRAY)));
    }

    private void finishTransaction(Player player, ItemStack pickaxe, MutableState<MenuState> menuState, long updatedBalance) {
        PickaxeUtils.updatePickaxeItem(pickaxe);
        menuState.update(state -> state.afterTransaction(updatedBalance));
        player.updateInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.35f);
    }

    private int levelsToBuy(PickaxeEnchantment enchantment, int currentLevel, long balance, boolean purchaseMaximum) {
        int levels = 0;
        long runningPrice = 0;
        for (int level = currentLevel + 1; level <= enchantment.maxLevel(); level++) {
            long nextPrice = enchantment.priceForLevel(level);
            if (nextPrice > balance - runningPrice) break;
            runningPrice += nextPrice;
            levels++;
            if (!purchaseMaximum) break;
        }
        return levels;
    }

    private long totalPrice(PickaxeEnchantment enchantment, int currentLevel, int levelsToBuy) {
        long total = 0;
        for (int level = currentLevel + 1; level <= currentLevel + levelsToBuy; level++)
            total = Math.addExact(total, enchantment.priceForLevel(level));
        return total;
    }

    private long loadTokenBalance(Player player) {
        try { return playerDataManager.getTokens(player.getUniqueId()); }
        catch (IllegalStateException exception) { return 0; }
    }

    private void failure(Player player, String message) {
        player.sendMessage(text(message, NamedTextColor.RED));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.7f);
    }

    private static String format(long value) { return NUMBER_FORMAT.format(value); }

    private static Component line(String label, String value) {
        return text("  • " + label, NamedTextColor.LIGHT_PURPLE).append(text(value, NamedTextColor.WHITE));
    }

    private static Component text(String value, NamedTextColor color) {
        return Component.text(value, color).decoration(TextDecoration.ITALIC, false);
    }

    private static Component text(String value, TextColor color) {
        return Component.text(value, color).decoration(TextDecoration.ITALIC, false);
    }

    private record MenuState(long tokenBalance, int maximumPreviewIndex) {
        private boolean isPreviewing(int enchantmentIndex) { return maximumPreviewIndex == enchantmentIndex; }
        private MenuState withMaximumPreview(int enchantmentIndex) { return new MenuState(tokenBalance, enchantmentIndex); }
        private MenuState afterTransaction(long updatedBalance) { return new MenuState(updatedBalance, NO_MAXIMUM_PREVIEW); }
    }

    private record EnchantmentView(int currentLevel, boolean maxed, boolean previewingMaximum, int previewLevels,
                                   long displayedPrice, long sellPrice) {}
}
