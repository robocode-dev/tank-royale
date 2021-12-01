# ReadMe - Nuget Packaging

### Do this once

Update this file:

    %appdata%\NuGet\NuGet.Config

Add this key inside <packageSources> section:

    <add key="local" value="%USERPROFILE%\.nuget\packages"/>

### Build bot-api

    gradle build

### Push Robocode.TankRoyale.BotApi to local

    cd bin\Release
    dotnet nuget push *.nupkg -s local
