package com.obsydian.obsydianprisons.pickaxe.command;

import com.obsydian.obsydianprisons.pickaxe.PickaxeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) return false;
        PickaxeUtils.createPickaxeItem(player);
        return true;
    }
}
