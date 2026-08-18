package com.obsydian.obsydianprisons.player;

import java.util.UUID;

public final class PlayerDataManager {

    private final PlayerDataCache playerDataCache;

    public PlayerDataManager(PlayerDataCache playerDataCache) {
        this.playerDataCache = playerDataCache;
    }

    public long addTokens(UUID uuid, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Token amount cannot be negative"
            );
        }

        PlayerData data = requirePlayerData(uuid);

        long newBalance = Math.addExact(
                data.getTokens(),
                amount
        );

        data.setTokens(newBalance);
        playerDataCache.markDirty(uuid);

        return newBalance;
    }

    public long removeTokens(UUID uuid, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Token amount cannot be negative"
            );
        }

        PlayerData data = requirePlayerData(uuid);

        if (data.getTokens() < amount) {
            throw new IllegalStateException(
                    "Player does not have enough tokens"
            );
        }

        long newBalance = data.getTokens() - amount;

        data.setTokens(newBalance);
        playerDataCache.markDirty(uuid);

        return newBalance;
    }

    public long getTokens(UUID uuid) {
        return requirePlayerData(uuid).getTokens();
    }

    private PlayerData requirePlayerData(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);

        if (data == null) {
            throw new IllegalStateException(
                    "Player data is not loaded for " + uuid
            );
        }

        return data;
    }
}
