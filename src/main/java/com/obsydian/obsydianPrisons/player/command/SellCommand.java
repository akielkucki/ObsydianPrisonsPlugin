package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.economy.Vault;
import com.obsydian.obsydianprisons.selling.SellConfig;
import com.obsydian.obsydianprisons.selling.SellService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellCommand implements CommandExecutor {
    Map<UUID, Long> lastSoldTimestamp = new HashMap<>();
    private final ObsydianPrisons plugin;
    public SellCommand(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
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
        SellService sellService = plugin.getSellService();
        UUID playerId = player.getUniqueId();
        SellService.SaleResult sale = sellService.sellInventory(player);
        double multiplier = plugin.getPlayerDataManager()
                .getPlayerData(playerId)
                .getMultiplier();
        if (!Double.isFinite(multiplier) || multiplier <= 0) {
            multiplier = 1.0;
        }
        player.sendMessage(Component.empty()
                .append(Component.text("───────── ", NamedTextColor.DARK_GRAY))
                .append(Component.text("SELL SUMMARY", NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" ─────────", NamedTextColor.DARK_GRAY)));

        player.sendMessage(Component.empty()
                .append(Component.text("  Items sold: ", NamedTextColor.GRAY))
                .append(Component.text(
                        String.format("%,d", sale.items()),
                        NamedTextColor.WHITE
                )));

        player.sendMessage(Component.empty()
                .append(Component.text("  Money earned: ", NamedTextColor.GRAY))
                .append(Component.text(
                        String.format("$%,.2f", sale.value()),
                        NamedTextColor.GREEN
                ).decorate(TextDecoration.BOLD)));

        player.sendMessage(Component.empty()
                .append(Component.text("  Sell multiplier: ", NamedTextColor.GRAY))
                .append(Component.text(
                        String.format("%.2fx", multiplier),
                        NamedTextColor.GOLD
                ).decorate(TextDecoration.BOLD)));

        player.sendMessage(Component.text(
                "────────────────────────────",
                NamedTextColor.DARK_GRAY
        ));
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);


        lastSoldTimestamp.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }
}
