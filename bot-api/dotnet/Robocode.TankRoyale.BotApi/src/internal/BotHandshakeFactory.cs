using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Internal;

internal static class BotHandshakeFactory
{
    internal static BotHandshake Create(string sessionId, BotInfo botInfo, string serverSecret)
    {
        var handshake = new BotHandshake
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotHandshake),
            SessionId = sessionId,
            Name = botInfo.Name,
            TeamId = EnvVars.GetTeamId(),
            TeamName = EnvVars.GetTeamName(),
            Version = botInfo.Version,
            Authors = new List<string>(botInfo.Authors),
            Description = botInfo.Description,
            Homepage = botInfo.Homepage,
            CountryCodes = botInfo.CountryCodes != null
                ? new List<string>(botInfo.CountryCodes)
                : new List<string>(),
            GameTypes = botInfo.GameTypes != null ? new HashSet<string>(botInfo.GameTypes) : new HashSet<string>(),
            Platform = botInfo.Platform,
            ProgrammingLang = botInfo.ProgrammingLang,
            InitialPosition = InitialPositionMapper.Map(botInfo.InitialPosition),
            Secret = serverSecret,
        };
        return handshake;
    }
}