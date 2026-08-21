package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.player.WarpCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DeleteWarpCommand implements CommandExecutor {
    private final ObsydianPrisons plugin;
    public DeleteWarpCommand(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!commandSender.hasPermission("obsydianprisons.delwarp")) {
            commandSender.sendMessage(Component.text("You do not have permission to use this command.", TextColor.color(0xFF0000)));
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(Component.text("Usage: /delwarp <name>", TextColor.color(0xFF0000)));
            return true;
        }

        String warpName = args[0].toLowerCase(Locale.ROOT);
        if (!WarpCache.instance.contains(warpName)) {
            commandSender.sendMessage(Component.text("That warp does not exist.", TextColor.color(0xFF0000)));
            return true;
        }
        plugin.getDatabaseManager().deleteWarp(warpName).whenComplete((ignored, error) ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (error != null) {
                        plugin.getLogger().warning("Could not delete warp " + warpName + ": " + error.getMessage());
                        commandSender.sendMessage(Component.text("Could not delete that warp.", TextColor.color(0xFF0000)));
                        return;
                    }
                    commandSender.sendMessage(Component.text("Deleted warp " + warpName + ".", TextColor.color(0x00FF00)));
                })
        );
        return true;
    }
}
