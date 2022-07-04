using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi.Tests.Test_utils;

public class BotInfoBuilder
{
    public BotInfoBuilder(BotInfo botInfo)
    {
        Name = botInfo.Name;
        Version = botInfo.Version;
        Authors = botInfo.Authors;
        Description = botInfo.Description;
        Homepage = botInfo.Homepage;
        CountryCodes = botInfo.CountryCodes;
        GameTypes = botInfo.GameTypes;
        Platform = botInfo.Platform;
        ProgrammingLang = botInfo.ProgrammingLang;
        InitialPosition = botInfo.InitialPosition;
    }

    public BotInfo Build()
    {
        return new BotInfo(Name, Version, Authors, Description, Homepage, CountryCodes, GameTypes, Platform,
            ProgrammingLang, InitialPosition);
    }

    public string Name { get; set; }

    public string Version { get; set; }

    public IEnumerable<string> Authors { get; set; }

    public string Description { get; set; }

    public string Homepage { get; set; }

    public IEnumerable<string> CountryCodes { get; set; }

    public IEnumerable<string> GameTypes { get; set; }

    public string Platform { get; set; }

    public string ProgrammingLang { get; set; }

    public InitialPosition InitialPosition { get; set; }
}