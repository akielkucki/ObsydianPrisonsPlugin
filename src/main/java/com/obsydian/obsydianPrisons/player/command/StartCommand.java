package com.obsydian.obsydianprisons.player.command;

import com.obsydian.obsydianprisons.pickaxe.PickaxeKeys;
import com.obsydian.obsydianprisons.pickaxe.PickaxeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Command can only be used by players.");
            return true;
        }
        var pdc = player.getPersistentDataContainer();
        if (pdc.has(PickaxeKeys.TIMESTAMP, PersistentDataType.LONG)) {
            if (System.currentTimeMillis() < pdc.get(PickaxeKeys.TIMESTAMP, PersistentDataType.LONG)) {
                player.sendMessage(Component.text(String.format(
                        "You already have a pickaxe, you may use this command again on %tF", pdc.get(PickaxeKeys.TIMESTAMP, PersistentDataType.LONG)
                ), TextColor.color(0xFF0000)));
                return true;
            }
        }
        pdc.set(PickaxeKeys.TIMESTAMP, PersistentDataType.LONG, System.currentTimeMillis() + 86400000 * 3);
        var pick = PickaxeUtils.createPickaxeItem(player);
        player.getInventory().addItem(pick);

        return true;
    }
}
