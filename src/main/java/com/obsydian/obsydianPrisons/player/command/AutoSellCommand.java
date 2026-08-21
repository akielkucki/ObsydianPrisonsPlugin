package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.player.models.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AutoSellCommand implements CommandExecutor {
    private final ObsydianPrisons plugin;
    public AutoSellCommand(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (!commandSender.hasPermission("obsydianprisons.autosell")) {
            var msg = Component.text("You do not have permission to use this command.", TextColor.color(0xFF0000));
            player.sendMessage(msg);
            return true;
        }
        boolean enabled = plugin.getAutoSellService().toggle(player);
        player.sendMessage(Component.text(
                "AutoSell has been " + (enabled ? "enabled." : "disabled."),
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED
        ));
        var data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Settings settings = data.getSettings();
        settings.setAutoSellEnabled(enabled);
        plugin.getPlayerDataManager().applySettings(player.getUniqueId(), settings);

        plugin.getDatabaseManager().updatePlayer(data)
                .exceptionally(error -> {
                    plugin.getLogger().severe("Could not save settings for " + player.getName() + ": " + error.getMessage());
                    return null;
                });
        return true;
    }
}
