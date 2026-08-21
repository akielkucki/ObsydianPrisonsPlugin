package com.obsydian.obsydianprisons.player;

import com.obsydian.obsydianprisons.player.models.Warp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WarpCache {
    public static WarpCache instance = new WarpCache();
    private Map<String, Warp> warps = new ConcurrentHashMap<>();
    public Warp getWarp(String name) {
        return warps.get(name);
    }
    public void addWarp(String name, Warp warp) {
        warps.put(name, warp);
    }
    public void removeWarp(String name) {
        warps.remove(name);
    }
    public void clear() {
        warps.clear();
    }
    public Map<String, Warp> getWarpsMap() {
        return warps;
    }
    public List<String> getWarpNames() {
        return List.copyOf(warps.keySet());
    }
    public boolean contains(String name) {
        return warps.containsKey(name);
    }
}
