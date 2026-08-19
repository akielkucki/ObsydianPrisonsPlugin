package com.obsydian.obsydianprisons.player.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class FakeSpecsCommand implements CommandExecutor {

    private static final TextColor LABEL_COLOR = TextColor.color(0xFF3B30);
    private static final TextColor VALUE_COLOR = TextColor.color(0x39FF14);
    private static final TextColor ACCENT_COLOR = TextColor.color(0xC000FF);

    private final JavaPlugin plugin;

    public FakeSpecsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    ThreadLocalRandom random = ThreadLocalRandom.current();
    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text(
                "Running hardware diagnostic...",
                NamedTextColor.YELLOW
        ));

        sendLater(player, 30, "Gather CPU Hardware Info...");
        sendLater(player, 50, "Measuring bandwidth/latency...");
        sendReport(player, 95);

        return true;
    }

    private void sendLater(Player player, long delay, String message) {
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (player.isOnline()) {
                        player.sendMessage(
                                Component.text("▸ ", ACCENT_COLOR)
                                        .append(Component.text(
                                                message,
                                                NamedTextColor.GRAY
                                        ))
                        );
                    }
                },
                delay
        );
    }

    private void sendReport(Player player, long delay) {
        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> {
                    if (!player.isOnline()) return;

                    player.sendMessage(Component.empty());
                    player.sendMessage(
                            Component.text(
                                    "Internal hardware specifications:",
                                    ACCENT_COLOR
                            )
                    );

                    sendSpec(
                            player,
                            "CPU",
                            "AMD Ryzen Threadripper PRO 9995WX"
                    );
                    sendSpec(
                            player,
                            "GPU",
                            "1x NVIDIA RTX PRO 6000 Blackwell Workstation Edition"
                    );
                    sendSpec(
                            player,
                            "RAM",
                            "Micron DDR5-6400 RDIMM 2Rx4 CL52 ECC x128GB"
                    );
                    sendSpec(
                            player,
                            "Storage",
                            "3946GB/3996GB Micron 9650 NVMe™ SSD"
                    );
                    sendSpec(
                            player,
                            "Network",
                            random.nextDouble(1,2)+" -> GBps"
                    );
                    sendSpec(
                            player,
                            "CHA_FAN connections",
                            "12"
                    );
                    sendSpec(
                            player,
                            "CPU Temperature",
                            random.nextDouble(40,56)+"C"
                    );
                    sendSpec(
                            player,
                            "PPT",
                            random.nextDouble(1,12)+"%"
                    );
                    sendSpec(
                            player,
                            "Location",
                            "New York City, New York, USA"
                    );
                    sendSpec(
                            player,
                            "TPS",
                            "20.000000000000000000000"
                    );
                    sendSpec(
                            player,
                            "Ping",
                            player.getPing() + "ms"
                    );

                    player.sendMessage(Component.empty());


                },
                delay
        );
    }

    private void sendSpec(Player player, String name, String value) {
        player.sendMessage(
                Component.text("  " + name + ": ", LABEL_COLOR)
                        .append(Component.text(value, VALUE_COLOR))
        );
    }
}