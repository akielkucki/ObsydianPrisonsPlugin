package com.obsydian.obsydianprisons.mine;

import java.util.Map;
import java.util.UUID;

public record MineData(
        UUID uuid,
        UUID owner,
        String mineType,
        String displayName,
        MinePosition minimum,
        MinePosition maximum,
        double resetPercentage,
        int resetDelay,
        MinePosition resetTeleportLocation,
        Map<String, Double> composition,
        int totalBlocks
) {}
