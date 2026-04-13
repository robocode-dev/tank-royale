using System;
using System.Collections.Generic;
using System.Diagnostics;
using Robocode.TankRoyale.BotApi.Mapper;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Internal;

static class BotHandshakeFactory
{
    internal static BotHandshake Create(string sessionId, BotInfo botInfo, bool isDroid, string serverSecret)
    {
        var handshake = new BotHandshake
        {
            Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotHandshake),
            SessionId = sessionId,
            Name = botInfo.Name,
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
            TeamId = EnvVars.GetTeamId(),
            TeamName = EnvVars.GetTeamName(),
            TeamVersion = EnvVars.GetTeamVersion(),
            IsDroid = isDroid,
            Secret = serverSecret,
        };

        // Set DebuggerAttached field
        bool debuggerAttached = IsDebuggerAttached();
        handshake.DebuggerAttached = debuggerAttached;

        // Log hint if debugger is detected
        if (debuggerAttached)
        {
            System.Console.WriteLine("Debugger detected. Consider enabling breakpoint mode for this bot in the controller.");
        }

        return handshake;
    }

    /// <summary>
    /// Detects if a debugger is attached to the process.
    /// </summary>
    /// <returns>true if a debugger is attached, false otherwise</returns>
    private static bool IsDebuggerAttached()
    {
        // Check for ROBOCODE_DEBUG environment variable override
        var env = Environment.GetEnvironmentVariable("ROBOCODE_DEBUG");
        if ("true".Equals(env, StringComparison.OrdinalIgnoreCase))
            return true;
        if ("false".Equals(env, StringComparison.OrdinalIgnoreCase))
            return false;

        // Check if a managed debugger is attached
        return Debugger.IsAttached;
    }
}