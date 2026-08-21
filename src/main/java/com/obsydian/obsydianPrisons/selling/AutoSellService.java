package com.obsydian.obsydianprisons.selling;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.player.tasks.AutoSellTask;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class AutoSellService {
    private final Set<UUID> enabledPlayers = new HashSet<>();
    private final Map<UUID, Integer> runningTasks = new HashMap<>();
    private final Map<UUID, ItemStack[]> playerItemsToSell = new HashMap<>();
    private final ObsydianPrisons plugin;
    public AutoSellService(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    public boolean isEnabled(UUID playerId) {
        return enabledPlayers.contains(playerId);
    }

    public boolean toggle(Player player) {
        UUID playerId = player.getUniqueId();
        if (enabledPlayers.contains(playerId)) {
            disable(playerId);
            return false;
        }

        enable(player);
        return true;
    }

    public void enable(Player player) {
        UUID playerId = player.getUniqueId();
        if (!enabledPlayers.add(playerId)) return;

        int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new AutoSellTask(this, plugin.getSellService(), player), 0L, 20L*30L);
        runningTasks.put(playerId, task);
        playerItemsToSell.put(playerId, new ItemStack[0]);
    }
    public void appendItems(ItemStack[] items, UUID playerId) {
        ItemStack[] itemsToSell = playerItemsToSell.getOrDefault(playerId, new ItemStack[0]);
        ItemStack[] newItems = new ItemStack[items.length + itemsToSell.length];
        System.arraycopy(itemsToSell, 0, newItems, 0, itemsToSell.length);
        System.arraycopy(items, 0, newItems, itemsToSell.length, items.length);
        playerItemsToSell.put(playerId, newItems);
    }
    public ItemStack[] getItemsToSell(UUID playerId) {
        return playerItemsToSell.getOrDefault(playerId, new ItemStack[0]);
    }
    public void clearItems(UUID playerId) {
        playerItemsToSell.put(playerId, new ItemStack[0]);
    }
    public void disable(UUID playerId) {
        enabledPlayers.remove(playerId);
        Integer taskId = runningTasks.remove(playerId);
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
        playerItemsToSell.remove(playerId);
    }
}
