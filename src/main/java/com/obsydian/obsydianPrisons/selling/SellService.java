package com.obsydian.obsydianprisons.selling;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.economy.Vault;
import com.obsydian.obsydianprisons.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public final class SellService {
    private final ObsydianPrisons plugin;
    private final SellConfig sellConfig;

    public SellService(ObsydianPrisons plugin, SellConfig sellConfig) {
        this.plugin = plugin;
        this.sellConfig = sellConfig;
    }

    // For buffered autosell drops that never entered the inventory.
    public SaleResult sell(UUID playerId, Collection<ItemStack> items) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(playerId);
        double multiplier = playerData.getMultiplier();
        if (!Double.isFinite(multiplier) || multiplier <= 0) multiplier = 1;
        SaleResult sale = calculateSale(items,multiplier);
        if (sale.value().signum() > 0)
            Vault.getEconomy().deposit(plugin.getName(), playerId, sale.value());
        return sale;
    }

    // For /sell: sells and removes exact player-storage slots.
    public SaleResult sellInventory(Player player) {
        ItemStack[] storage = player.getInventory().getStorageContents();
        SaleResult sale = sell(player.getUniqueId(), Arrays.asList(storage));

        if (sale.value().signum() > 0) {
            for (int slot = 0; slot < storage.length; slot++) {
                ItemStack item = storage[slot];
                if (item != null && !item.getType().isAir() &&
                        sellConfig.canSell(item.getType())) storage[slot] = null;
            }

            player.getInventory().setStorageContents(storage);
        }

        return sale;
    }

    private SaleResult calculateSale(Collection<ItemStack> items, double multiplier) {
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;

        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;

            double unitPrice = sellConfig.getWorth(item.getType());
            if (unitPrice <= 0) continue;

            total = total.add(BigDecimal.valueOf(unitPrice)
                    .multiply(BigDecimal.valueOf(item.getAmount())));
            itemCount += item.getAmount();
        }

        return new SaleResult(
                itemCount,
                total.multiply(BigDecimal.valueOf(multiplier))
        );
    }

    public record SaleResult(int items, BigDecimal value) {

    }
}
