package com.obsydian.obsydianprisons.pickaxe.listener;

import com.lukemango.plotmines.listener.MineCreatedEvent;
import com.lukemango.plotmines.listener.MineDeletedEvent;
import com.lukemango.plotmines.manager.impl.Mine;
import com.lukemango.plotmines.util.FinePosition;
import com.obsydian.obsydianprisons.ObsydianPrisons;
import com.obsydian.obsydianprisons.mine.MineCuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class MineEvents implements Listener {
    private final ObsydianPrisons plugin;
    public MineEvents(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onMineCreate(MineCreatedEvent event) {
        Mine mine = event.getMine();
        UUID mineId = mine.getUuid();
        Location first = toLocation(mine.getMinimum());
        Location last = toLocation(mine.getMaximum());

        plugin.getMineRegionManager().addOrUpdatePlotMine(mine.getUuid(), MineCuboid.from(
                mineId.toString(),
                first,
                last
        ));
        event.getCreator().sendMessage("Created mine at " + first + " to " + last);
    }
    @EventHandler
    public void onMineDelete(MineDeletedEvent event) {
        UUID mineId = event.getMine().getUuid();
        plugin.getMineRegionManager().removePlotMine(mineId);
    }
    private Location toLocation(FinePosition finePosition) {
        double x = finePosition.x(), y = finePosition.y(), z = finePosition.z();
        World world = Bukkit.getWorld(finePosition.world());
        return new Location(world,x,y,z);
    }
}
