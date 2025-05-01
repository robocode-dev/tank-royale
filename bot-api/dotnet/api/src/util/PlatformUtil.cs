using System.Reflection;
using System.Runtime.Versioning;

namespace Robocode.TankRoyale.BotApi.Util;

/// <summary>
/// Collection utility class.
/// </summary>
static class PlatformUtil
{
    /// <summary>
    /// Returns the name of the platform (framework).
    /// </summary>
    /// <returns>A string containing the platform name.</returns>
    internal static string GetPlatformName() =>
        Assembly.GetEntryAssembly()?.GetCustomAttribute<TargetFrameworkAttribute>()?.FrameworkName;
}