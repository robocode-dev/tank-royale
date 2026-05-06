workspace "Robocode Tank Royale Server" {
    model {
        # External clients
        gui = person "GUI" "Visualizes battles, sends control commands" {
            tags "Person"
        }
        bot = person "Bot" "Sends intents, receives ticks" {
            tags "Person"
        }
        recorder = person "Recorder" "Records game events for playback" {
            tags "Person"
        }
        # Server system
        gameServer = softwareSystem "Server" {
            serverContainer = container "Server Container" "Core server functionality" {
                websocketHandler = component "WebSocket Handler" "Routes messages, manages connections (bots, observers, controllers)"
                gameLoopExecutor = component "Game Loop Executor" "30 TPS tick-based loop, drives game simulation"
                physicsEngine = component "Physics Engine" "Movement, collision detection, bullet firing"
                eventDispatcher = component "Event Dispatcher" "Generates and broadcasts game events to observers"
                battleManager = component "Battle Manager" "Battle state machine: ready, running, paused, ended"
                botManager = component "Bot Manager" "Tracks connected bots, handles handshake and intent processing"
                scoreKeeper = component "Score Keeper" "Tracks kills, damage, survival, and calculates rankings"
                # Relationships
                gui -> websocketHandler "WebSocket" "Game state, control commands"
                bot -> websocketHandler "WebSocket" "Handshake, intents"
                recorder -> websocketHandler "WebSocket" "Observe game events"
                websocketHandler -> botManager "Route" "Bot messages"
                websocketHandler -> battleManager "Route" "Control commands"
                botManager -> gameLoopExecutor "Provide" "Bot intents"
                gameLoopExecutor -> physicsEngine "Execute" "Movement and firing"
                physicsEngine -> eventDispatcher "Report" "Collision and hit events"
                eventDispatcher -> websocketHandler "Broadcast" "Game events"
                gameLoopExecutor -> battleManager "Query" "Battle state"
                gameLoopExecutor -> scoreKeeper "Update" "Kill and damage scores"
                battleManager -> eventDispatcher "Trigger" "Battle phase events"
            }
        }
    }
    views {
        component serverContainer "TankRoyale-ServerComponent" "Server Components" {
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
        }
    }
}
