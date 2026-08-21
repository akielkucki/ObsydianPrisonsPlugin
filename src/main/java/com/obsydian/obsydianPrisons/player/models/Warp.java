package com.obsydian.obsydianprisons.player.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

@DatabaseTable(tableName = "warps")
public class Warp {
    @DatabaseField(id = true, unique = true, canBeNull = false, columnName = "warp_name")
    private String warpName;
    @DatabaseField(canBeNull = false, columnName = "world_uuid")
    private String worldUuid;

    @DatabaseField(canBeNull = false)
    private double x;

    @DatabaseField(canBeNull = false)
    private double y;

    @DatabaseField(canBeNull = false)
    private double z;

    @DatabaseField(canBeNull = false)
    private float yaw;

    @DatabaseField(canBeNull = false)
    private float pitch;

    public Warp(String warpName,String worldUuid, double x, double y, double z, float yaw, float pitch) {
        this.warpName = warpName;
        this.worldUuid = worldUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    public Warp() {}
    public String getWarpName() {
        return warpName;
    }
    public Location getLocation() {
        World world = Bukkit.getWorld(UUID.fromString(worldUuid));

        if (world == null) {
            throw new IllegalStateException(
                    "Warp world is not loaded: " + worldUuid
            );
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}
