using System;
using System.Collections.Generic;
using System.Globalization;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;

[assembly: InternalsVisibleTo("Robocode.TankRoyale.BotApi.Tests")]

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Initial starting position containing a start coordinate (x,y) and angle.
///
/// The initial position is only used when debugging to request the server to let a bot start at a specific position.
/// Note that initial starting positions must be enabled at the server-side; otherwise the initial starting position
/// is ignored.
/// </summary>
public sealed class InitialPosition
{
    /// <summary>
    /// Initializes a new instance of the InitialPosition class.
    /// </summary>
    /// <param name="x">The x coordinate, where <c>null</c> means it is random.</param>
    /// <param name="y">The x coordinate, where <c>null</c> means it is random.</param>
    /// <param name="direction">The shared direction of the body, gun, and radar, where <c>null</c> means it is random.</param>
    public InitialPosition(double? x, double? y, double? direction)
    {
        X = x;
        Y = y;
        Direction = direction;
    }

    private bool Equals(InitialPosition other)
    {
        return Nullable.Equals(X, other.X) && Nullable.Equals(Y, other.Y) && Nullable.Equals(Direction, other.Direction);
    }

    public override bool Equals(object obj)
    {
        return ReferenceEquals(this, obj) || obj is InitialPosition other && Equals(other);
    }

    public override int GetHashCode()
    {
        return HashCode.Combine(X, Y, Direction);
    }

    /// <summary>
    /// The x coordinate.
    /// </summary>
    public double? X { get; }

    /// <summary>
    /// The Y coordinate.
    /// </summary>
    public double? Y { get; }

    /// <summary>
    /// The shared direction of the body, gun, and radar.
    /// </summary>
    public double? Direction { get; }

    public override string ToString()
    {
        return string.Format(CultureInfo.InvariantCulture, "{0},{1},{2}", X, Y, Direction);
    }

    public static InitialPosition FromString(string initialPosition)
    {
        if (initialPosition == null || string.IsNullOrWhiteSpace(initialPosition)) return null;
        var values = Regex.Split(initialPosition.Trim(), @"\s*,\s*|\s+");
        return ParseInitialPosition(values);
    }

    private static InitialPosition ParseInitialPosition(IReadOnlyList<string> values)
    {
        if (values.Count < 1) return null;

        var x = ParseDouble(values[0]);
        if (values.Count < 2)
        {
            return new InitialPosition(x, null, null);
        }

        var y = ParseDouble(values[1]);
        double? angle = null;
        if (values.Count >= 3)
        {
            angle = ParseDouble(values[2]);
        }

        if (x == null && y == null && angle == null) return null;

        return new InitialPosition(x, y, angle);
    }

    private static double? ParseDouble(string str)
    {
        if (str == null) return null;
        try
        {
            return double.Parse(str.Trim(), CultureInfo.InvariantCulture);
        }
        catch (FormatException)
        {
            return null;
        }
    }
}