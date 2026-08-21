package com.obsydian.obsydianprisons.player;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.obsydian.obsydianprisons.player.models.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@DatabaseTable(tableName = "player_data")
public class PlayerData {

    private static final Logger log = LogManager.getLogger(PlayerData.class);
    @DatabaseField(id = true, canBeNull = false)
    private String uuid;

    @DatabaseField(canBeNull = false)
    private long tokens;
    @DatabaseField(canBeNull = false, defaultValue = "1.0")
    private double multiplier;
    @DatabaseField(
            columnName = "settings",
            canBeNull = false,
            dataType = DataType.LONG_STRING
    )
    private String settingsJson = new Settings().serialize();
    // ORMLite requires a no-argument constructor.
    protected PlayerData() {

    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid.toString();
        this.multiplier = 1.0;
    }

    public UUID getUuid() {
        return UUID.fromString(uuid);
    }

    public long getTokens() {
        return tokens;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }
    public double getMultiplier() {
        return multiplier;
    }
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
    public Settings getSettings() {
        try {
            return Settings.deserialize(settingsJson);
        } catch (Exception e) {
            return new Settings();
        }
    }
    public void setSettings(String settings) {
        this.settingsJson = settings;

    }
}
