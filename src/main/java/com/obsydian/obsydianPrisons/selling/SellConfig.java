package com.obsydian.obsydianprisons.selling;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class SellConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private final Map<String, Double> materialWorths;

    public SellConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        materialWorths = new HashMap<>();
    }

    public void setupConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    public void loadFromDisk() {
        for (Material material : Material.values()) {
            if (config.contains(material.name())) {
                materialWorths.put(material.name(), config.getDouble(material.name()));
            }
        }
    }

    public double getWorth(Material material) {
        return materialWorths.getOrDefault(material.name(), 0.0);
    }

    public double getWorth(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }

        return getWorth(item.getType()) * item.getAmount();
    }
    public double getTokenWorth(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0.0;
        }
        return Math.log10(getWorth(item.getType()) * item.getAmount());
    }

    public boolean canSell(Material material) {
        return getWorth(material) > 0;
    }

    public void setWorth(Material material, double worth) {
        config.set(material.name(), worth);
        plugin.saveConfig();
    }
}
