package com.obsydian.obsydianprisons.player;

import com.obsydian.obsydianprisons.ObsydianPrisons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlayerDataFlushTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PlayerDataFlushTask.class);
    private final ObsydianPrisons plugin;

    public PlayerDataFlushTask(ObsydianPrisons plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getDatabaseManager()
                .flush()
                .exceptionally(error -> {
                    plugin.getSLF4JLogger().error(
                            "Could not flush player data",
                            error
                    );
                    // Debug
                    log.error("Could not flush player data", error);
                    return null;
                }).thenRun(() -> {
                    // Debug
                    log.info("Flushed player data");
                });
    }
}
