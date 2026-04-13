workspace "Robocode Tank Royale" {
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
        tankRoyale = softwareSystem "Robocode Tank Royale" "Real-time tank battle arena" {
            # Containers (all independent artifacts published to Maven Central)
            server = container "Server" "Kotlin (JVM)" "Runs battles at 30 TPS, manages state, enforces rules. Independent CLI artifact."
            gui = container "GUI" "Kotlin + Java Swing" "Desktop application for visualizing battles. Can optionally embed Server, Booter, Recorder."
            booter = container "Booter" "Kotlin (JVM)" "Boots up bots from local directories, manages bot processes. Independent CLI artifact."
            recorder = container "Recorder" "Kotlin (JVM)" "Records game events to ND-JSON files for playback. Independent CLI artifact."
            javaApi = container "Java Bot API" "Java library (JAR)" "Enables bot developers to write bots in Java"
            pythonApi = container "Python Bot API" "Python 3 package" "Enables bot developers to write bots in Python"
            dotnetApi = container ".NET Bot API" "C# (.NET Core)" "Enables bot developers to write bots in C#"

            # Container relationships
            gui -> server "WebSocket" "Game state, control commands"
            gui -> booter "Optionally embeds" "For convenient local bot launching"
            gui -> recorder "Optionally embeds" "For convenient local recording"
            booter -> server "WebSocket" "Bot handshake on behalf of bots"
            recorder -> server "WebSocket" "Observes game events"
            javaApi -> server "WebSocket" "Bot handshake, intents"
            pythonApi -> server "WebSocket" "Bot handshake, intents"
            dotnetApi -> server "WebSocket" "Bot handshake, intents"
        }
        # External systems
        fileSystem = softwareSystem "File System" "Local storage for replays, configs, bot directories"

        # Actor relationships
        botDev -> javaApi "Uses"
        botDev -> pythonApi "Uses"
        botDev -> dotnetApi "Uses"
        spectator -> gui "Watches battles"
        admin -> gui "Configures tournaments"
        gui -> fileSystem "Read/Write" "Replays, configurations"
        recorder -> fileSystem "Write" "ND-JSON recordings"
        booter -> fileSystem "Read" "Bot directories"
    }
    views {
        container tankRoyale "TankRoyale-Container" "Robocode Tank Royale - Container Diagram" {
            include *
            autoLayout
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
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
        }
    }
}
