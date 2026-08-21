package com.obsydian.obsydianprisons.player.models;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public final class Settings {

    private static final Gson GSON = new Gson();

    private boolean autoSellEnabled;

    public Settings() {
        this.autoSellEnabled = false;
    }

    public String serialize() {
        return GSON.toJson(this);
    }

    public static Settings deserialize(String json) {
        if (json == null || json.isBlank()) {
            return new Settings();
        }

        try {
            Settings settings = GSON.fromJson(json, Settings.class);
            return settings != null ? settings : new Settings();
        } catch (JsonSyntaxException exception) {
            // Consider logging corrupted JSON here.
            return new Settings();
        }
    }

    public boolean isAutoSellEnabled() {
        return autoSellEnabled;
    }

    public void setAutoSellEnabled(boolean autoSellEnabled) {
        this.autoSellEnabled = autoSellEnabled;
    }
}