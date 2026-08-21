package com.obsydian.obsydianprisons.player.placeholders;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class ObsydianExpansion extends PlaceholderExpansion {


    @Override
    public String getIdentifier() {
        return "obsydianprisons";
    }

    @Override
    public String getAuthor() {
        return "ObsydianTeam";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
    private final ObsydianPrisons plugin;
    public ObsydianExpansion(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return null;

        // Example placeholders
        switch (params.toLowerCase()) {
            case "multiplier":
                return plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).getMultiplier() + "x";
            default:
                return null;
        }
    }

}