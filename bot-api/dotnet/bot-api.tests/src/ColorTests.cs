using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests
{
    public class ColorTests
    {
        [Test]
        [TestCase(0x00, 0x00, 0x00)]
        [TestCase(0xFF, 0xFF, 0xFF)]
        [TestCase(0x13, 0x9A, 0xF7)]
        public void Test1(int red, int green, int blue)
        {
            var color = new Color(red, green, blue);

            Assert.That(color.RedValue, Is.EqualTo(red));
            Assert.That(color.GreenValue, Is.EqualTo(green));
            Assert.That(color.BlueValue, Is.EqualTo(blue));
        }
    }
}