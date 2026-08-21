package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.player.WarpCache;
import com.obsydian.obsydianprisons.player.models.Warp;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SetWarpCommand implements CommandExecutor {
    private final ObsydianPrisons plugin;
    public SetWarpCommand(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("obsydianprisons.setwarp")) {
            player.sendMessage(Component.text("You do not have permission to use this command.", TextColor.color(0xFF0000)));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /setwarp <name>", TextColor.color(0xFF0000)));
            return true;
        }

        String warpName = args[0].toLowerCase(Locale.ROOT);
        if (!warpName.matches("[a-z0-9_-]{1,32}")) {
            player.sendMessage(Component.text("Warp names may only contain letters, numbers, underscores and hyphens.", TextColor.color(0xFF0000)));
            return true;
        }
        if (WarpCache.instance.contains(warpName)) {
            player.sendMessage(Component.text("A warp with that name already exists.", TextColor.color(0xFF0000)));
            return true;
        }

        Location location = player.getLocation();
        Warp warp = new Warp(
                warpName,
                location.getWorld().getUID().toString(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );

        plugin.getDatabaseManager().createWarp(warp).whenComplete((ignored, error) ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (error != null) {
                        plugin.getLogger().warning("Could not create warp " + warpName + ": " + error.getMessage());
                        player.sendMessage(Component.text("Could not create that warp.", TextColor.color(0xFF0000)));
                        return;
                    }
                    player.sendMessage(Component.text("Created warp " + warpName + ".", TextColor.color(0x00FF00)));
                })
        );
        return true;
    }
}
