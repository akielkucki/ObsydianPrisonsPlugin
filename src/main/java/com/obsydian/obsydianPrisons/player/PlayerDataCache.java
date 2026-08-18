package com.obsydian.obsydianprisons.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataCache {
    public static PlayerDataCache instance = new PlayerDataCache();
    private final Map<UUID, PlayerData> playerDataCache =
            new ConcurrentHashMap<>();

    private final Set<UUID> dirtyPlayers =
            ConcurrentHashMap.newKeySet();

    public PlayerData get(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public void put(UUID uuid, PlayerData data) {
        playerDataCache.put(uuid, data);
    }

    public boolean contains(UUID uuid) {
        return playerDataCache.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        playerDataCache.remove(uuid);
        dirtyPlayers.remove(uuid);
    }

    public void markDirty(UUID uuid) {
        dirtyPlayers.add(uuid);
    }

    public boolean isDirty(UUID uuid) {
        return dirtyPlayers.contains(uuid);
    }

    public void markClean(UUID uuid) {
        dirtyPlayers.remove(uuid);
    }

    public Set<UUID> getDirtyPlayersSnapshot() {
        return Set.copyOf(dirtyPlayers);
    }
}
