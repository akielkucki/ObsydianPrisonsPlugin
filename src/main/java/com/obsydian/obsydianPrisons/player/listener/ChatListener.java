package com.obsydian.obsydianprisons.player.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutionException;

public final class ChatListener implements Listener {

    private static final String RANK_PLACEHOLDER = "%prisonranksx_currentrank_name%";
    private static final String GROUP_PLACEHOLDER = "%luckperms_prefix%";

//    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
//            LegacyComponentSerializer.builder()
//                    .character('&')
//                    .hexColors()
//                    .useUnusualXRepeatedCharacterHexFormat()
//                    .build();

    private final JavaPlugin plugin;

    public ChatListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String playerMinePrefix = resolveRankPrefix(event.getPlayer(), RANK_PLACEHOLDER);
        String playerGroupPrefix = resolveRankPrefix(event.getPlayer(), GROUP_PLACEHOLDER);
        event.renderer((source, sourceDisplayName, message, viewer) -> {
            TextComponent.Builder result = Component.text();
            Component rankFormat = MiniMessage.miniMessage().deserialize(String.format("<#737373>[</#737373><#00ff08><bold>%s</#00ff08><#737373>]</#737373> ",playerMinePrefix));
            Component groupFormat = MiniMessage.miniMessage().deserialize(
                    playerGroupPrefix.isBlank() ? " " :
                    String.format("<#737373>[</#737373>%s<#737373>]</#737373> ",playerGroupPrefix)
            );
            result.append(rankFormat)
                    .append(groupFormat)
                    .append(sourceDisplayName)
                    .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(message);

            return result.build();
        });
    }

    private String resolveRankPrefix(Player player, String placeholder) {
        String parsedRank;

        try {
            // PlaceholderAPI expansions may use Bukkit APIs, so parse on the
            // main server thread instead of the asynchronous chat thread.
            parsedRank = getParsedRank(player,placeholder);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return "";
        } catch (ExecutionException exception) {
            plugin.getLogger().warning(
                    "Could not resolve chat rank for "
                            + player.getName()
                            + ": "
                            + exception.getCause().getMessage()
            );
            return "";
        }

        if (parsedRank == null
                || parsedRank.isBlank()
                || parsedRank.equalsIgnoreCase(placeholder)) {
            return "";
        }

        // Supports both '&' and section-sign legacy colors returned by PAPI.
        parsedRank = parsedRank.replace('§', '&');

        return parsedRank;
    }

    private String getParsedRank(Player player, String placeholder) throws InterruptedException, ExecutionException {
        return Bukkit.getScheduler()
                .callSyncMethod(
                        plugin,
                        () -> PlaceholderAPI.setPlaceholders(
                                player,
                                placeholder
                        )
                )
                .get();
    }
}