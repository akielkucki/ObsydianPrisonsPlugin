package com.obsydian.obsydianPrisons.mines;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

public record MineCuboid(
        String name,
        UUID worldId,
        int minX,
        int minY,
        int minZ,
        int maxX,
        int maxY,
        int maxZ
) {
    public static MineCuboid from(String name, Location first, Location second) {
        // TODO Validate both locations and normalize their minimum/maximum coordinates.
        throw new UnsupportedOperationException("TODO: create a mine cuboid from two locations");
    }

    public boolean contains(Block block) {
        // TODO Check the world UUID and whether the block coordinates are inside this cuboid.
        return false;
    }
}
