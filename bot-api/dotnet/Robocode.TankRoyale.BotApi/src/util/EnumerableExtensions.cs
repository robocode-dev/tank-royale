using System.Collections.Generic;
using System.Linq;

namespace Robocode.TankRoyale.BotApi.Util;

public static class EnumerableExtensions
{
    /// <summary>
    /// Checks if a IEnumerable is null, empty or contains only blank lines.
    /// </summary>
    /// <param name="source"></param>
    /// <returns>true if the IEnumerable is null, empty or contains only blank lines; false otherwise.</returns>
    public static bool IsNullOrEmptyOrContainsOnlyBlanks(this IEnumerable<string> source)
    {
        return source == null || source.All(string.IsNullOrWhiteSpace);
    }

    /// <summary>
    /// Converts the input IEnumerable into a List of string with no blank strings.
    /// </summary>
    /// <param name="source"></param>
    /// <returns>List of string with no blank strings.</returns>
    public static List<string> ToListWithNoBlanks(this IEnumerable<string> source)
    {
        return source == null
            ? new List<string>()
            : (from str in source where str.Trim().Length > 0 select str.Trim()).ToList();
    }
}