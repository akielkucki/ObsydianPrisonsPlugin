package com.obsydian.obsydianprisons.player.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.List;

public final class RefreshServerListEvent implements Listener {

    private static final MiniMessage MINI_MESSAGE =
            MiniMessage.miniMessage();

    private static final long ROTATION_INTERVAL_MILLIS = 5_000L;

    private static final String BRAND_LINE =
            "    <gradient:#7C3AED:#E9D5FF>"
                    + "<bold>OBSYDIAN PRISONS</bold>"
                    + "</gradient>"
                    + " <dark_gray>◆</dark_gray>"
                    + " <green>1.9+</green>";

    private static final List<String> ROTATING_LINES = List.of(
            "  <gray>Forge your fortune.</gray> "
                    + "<light_purple><bold>Rule the mines.</bold></light_purple>",

            "  <gold><bold>RANK UP</bold></gold> "
                    + "<dark_gray>•</dark_gray> "
                    + "<light_purple><bold>POWER UP</bold></light_purple> "
                    + "<dark_gray>•</dark_gray> "
                    + "<aqua><bold>BREAK LIMITS</bold></aqua>",

            "  <light_purple>Custom Enchants</light_purple> "
                    + "<dark_gray>◆</dark_gray> "
                    + "<yellow>Massive Rewards</yellow> "
                    + "<dark_gray>◆</dark_gray> "
                    + "<green>Player Mines</green>",

            "  <green>{online} prisoners online</green> "
                    + "<dark_gray>◆</dark_gray> "
                    + "<white>Your empire starts now.</white>"
    );

    @EventHandler
    public void onServerListRefresh(ServerListPingEvent event) {
        long rotation = System.currentTimeMillis()
                / ROTATION_INTERVAL_MILLIS;

        int messageIndex = (int) (
                rotation % ROTATING_LINES.size()
        );

        String rotatingLine = ROTATING_LINES
                .get(messageIndex)
                .replace(
                        "{online}",
                        Integer.toString(event.getNumPlayers())
                );

        Component motd = MINI_MESSAGE.deserialize(BRAND_LINE)
                .append(Component.newline())
                .append(MINI_MESSAGE.deserialize(rotatingLine));

        event.motd(motd);
    }
}