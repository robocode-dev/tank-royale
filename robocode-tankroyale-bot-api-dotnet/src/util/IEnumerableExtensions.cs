using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi.Util
{
  public static class IEnumerableExtensions
  {
    /// <summary>
    /// Checks if a IEnumerable is null, empty or contains only blank lines.
    /// </summary>
    /// <param name="source"></param>
    /// <returns>true if the IEnumerable is null, empty or contains only blank lines; false otherwise.</returns>
    public static bool IsNullOrEmptyOrContainsBlanks(this IEnumerable<string> source)
    {
      if (source != null)
      {
        foreach (string str in source)
        {
          if (!string.IsNullOrEmpty(str))
          {
            return false;
          }
        }
      }
      return true;
    }

    /// <summary>
    /// Converts the input IEnumerable into a List of string with no blank strings.
    /// </summary>
    /// <param name="source"></param>
    /// <returns>List of string with no blank strings.</returns>
    public static List<string> ToListWithNoBlanks(this IEnumerable<string> source)
    {
      if (source == null)
      {
        return new List<string>();
      }
      var list = new List<string>();
      foreach (string str in source)
      {
        if (str.Trim().Length > 0)
        {
          list.Add(str.Trim());
        }
      }
      return list;
    }
  }
}
