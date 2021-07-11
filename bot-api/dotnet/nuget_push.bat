dotnet clean
dotnet build
cd .\bin\Debug
dotnet nuget push .\Robocode.TankRoyale.BotApi.0.9.8.nupkg -s D:\LocalNugetPackages