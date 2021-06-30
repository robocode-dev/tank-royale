rootProject.name = "robocode-tankroyale"

// Schema Generator
include("schema-gen:java")

// Booter
include("booter")

// Server
include("server")

// GUI app
include("gui-app")

// Bot API
include("bot-api:java")

// Sample Bots
include("sample-bots:java:Corners")
include("sample-bots:java:Crazy")
include("sample-bots:java:Fire")
include("sample-bots:java:MyFirstBot")
include("sample-bots:java:RamFire")
include("sample-bots:java:SpinBot")
include("sample-bots:java:Target")
include("sample-bots:java:TrackFire")
include("sample-bots:java:Walls")