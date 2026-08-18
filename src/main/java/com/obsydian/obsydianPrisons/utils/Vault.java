package com.obsydian.obsydianPrisons.utils;

import net.milkbowl.vault2.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Vault {

    private static Economy economy;

    private Vault() {}

    public static boolean setup(JavaPlugin plugin) {
        var vaultPlugin =
                plugin.getServer().getPluginManager().getPlugin("Vault");

        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            plugin.getLogger().severe("VaultUnlocked is missing or disabled.");
            return false;
        }

        RegisteredServiceProvider<Economy> registration =
                plugin.getServer()
                        .getServicesManager()
                        .getRegistration(Economy.class);

        if (registration == null) {
            plugin.getLogger().severe(
                    "No Vault2 economy provider is registered."
            );
            return false;
        }

        economy = registration.getProvider();

        plugin.getLogger().info(
                "Using Vault2 economy provider: " +
                        registration.getPlugin().getDescription().getFullName()
        );

        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }
}