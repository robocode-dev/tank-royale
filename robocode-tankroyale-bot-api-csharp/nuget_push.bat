dotnet clean
dotnet build
dotnet pack
cd bin\Debug
dotnet nuget push .\Robocode.TankRoyale.BotApi.0.7.1.nupkg -s D:\LocalNugetPackages