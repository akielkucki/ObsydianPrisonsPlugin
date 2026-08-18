package com.obsydian.obsydianprisons.mine;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MineRegionManager {
    private static final Logger log = LoggerFactory.getLogger(MineRegionManager.class);
    private List<MineCuboid> regions = List.of();
    private final Gson gson = new Gson();

    public void loadDirectory(Path directory) {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(
                    "Mine directory does not exist: " + directory
            );
        }
        List<MineCuboid> loaded = new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        if (isYamlFile(path)) {
                            loadYamlFile(path, loaded);
                        } else if (isJsonFile(path)) {
                            loadJsonFile(path, loaded);
                        }
                    });
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Could not read mine directory: " + directory,
                    exception
            );
        }

        regions = List.copyOf(loaded);
        log.info("Loaded {} mine regions from {}", regions.size(), directory);
    }

    private void loadYamlFile(Path path, List<MineCuboid> loaded) {
        log.info("Loading mine region file: {}", path);

        try {
            String contents = Files.readString(path);

            // Remove Bukkit ConfigurationSerializable markers.
            String sanitized = contents.lines().filter(line -> !line.stripLeading().startsWith("==:")).collect(Collectors.joining("\n"));

            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(sanitized);

            String mineName = config.getString("Mine.name", path.getFileName().toString());

            String regionType = config.getString("Mine.region.type");

            if (!"CUBOID".equalsIgnoreCase(regionType)) {
                log.warn("Skipping non-cuboid mine {} with type {}", mineName, regionType);
                return;
            }

            String worldName = config.getString("Mine.region.world");

            World world = Bukkit.getWorld(worldName);

            if (world == null) {
                log.warn("Skipping mine {} because world {} is not loaded", mineName, worldName);
                return;
            }

            Location first = readLocation(config, "Mine.region.p1", world);

            Location second = readLocation(config, "Mine.region.p2", world);

            if (first == null || second == null) {
                log.warn("Skipping mine {} because its region is invalid", mineName);
                return;
            }

            loaded.add(MineCuboid.from(mineName, first, second));

        } catch (IOException | InvalidConfigurationException exception) {
            log.warn("Could not load mine region file {}", path, exception);
        }
    }

    private Location readLocation(YamlConfiguration config, String path, World world) {
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section == null) {
            return null;
        }

        if (!section.contains("x") || !section.contains("y") || !section.contains("z")) {
            return null;
        }

        return new Location(world, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
    }
    private void loadJsonFile(
            Path path,
            List<MineCuboid> loaded
    ) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            MineData[] mines =
                    gson.fromJson(reader, MineData[].class);

            if (mines == null) {
                log.warn("JSON file contains no mines: {}", path);
                return;
            }

            for (MineData mine : mines) {
                if (mine == null ||
                        mine.minimum() == null ||
                        mine.maximum() == null) {
                    log.warn("Skipping invalid mine in {}", path);
                    continue;
                }

                World world = Bukkit.getWorld(
                        mine.minimum().world()
                );

                if (world == null) {
                    log.warn(
                            "Skipping mine {} because world {} is not loaded",
                            mine.uuid(),
                            mine.minimum().world()
                    );
                    continue;
                }

                Location first = MineCuboid.parseMineLocation(
                        mine.minimum(),
                        world
                );

                Location second = MineCuboid.parseMineLocation(
                        mine.maximum(),
                        world
                );

                String name = mine.uuid() != null
                        ? mine.uuid().toString()
                        : path.getFileName().toString();

                loaded.add(MineCuboid.from(
                        name,
                        first,
                        second
                ));
            }
        } catch (IOException | JsonParseException ignored) {
            log.warn("Could not parse mine file {} likely not a mine file", path);
        }
    }

    private Optional<MineCuboid> findMine(Block block) {
        return regions.stream()
                .filter(region -> region.contains(block))
                .findFirst();
    }


    public boolean isMineBlock(Block block) {
        return findMine(block).isPresent();
    }
    public MineCuboid getMineRegion(Block block) {
        return findMine(block)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Block is not in a mine region"
                        )
                );
    }

    public List<MineCuboid> getRegions() {
        return regions;
    }


    private boolean isYamlFile(Path path) {
        String name = path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return name.endsWith(".yml") || name.endsWith(".yaml");
    }

    private boolean isJsonFile(Path path) {
        return path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT)
                .endsWith(".json");
    }
}
