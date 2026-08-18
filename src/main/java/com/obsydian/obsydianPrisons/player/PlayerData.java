package com.obsydian.obsydianprisons.player;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "player_data")
public class PlayerData {

    @DatabaseField(id = true, canBeNull = false)
    private String uuid;

    @DatabaseField(canBeNull = false)
    private long tokens;

    // ORMLite requires a no-argument constructor.
    protected PlayerData() {
    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid.toString();
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
}
