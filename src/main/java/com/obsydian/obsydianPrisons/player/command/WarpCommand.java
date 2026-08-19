package com.obsydian.obsydianprisons.player.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) return true;
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /warp <name>", TextColor.color(0xFF0000)));
            return true;
        }
        String warpName = args[0];
        if (!player.hasPermission("obsydianprisons.warp." + warpName)) {
            player.sendMessage(Component.text("You do not have permission to use this warp.", TextColor.color(0xFF0000)));
            return true;
        }

        return true;
    }
}
