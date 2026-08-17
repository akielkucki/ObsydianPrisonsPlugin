# ObsydianPrisons

A PaperMC plugin powering the prison gamemode on [Obsydian](https://obsydian.gg) — custom multi-tool pickaxes, enchant progression, configurable item selling, and per-player token persistence.

[![Paper](https://img.shields.io/badge/Paper-1.21.11-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Build](https://img.shields.io/badge/build-Maven-C71A36)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-Proprietary-red)](LICENSE)

> **Status:** early development (`1.0-SNAPSHOT`). Core loops are playable, but the enchant system and token economy are still being built out — see [Roadmap](#roadmap).

---

## Features

### Custom pickaxe
Every player receives a personal, unbreakable pickaxe on join. The tool is tagged with the owner's UUID in its persistent data container, so it can be identified reliably no matter how it's moved or renamed.

### Multi-tool
Left-clicking a block swaps the pickaxe to the correct tool type on the fly — pickaxe, shovel, axe, or hoe — based on the block's `MINEABLE_*` tag. One item in the hotbar, correct mining speed everywhere. Enchantments and persistent data survive the swap.

### Mining loop
- Drops go straight to the player's inventory instead of onto the ground.
- Fortune scales drop quantities on solid blocks.
- Break particles at the mined block.
- Random gem payouts announced in chat.
- When the inventory fills up, an occasional floating `TextDisplay` hologram and sound prompt the player to `/sell`.

### Selling
`/sell` liquidates every sellable item in the player's inventory and reports a formatted summary of items sold and money earned. A 10-second cooldown applies, with a clickable store link for players who want AutoSell. Prices are fully config-driven — anything not listed in `config.yml` cannot be sold.

### Persistence
Player tokens are stored in a local SQLite database via ORMLite. All database work runs on a dedicated single-threaded executor and returns `CompletableFuture`s, keeping the main server thread free. If the connection can't be opened at startup, the plugin logs the failure and disables itself rather than running in a broken state.

---

## Requirements

| Component | Version |
|---|---|
| **Server** | PaperMC 1.21.11+ (uses the Paper API, not plain Bukkit/Spigot) |
| **Java** | 21 or newer |
| **Build** | Maven 3.6+ |

---

## Installation

1. Download or build `ObsydianPrisons-1.0-SNAPSHOT.jar` (see [Building](#building)).
2. Drop the jar into your server's `plugins/` directory.
3. Start the server. Default config and the player database are generated on first run.
4. Edit `plugins/ObsydianPrisons/config.yml` to set your prices, then restart.

---

## Building

```bash
git clone https://github.com/akielkucki/ObsydianPrisonsPlugin.git
cd ObsydianPrisonsPlugin
mvn clean package
```

The shaded jar lands in `target/ObsydianPrisons-1.0-SNAPSHOT.jar`.

Runtime dependencies (Triumph GUI, ORMLite, SQLite JDBC) are shaded into the jar and relocated under `com.obsydian.obsydianPrisons.libs.*` to avoid clashing with other plugins on the server.

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/sell` | Sells every sellable item in your inventory | *none* |

---

## Configuration

`config.yml` is a flat map of Bukkit `Material` names to their per-item value. Materials absent from the file are worth nothing and are skipped by `/sell`.

```yaml
DIAMOND: 100.0
EMERALD: 75.0
GOLD_INGOT: 25.0
IRON_INGOT: 10.0
COAL: 2.0
ROTTEN_FLESH: 0.25
```

Material names must match the [Bukkit `Material` enum](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) exactly.

---

## Data storage

Player data lives in `plugins/ObsydianPrisons/player_data.db` (SQLite). The `player_data` table is created automatically on first startup.

| Column | Type | Notes |
|---|---|---|
| `uuid` | `String` | Primary key — the player's UUID |
| `tokens` | `long` | Token balance |

---

## Project layout

```
src/main/java/com/obsydian/obsydianPrisons/
├── ObsydianPrisons.java          # Plugin entry point, wiring, lifecycle
├── daos/
│   └── DatabaseManager.java      # Async SQLite/ORMLite access layer
├── models/
│   └── PlayerData.java           # ORMLite entity
├── pickaxe/
│   ├── lib/                      # Pickaxe factory, NamespacedKeys, enchant GUI
│   └── listeners/                # Join, break, multi-tool, enchant menu
└── sell/
    ├── cfg/ConfigManager.java    # Material price lookup
    └── commands/SellCommand.java # /sell
```

---

## Roadmap

- [ ] Wire the enchant GUI to real levels, costs, and token spending
- [ ] Persist gem/token payouts from mining to the database
- [ ] Move hardcoded values (pickaxe material, enchant levels, gem rates) into config
- [ ] Ranks, mines, and prestige progression
- [ ] Permissions for `/sell` and admin commands
- [ ] Configurable sell cooldown and an AutoSell permission node

---

## Contributing

This is a closed-source project. Contributions are accepted from team members and invited collaborators only — please keep changes focused, match the existing package layout, and target Java 21. By submitting a contribution you agree that it becomes part of the Software under the terms of the [LICENSE](LICENSE).

## License

Proprietary — see [LICENSE](LICENSE). Copyright © 2026 Obsydian, all rights reserved.

You may download the plugin and run it privately, including on servers you own or operate. You may **not** redistribute it, resell it, bundle it into a server pack or hosting package, or distribute modified versions. See the [LICENSE](LICENSE) file for the full terms.
