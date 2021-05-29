dotnet clean
rem dotnet build
dotnet pack
cd .\bin\Debug
dotnet nuget push .\Robocode.TankRoyale.BotApi.0.9.8.nupkg -s D:\LocalNugetPackages