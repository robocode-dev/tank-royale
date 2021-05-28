dotnet clean
rem dotnet build
dotnet pack
rem dotnet pack
rem cd .\bin\Release\
cd .\bin\Debug
dotnet nuget push .\Robocode.TankRoyale.BotApi.0.9.8.nupkg -s D:\LocalNugetPackages