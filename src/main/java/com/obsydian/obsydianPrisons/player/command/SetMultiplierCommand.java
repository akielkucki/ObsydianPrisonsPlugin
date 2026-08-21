package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetMultiplierCommand implements CommandExecutor {
    private final ObsydianPrisons plugin;
    public SetMultiplierCommand(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!commandSender.hasPermission("obsydianprisons.admin.setmultiplier")) {
            commandSender.sendMessage(Component.text("You do not have permission to use this command.", TextColor.color(0xFF0000)));
            return true;
        }
        if (strings.length != 2) {
            commandSender.sendMessage(Component.text("Usage: /setmultiplier <[float]multiplier> <player>", TextColor.color(0xFF0000)));
            return true;
        }
        try {
            double multiplier = Double.parseDouble(strings[0]);
            String playerName = strings[1];
            Player player = commandSender.getServer().getPlayer(playerName);
            if (player == null) {
                commandSender.sendMessage(Component.text("Player not found.", TextColor.color(0xFF0000)));
                return true;
            }
            if (multiplier < 0) {
                commandSender.sendMessage(Component.text("Multiplier must be greater than or equal to 0.", TextColor.color(0xFF0000)));
            }
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            data.setMultiplier(multiplier);

            plugin.getDatabaseManager().updatePlayer(data).whenComplete((ignored, error) -> {
                if (error != null) {
                    plugin.getLogger().warning("Could not update multiplier for " + player.getName() + ": " + error.getMessage());
                    commandSender.sendMessage(Component.text("Could not update multiplier for " + player.getName() + ".", TextColor.color(0xFF0000)));
                    return;
                }
                commandSender.sendMessage(Component.text("Updated multiplier for " + player.getName() + ".", TextColor.color(0x00FF00)));
            });

        } catch (NumberFormatException exception) {
            commandSender.sendMessage(Component.text("Multiplier must be a number.", TextColor.color(0xFF0000)));
            return true;
        }
        return false;
    }
}
