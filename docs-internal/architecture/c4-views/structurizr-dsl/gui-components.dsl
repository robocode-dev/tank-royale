workspace "Robocode Tank Royale GUI" {
    model {
        # External actors
        user = person "User" "Spectator or Battle Operator" {
            tags "Person"
        }
        # External systems
        gameServer = softwareSystem "Server" "Runs battles at 30 TPS"
        fileSystem = softwareSystem "File System" "Local storage"
        # GUI system
        guiSystem = softwareSystem "GUI Application" {
            guiContainer = container "GUI Container" "Desktop application" {
                battleArena = component "Battle Arena" "Renders tanks, bullets, explosions in real-time"
                controlPanel = component "Control Panel" "Start, stop, pause battles, adjust TPS"
                botSelector = component "Bot Selector" "Browse and select bots for battle"
                wsClient = component "Server Connection" "WebSocket client to game server"
                replayViewer = component "Replay Viewer" "Load and playback recorded battles"
                configManager = component "Config Manager" "Manage arena settings, game rules"
                embeddedBooter = component "Embedded Booter" "Boots local bots via Booter module"
                embeddedServer = component "Embedded Server" "Runs local server instance"
                # Internal relationships
                user -> controlPanel "Interacts with" "Start/stop/pause"
                user -> botSelector "Selects bots" "Choose participants"
                user -> battleArena "Watches" "Real-time visualization"
                user -> replayViewer "Reviews" "Past battles"
                controlPanel -> wsClient "Send" "Control commands"
                wsClient -> battleArena "Update" "Game state (30 TPS)"
                botSelector -> embeddedBooter "Start" "Selected bots"
                embeddedBooter -> embeddedServer "Connect bots" "WebSocket"
                replayViewer -> battleArena "Render" "Replay frames"
                configManager -> embeddedServer "Configure" "Game rules"
            }
        }
        # External relationships
        guiSystem -> gameServer "WebSocket" "Game state, control"
        guiSystem -> fileSystem "Read/Write" "Bot dirs, replays"
    }
    views {
        component guiContainer "TankRoyale-GUIComponent" "GUI Application Components" {
            include *
            autoLayout
        }
        styles {
            element "Person" {
                background #08427b
                color #ffffff
                shape Person
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Software System" {
                background #1168bd
                color #ffffff
            }
        }
    }
}
