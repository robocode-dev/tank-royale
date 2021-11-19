enableFeaturePreview("VERSION_CATALOGS")

// Schema Generator
include("schema:java")
include("schema:dotnet")

// Booter
include("booter")

// Server
include("server")

// GUI app
include("gui-app")

// Bot API
include("bot-api:java")
include("bot-api:dotnet")

// Sample Bots archives
include("sample-bots:java")
include("sample-bots:dotnet")

// Docs
include("docs")
