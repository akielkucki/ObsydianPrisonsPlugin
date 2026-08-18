package com.obsydian.obsydianprisons.player.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnderChestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (!commandSender.hasPermission("obsydianprisons.enderchest")) {
            commandSender.sendMessage(Component.text("You do not have permission to use this command.", TextColor.color(0xFF0000)));
            return true;
        }
        player.sendMessage(Component.text("Opening your Ender Chest...", TextColor.color(0x00FF00)));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        player.openInventory(player.getEnderChest());
        return true;
    }
}
