using System.Collections.Generic;

namespace Robocode.TankRoyale.BotApi.Util;

/// <summary>
/// Collection utility class.
/// </summary>
static class CollectionUtil
{
    /// <summary>
    /// Creates a mutable list that is a copy of another list.
    /// </summary>
    /// <param name="list">The list to copy, where <c>null</c> results in returning an empty mutable list.</param>
    /// <returns>A mutable list that is a copy of the input list.</returns>
    internal static IList<T> ToMutableList<T>(IEnumerable<T> list) {
        return list == null ? new List<T>() : new List<T>(list);
    }

    /// <summary>
    /// Creates a mutable set that copies all items from a collection, but is removing duplicates.
    /// </summary>
    /// <param name="collection">The collection to copy, where <c>null</c> results in returning an empty mutable set.
    /// </param>
    /// <returns>A mutable set that is a copy of the input collection.</returns>
    internal static ISet<T> ToMutableSet<T>(ISet<T> collection) {
        return collection == null ? new HashSet<T>() : new HashSet<T>(collection);
    } 
}