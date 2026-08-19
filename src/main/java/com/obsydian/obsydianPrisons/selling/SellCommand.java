package com.obsydian.obsydianprisons.selling;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.economy.Vault;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellCommand implements CommandExecutor {
    Map<UUID, Long> lastSoldTimestamp = new HashMap<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) return false;
        if (lastSoldTimestamp.containsKey(player.getUniqueId()) && System.currentTimeMillis() - lastSoldTimestamp.get(player.getUniqueId()) < 10000) {
            long timeLeft = 10000- (System.currentTimeMillis() - lastSoldTimestamp.get(player.getUniqueId()));
            player.sendMessage(Component.text(
                    "────────────────────────────",
                    NamedTextColor.DARK_GRAY
            ));

            player.sendMessage(Component.empty()
                    .append(Component.text("⌛ ", NamedTextColor.GOLD))
                    .append(Component.text("SELL COOLDOWN", NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD)));

            player.sendMessage(Component.empty()
                    .append(Component.text("You can sell again in ", NamedTextColor.GRAY))
                    .append(Component.text(timeLeft/1000+"s", NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD))
                    .append(Component.text(".", NamedTextColor.GRAY)));

            player.sendMessage(Component.empty()
                    .append(Component.text("Want to skip the cooldown? ", NamedTextColor.GRAY))
                    .append(Component.text("Get AutoSell", TextColor.color(0xFF00AE))
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.openUrl("https://obsydian.gg/store"))
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Click to visit the store", NamedTextColor.GREEN)
                            ))));

            player.sendMessage(Component.text(
                    "────────────────────────────",
                    NamedTextColor.DARK_GRAY
            ));
            return true;
        }
        Inventory inv = player.getInventory();

        ObsydianPrisons plugin = ObsydianPrisons.getInstance();
        SellConfig configManager = plugin.getConfigManager();

        double runningTotal = 0;
        int totalSoldItems = 0;

        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);

            if (item == null || item.getType().isAir()) continue;
            if (!configManager.canSell(item.getType())) continue;

            runningTotal += configManager.getWorth(item);
            totalSoldItems += item.getAmount();

            inv.setItem(slot, null);
        }
        if (totalSoldItems == 0) {
            player.sendMessage(Component.text("You have nothing to sell!", NamedTextColor.RED));
            return true;
        }
        player.sendMessage(Component.empty()
                .append(Component.text("───────── ", NamedTextColor.DARK_GRAY))
                .append(Component.text("SELL SUMMARY", NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" ─────────", NamedTextColor.DARK_GRAY)));

        player.sendMessage(Component.empty()
                .append(Component.text("  Items sold: ", NamedTextColor.GRAY))
                .append(Component.text(
                        String.format("%,d", totalSoldItems),
                        NamedTextColor.WHITE
                )));

        player.sendMessage(Component.empty()
                .append(Component.text("  Money earned: ", NamedTextColor.GRAY))
                .append(Component.text(
                        String.format("$%,.2f", runningTotal),
                        NamedTextColor.GREEN
                ).decorate(TextDecoration.BOLD)));

        player.sendMessage(Component.text(
                "────────────────────────────",
                NamedTextColor.DARK_GRAY
        ));

        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        Vault.getEconomy().deposit(plugin.getName(), player.getUniqueId(), new BigDecimal(runningTotal));

        lastSoldTimestamp.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }
}
