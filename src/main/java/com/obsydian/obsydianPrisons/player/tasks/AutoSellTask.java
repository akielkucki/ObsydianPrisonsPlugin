package com.obsydian.obsydianprisons.player.tasks;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.selling.AutoSellService;
import com.obsydian.obsydianprisons.selling.SellService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

public class AutoSellTask implements Runnable {
    private final AutoSellService autoSellService;
    private final SellService sellService;
    private final Player player;
    private ObsydianPrisons plugin;
    
    public AutoSellTask(AutoSellService autoSellService, SellService sellService, Player player) {
        this.autoSellService = autoSellService;
        this.sellService = sellService;
        this.player = player;
        this.plugin = ObsydianPrisons.getInstance();
    }
    @Override
    public void run() {
        SellService.SaleResult sale = sellService.sell(
                player.getUniqueId(),
                Arrays.asList(autoSellService.getItemsToSell(player.getUniqueId()))
        );
        if (sale.items() > 0) {
            player.sendActionBar(Component.text(
                    String.format("AutoSold %d items for $%s with a %.2fx multiplier", sale.items(), sale.value(), plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getMultiplier()),
                    NamedTextColor.GREEN
            ));
            player.sendMessage(Component.text("\n", NamedTextColor.GREEN)
                    .append(Component.text(
                            String.format("You have AutoSold %d items for $%s with a %.2fx multiplier!", sale.items(), NumberFormat.getInstance().format(sale.value()), plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getMultiplier()),
                            NamedTextColor.GREEN
                    ))
                    .append(Component.text("\n", NamedTextColor.GREEN))
            );
            autoSellService.clearItems(player.getUniqueId());
        }
    }
}
