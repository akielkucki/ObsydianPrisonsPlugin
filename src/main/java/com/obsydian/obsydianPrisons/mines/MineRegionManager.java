package com.obsydian.obsydianPrisons.mines;

import org.bukkit.block.Block;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class MineRegionManager {
    private List<MineCuboid> regions = List.of();

    public void loadDirectory(Path directory) {
        // TODO Read the YAML files, parse each cuboid, and replace regions with an immutable list.
    }

    public Optional<MineCuboid> findMine(Block block) {
        // TODO Search the cached regions and return the first cuboid containing this block.
        return Optional.empty();
    }

    public boolean isMineBlock(Block block) {
        return findMine(block).isPresent();
    }

    public List<MineCuboid> getRegions() {
        return regions;
    }
}
