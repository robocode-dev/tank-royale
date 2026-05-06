workspace "Robocode Tank Royale Bot API" {
    model {
        # External
        developer = person "Bot Developer" "Writes custom bot AI"
        server = softwareSystem "Server" "Robocode Tank Royale server"

        # Bot API system
        botApi = softwareSystem "Bot API" {
            apiContainer = container "Bot API Container" "Core API functionality" {
                baseBot = component "BaseBot" "Abstract base class for bots"
                botInternals = component "BotInternals" "Hidden state management"
                websocketHandler = component "WebSocket Handler" "Network communication"
                eventMapper = component "Event Mapper" "Deserialize server events"
                intentBuilder = component "Intent Builder" "Serialize bot actions"
                radar = component "Radar" "Scanning system"
                stateManager = component "State Manager" "Game state tracking"

                # Relationships
                developer -> baseBot "Implements/Uses"
                baseBot -> botInternals "Manages"
                baseBot -> radar "Uses for scanning"
                baseBot -> stateManager "Reads state from"
                websocketHandler -> eventMapper "Route messages"
                eventMapper -> baseBot "Callback to"
                baseBot -> intentBuilder "Send actions"
                intentBuilder -> websocketHandler "Send intent"
                stateManager -> botInternals "Updates"
            }
        }

        # External connections
        developer -> botApi "Uses"
        botApi -> server "WebSocket"
    }

    views {
        component apiContainer "TankRoyale-BotAPIComponent" "Bot API Components" {
            include *
            autoLayout
        }

        styles {
            element "Person" {
                shape "Person"
                background "#08427b"
                color "#ffffff"
            }
        }
    }
}
