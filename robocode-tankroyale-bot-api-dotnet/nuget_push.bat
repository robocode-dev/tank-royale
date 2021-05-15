dotnet clean
dotnet build --configuration Release
dotnet pack --configuration Release
cd .\bin\Release\
dotnet nuget push .\Robocode.TankRoyale.BotApi.0.9.6.nupkg -s D:\LocalNugetPackages