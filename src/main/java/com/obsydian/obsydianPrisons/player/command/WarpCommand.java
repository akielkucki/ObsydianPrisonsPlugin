package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.player.WarpCache;
import com.obsydian.obsydianprisons.player.models.Warp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class WarpCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) return true;
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /warp <name>", TextColor.color(0xFF0000)));
            return true;
        }
        String warpName = args[0].toLowerCase(Locale.ROOT);
        if (!player.hasPermission("obsydianprisons.warp.*")
                && !player.hasPermission("obsydianprisons.warp." + warpName)) {
            player.sendMessage(Component.text("You do not have permission to use this warp.", TextColor.color(0xFF0000)));
            return true;
        }

        Warp warp = WarpCache.instance.getWarp(warpName);
        if (warp == null) {
            player.sendMessage(Component.text("That warp does not exist.", TextColor.color(0xFF0000)));
            return true;
        }

        try {
            if (!player.teleport(warp.getLocation())) {
                player.sendMessage(Component.text("Could not teleport to that warp.", TextColor.color(0xFF0000)));
                return true;
            }
            player.sendMessage(Component.text("Teleported to " + warpName + ".", TextColor.color(0x00FF00)));
        } catch (IllegalStateException exception) {
            player.sendMessage(Component.text("That warp's world is not loaded.", TextColor.color(0xFF0000)));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            return WarpCache.instance.getWarpNames().stream()
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
