workspace "Robocode Tank Royale - System Context" {
    model {
        # External actors
        botDev = person "Bot Developer" "Develops AI bots in Java/Python/.NET" {
            tags "Person"
        }
        spectator = person "Spectator" "Watches battles live or replays" {
            tags "Person"
        }
        admin = person "Battle Operator" "Manages tournaments and configurations" {
            tags "Person"
        }
        # Main system
        tankRoyale = softwareSystem "Robocode Tank Royale" "Real-time programming game where developers write AI bots that battle autonomously in a 2D arena"
        # External systems
        userBots = softwareSystem "Bots" "Custom bot code written by developers using Robocode Tank Royale Bot APIs"
        fileSystem = softwareSystem "File System" "Local storage for replays, configurations, and bot directories"
        # Actor relationships
        botDev -> userBots "Develops" "Source code"
        botDev -> tankRoyale "Runs bots against" "WebSocket connection"
        spectator -> tankRoyale "Watches battles in" "GUI"
        admin -> tankRoyale "Configures and manages" "GUI, config files"
        # System relationships
        userBots -> tankRoyale "Connects to" "WebSocket: Bot handshake, movement intents, game events"
        tankRoyale -> fileSystem "Reads/Writes" "Replays, configurations, bot directories"
    }
    views {
        systemContext tankRoyale {
            include *
            autoLayout lr
        }
        styles {
            element "Person" {
                background #08427b
                color #ffffff
                shape Person
            }
            element "Software System" {
                background #1168bd
                color #ffffff
                shape Box
            }
            relationship "Relationship" {
                thickness 2
                color #707070
            }
        }
    }
}
