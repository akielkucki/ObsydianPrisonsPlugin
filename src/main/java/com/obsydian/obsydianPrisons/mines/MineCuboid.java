package com.obsydian.obsydianPrisons.mines;

import com.obsydian.obsydianPrisons.models.MinePosition;
import org.bukkit.Location;
import org.bukkit.World;
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
        if (first == null || second == null) {
            throw new IllegalArgumentException("Both locations must be non-null");
        }
        return new MineCuboid(
                name,
                first.getWorld().getUID(),

                Math.min(first.getBlockX(), second.getBlockX()),
                Math.min(first.getBlockY(), second.getBlockY()),
                Math.min(first.getBlockZ(), second.getBlockZ()),

                Math.max(first.getBlockX(), second.getBlockX()),
                Math.max(first.getBlockY(), second.getBlockY()),
                Math.max(first.getBlockZ(), second.getBlockZ())
        );
    }
    public static Location parseMineLocation(MinePosition position, World world) {
        if (world == null) {
            throw new IllegalArgumentException(
                    "Mine world must be loaded"
            );
        }
        return new Location(world, position.x(), position.y(), position.z());
    }

    public boolean contains(Block block) {
        if (!block.getWorld().getUID().equals(worldId)) {
            return false;
        }
        Location l = block.getLocation();
        int lx = l.getBlockX();
        int ly = l.getBlockY();
        int lz = l.getBlockZ();
        return lx >= minX
                && lx <= maxX
                && ly >= minY
                && ly <= maxY
                && lz >= minZ
                && lz <= maxZ;
    }
}
