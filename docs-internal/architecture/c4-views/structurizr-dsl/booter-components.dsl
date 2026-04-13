workspace "Robocode Tank Royale - Booter" {
    model {
        # Parent container reference
        booter = softwareSystem "Booter" "Independent artifact for managing bot processes" {
            booterContainer = container "Booter Container" "Core booter functionality" {
                # CLI Layer
                booterCli = component "BooterCli" "Command-line interface entry point" "Kotlin + Clikt"
                bootCli = component "BootCli" "Subcommand for booting bots" "Kotlin + Clikt"
                dirCli = component "DirCli" "Subcommand for listing bot directories" "Kotlin + Clikt"
                infoCli = component "InfoCli" "Subcommand for showing bot info" "Kotlin + Clikt"

                # Core Layer
                processManager = component "ProcessManager" "Creates and manages bot processes" "Kotlin + Java Process API"

                # Model Layer
                bootEntry = component "BootEntry" "Represents bootable bot configuration" "Kotlin data class"
                dirBootEntry = component "DirBootEntry" "Bot entry with directory path" "Kotlin data class"

                # Utility Layer
                dirCommand = component "DirCommand" "Scans directories for bots" "Kotlin + Java NIO"

                # Internal relationships
                booterCli -> bootCli "routes to"
                booterCli -> dirCli "routes to"
                booterCli -> infoCli "routes to"
                bootCli -> processManager "uses"
                dirCli -> dirCommand "uses"
                infoCli -> dirCommand "uses"
                processManager -> bootEntry "creates processes from"
                dirCommand -> dirBootEntry "produces"
            }
        }

        # External systems
        fileSystem = softwareSystem "File System" "Bot directories and configurations"
        botProcess = softwareSystem "Bot Process" "Running bot instance"

        # External relationships
        dirCommand -> fileSystem "reads"
        processManager -> botProcess "creates/manages"
    }

    views {
        component booterContainer "Booter-Components" "Booter Internal Components" {
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

