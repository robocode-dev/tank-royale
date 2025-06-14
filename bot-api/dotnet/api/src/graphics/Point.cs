using System;
using System.Globalization;
using JetBrains.Annotations;

namespace Robocode.TankRoyale.BotApi.Graphics;

/// <summary>
/// Represents an ordered pair of x and y coordinates that define a point in a two-dimensional plane.
/// </summary>
[PublicAPI]
public readonly struct Point : IEquatable<Point>
{
    /// <summary>
    /// Gets the x-coordinate of this Point.
    /// </summary>
    public readonly double X { get; }

    /// <summary>
    /// Gets the y-coordinate of this Point.
    /// </summary>
    public readonly double Y { get; }

    /// <summary>
    /// Initializes a new instance of the Point structure with the specified coordinates.
    /// </summary>
    /// <param name="x">The x-coordinate of the point.</param>
    /// <param name="y">The y-coordinate of the point.</param>
    public Point(double x, double y)
    {
        X = x;
        Y = y;
    }

    /// <summary>
    /// Determines whether the specified object is equal to the current Point.
    /// </summary>
    /// <param name="obj">The object to compare with the current Point.</param>
    /// <returns>true if the specified object is equal to the current Point; otherwise, false.</returns>
    public override bool Equals(object obj)
    {
        return obj is Point point && Equals(point);
    }

    /// <summary>
    /// Determines whether the specified Point is equal to the current Point.
    /// </summary>
    /// <param name="other">The Point to compare with the current Point.</param>
    /// <returns>true if the specified Point is equal to the current Point; otherwise, false.</returns>
    public bool Equals(Point other)
    {
        const double epsilon = 1e-10; // Define a tolerance for floating-point comparisons
        return Math.Abs(X - other.X) < epsilon && Math.Abs(Y - other.Y) < epsilon;
    }

    /// <summary>
    /// Returns the hash code for this Point.
    /// </summary>
    /// <returns>A hash code for the current Point.</returns>
    public override int GetHashCode()
    {
        return HashCode.Combine(X, Y);
    }

    /// <summary>
    /// Returns a string that represents the current Point.
    /// </summary>
    /// <returns>A string that represents the current Point.</returns>
    public override string ToString()
    {
        return $"({X.ToString(CultureInfo.InvariantCulture)}, {Y.ToString(CultureInfo.InvariantCulture)})";
    }

    /// <summary>
    /// Determines whether two Point objects are equal.
    /// </summary>
    /// <param name="left">The first Point to compare.</param>
    /// <param name="right">The second Point to compare.</param>
    /// <returns>true if the points are equal; otherwise, false.</returns>
    public static bool operator ==(Point left, Point right)
    {
        return left.Equals(right);
    }

    /// <summary>
    /// Determines whether two Point objects are not equal.
    /// </summary>
    /// <param name="left">The first Point to compare.</param>
    /// <param name="right">The second Point to compare.</param>
    /// <returns>true if the points are not equal; otherwise, false.</returns>
    public static bool operator !=(Point left, Point right)
    {
        return !(left == right);
    }
}