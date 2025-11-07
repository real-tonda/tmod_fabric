# TMod Fabric

A server-side Fabric mod for Minecraft 1.20.1.

### Coded with the help of AI

## Features

- Skin restoration for non-cracked players on cracked servers
- Useful commands for server administration

## Commands

    Player Commands (Public)
        /thelp Show this help message
        /tps Show server TPS
        /ping [player] Show player latency
        /seen <player> Check when player was last online
        /skin <set|clear|update> [args] Manage player skins
        
    Operator Commands (Permission Level 4)
        /fly [player] - Toggle flight mode
        /gmc [player] - Set gamemode to Creative
        /gms [player] - Set gamemode to Survival
        /gma [player] - Set gamemode to Adventure
        /gmsp [player] - Set gamemode to Spectator
        /whois <player> - Show detailed player information
        /freeze <player> - Freeze/unfreeze a player
        /god [player] - Toggle god mode (min 0.5 hearts)
        /broadcast <message> - Send server-wide announcement
        /bc <message> - Alias for /broadcast
        /dims - List all dimensions with player counts
        /dim <dimension> - Teleport to a dimension
        /blacklist <add|remove|list|clear> - Manage item blacklist *currently broken*
        /modcfg <list|enable|disable|toggle|info> - Configure mod features

## Building

1. Clone this repository
2. Run `./gradlew build` (Linux/Mac) or `gradlew.bat build` (Windows)
3. The built mod will be in `build/libs/`

## License

MIT License
