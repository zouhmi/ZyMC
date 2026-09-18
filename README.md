# ZyMC

A Minecraft server written from scratch in Java, targeting 1.21.11.

## Build

Requires JDK 21.

    ./gradlew clean build

## Modules

- `zmc-core` - shared low-level: math, buffers, logging, config, collections, ids
- `zymc-network` - protocol layer: packets, connection lifecycle, Netty pipeline
- `zymc-world` - world, chunks, regions, chunk I/O, generation, block data
- `zymc-entity` - entity system, tick loop, entity storage, spawns
- `zymc-game` - physics, pathfinding, redstone, lighting, gameplay mechanics
- `zymc-plugins` - Bukkit/Spigot/Paper API bridge: events, scheduler, commands, permissions, lifecycle
- `zymc-server` - top-level server entry, bootstrap, integration of all subsystems
- `zymc-benchmark` - load-test harness, benchmark logger, synthetic clients

## Current status

Bootstrap complete: Gradle 8.13 + JDK 21.0.9 build, clean compile of all modules,
placeholder server and benchmark entry points. No gameplay yet.

## Target

- 16 GB RAM, 250 concurrent players, stable 20 TPS
- Vanilla-accurate gameplay, no player-facing changes for performance
- Full native support for Bukkit, Spigot, and Paper plugins
- Tiered RAM testing: 1 GB, 2 GB, 4 GB, 16 GB
