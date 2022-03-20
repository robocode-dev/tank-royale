using System;
using System.Globalization;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;

[assembly: InternalsVisibleTo("Robocode.TankRoyale.BotApi.Tests")]
namespace Robocode.TankRoyale.BotApi
{
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
        /// <param name="angle">The angle, where <c>null</c> means it is random.</param>
        internal InitialPosition(double? x, double? y, double? angle)
        {
            X = x;
            Y = y;
            Angle = angle;
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
        /// The Angle coordinate.
        /// </summary>
        public double? Angle { get; }

        /// <inheritdoc/>
        public override string ToString()
        {
            return string.Format(CultureInfo.InvariantCulture, "{0},{1},{2}", X, Y, Angle);
        }

        public static InitialPosition FromString(string initialPosition)
        {
            if (initialPosition == null || string.IsNullOrWhiteSpace(initialPosition)) return null;
            var values = Regex.Split(initialPosition.Trim(), @"\s*,\s*|\s+");
            return ParseInitialPosition(values);
        }

        private static InitialPosition ParseInitialPosition(string[] values)
        {
            if (values.Length < 1) return null;

            var x = ParseDouble(values[0]);
            if (values.Length < 2)
            {
                return new InitialPosition(x, null, null);
            }
            var y = ParseDouble(values[1]);
            double? angle = null;
            if (values.Length >= 3)
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
}