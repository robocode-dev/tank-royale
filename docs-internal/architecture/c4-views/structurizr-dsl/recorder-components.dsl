workspace "Robocode Tank Royale - Recorder" {
    model {
        # Recorder system
        recorderSystem = softwareSystem "Recorder" "Independent artifact for recording game events" {
            recorderContainer = container "Recorder Container" "Standalone JAR or embedded component" {
                # CLI Layer
                recorderCli = component "RecorderCli" "Command-line interface entry point" "Kotlin + Clikt"

                # Core Layer
                recordingObserver = component "RecordingObserver" "WebSocket client connecting as game observer" "Kotlin + WebSocket"
                gameRecorder = component "GameRecorder" "Writes game events to ND-JSON files" "Kotlin + GZIP"

                # Internal relationships
                recorderCli -> recordingObserver "configures and starts"
                recordingObserver -> gameRecorder "routes events to"
            }
        }

        # External systems
        gameServer = softwareSystem "Server" "Runs battles and broadcasts events"
        fileSystem = softwareSystem "File System" "Storage for recorded battle files"

        # External relationships
        recordingObserver -> gameServer "WebSocket (observer mode)" "Receives game events"
        gameRecorder -> fileSystem "writes" "ND-JSON + GZIP"
    }

    views {
        component recorderContainer "Recorder-Components" "Recorder Internal Components" {
            include *
            autoLayout
        }

        styles {
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

