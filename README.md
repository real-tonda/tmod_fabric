# TMod Fabric

A server-side Fabric mod for Minecraft 1.21.8.

### Coded with the help of AI

## Features

### Core Features
- **Skin Restoration** - Compatible with non-cracked players on cracked servers
- **Server Administration** - Comprehensive command suite for server management
- **Mod Feature Control** - Disable/enable specific client mod features server-side
- **Item Blacklist** - Prevent usage of specific items
- **QOL Tweaks** - Configurable game behavior improvements:
  - Villager infinite restocks
  - Faster villager breeding
  - Remove anvil "too expensive" limit
  - Adjustable hopper transfer speed

## Commands

### Player Commands (Public)
- `/thelp` - Show this help message
- `/tps` - Show server TPS
- `/ping [player]` - Show player latency
- `/seen <player>` - Check when player was last online
- `/skin <set|clear|update> [args]` - Manage player skins

### Operator Commands (Permission Level 4)

#### Gameplay
- `/fly [player]` - Toggle flight mode
- `/gmc [player]` - Set gamemode to Creative
- `/gms [player]` - Set gamemode to Survival
- `/gma [player]` - Set gamemode to Adventure
- `/gmsp [player]` - Set gamemode to Spectator
- `/god [player]` - Toggle god mode (min 0.5 hearts)
- `/freeze <player>` - Freeze/unfreeze a player

#### Information & Teleportation
- `/whois <player>` - Show detailed player information
- `/dims` - List all dimensions with player counts
- `/dim <dimension>` - Teleport to a dimension

#### Server Management
- `/broadcast <message>` - Send server-wide announcement
- `/bc <message>` - Alias for /broadcast
- `/blacklist <add|remove|list|clear>` - Manage item blacklist
- `/modcfg <list|enable|disable|toggle|info>` - Configure mod features

#### Utilities & Configuration
- `/tutils` - Show tutils information
- `/tutils cfg <key> <value>` - Configure QOL game behavior
  - **Boolean configs:** `villager.infiniteRestocks`, `villager.fasterBreeding`, `anvil.notExpensive`
  - **Integer configs:** `redstone.hopperTicks` (1-20, default: 8) - Control hopper transfer speed
- `/tutils repeatcmd <command> <times>` - Repeat a command multiple times (max 1000)

## Building

1. Clone this repository
2. Run `./gradlew build` (Linux/Mac) or `gradlew.bat build` (Windows)
3. The built mod will be in `build/libs/`

## Please report any bugs in the bugtracker

## License

MIT License
