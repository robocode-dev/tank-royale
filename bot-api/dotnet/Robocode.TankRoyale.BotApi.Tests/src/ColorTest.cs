using System;
using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests
{
    public class ColorTests
    {
        public class ConstructorTests
        {
            [Test]
            [TestCase(0x00, 0x00, 0x00)]
            [TestCase(0xFF, 0xFF, 0xFF)]
            [TestCase(0x13, 0x9A, 0xF7)]
            public void Constructor_ShouldWork(int red, int green, int blue)
            {
                var color = new Color(red, green, blue);

                Assert.That(color.RedValue, Is.EqualTo(red));
                Assert.That(color.GreenValue, Is.EqualTo(green));
                Assert.That(color.BlueValue, Is.EqualTo(blue));
            }

            [Test]
            [TestCase(-1, 70, 100)]    // negative number (1st param)
            [TestCase(50, -100, 100)]  // negative number (2nd param)
            [TestCase(50, 70, -100)]   // negative number (3rd param)
            [TestCase(256, 255, 255)]  // number too big  (1st param)
            [TestCase(255, 1000, 0)]   // number too big  (2nd param)
            [TestCase(50, 100, 300)]   // number too big  (3rd param)
            public void Constructor_ShouldThrowException(int red, int green, int blue)
            {
                Assert.Throws<ArgumentException>(() => new Color(red, green, blue));
            }
        }

        public class FromRgbTests
        {
            [Test]
            [TestCase(0x000000, 0x00, 0x00, 0x00)]
            public void FromRgb_ShouldWork(int rgb, int expectedRed, int expectedGreen, int expectedBlue)
            {
                var color = Color.FromRgb(rgb);

                Assert.That(color.RedValue, Is.EqualTo(expectedRed));
                Assert.That(color.GreenValue, Is.EqualTo(expectedGreen));
                Assert.That(color.BlueValue, Is.EqualTo(expectedBlue));
            }

            [Test]
            public void FromRgb_ShouldReturnNullWhenInputIsNull()
            {
                Assert.That(Color.FromRgb(null), Is.Null);
            }
        }

        public class FromHexTests
        {
            [Test]
            [TestCase("000000", 0x00, 0x00, 0x00)]
            [TestCase("000", 0x00, 0x00, 0x00)]
            [TestCase("FfFfFf", 0xFF, 0xFF, 0xFF)]
            [TestCase("fFF", 0xFF, 0xFF, 0xFF)]
            [TestCase("1199cC", 0x11, 0x99, 0xCC)]
            [TestCase("19C", 0x11, 0x99, 0xCC)]
            [TestCase("  123456", 0x12, 0x34, 0x56)] // White spaces
            [TestCase("789aBc\t", 0x78, 0x9A, 0xBC)] // White space
            [TestCase(" 123\t", 0x11, 0x22, 0x33)]   // White spaces
            [TestCase("AbC\t", 0xAA, 0xBB, 0xCC)]    // White space
            public void FromHex_ShouldWork(string hex, int expectedRed, int expectedGreen, int expectedBlue)
            {
                var color = Color.FromHex(hex);

                Assert.That(color.RedValue, Is.EqualTo(expectedRed));
                Assert.That(color.GreenValue, Is.EqualTo(expectedGreen));
                Assert.That(color.BlueValue, Is.EqualTo(expectedBlue));
            }

            [Test]
            [TestCase("00000")] // Too short
            [TestCase("0000000")] // Too long
            [TestCase("0000 00")] // White space
            [TestCase("xxxxxx")] // Wrong letters
            [TestCase("abcdeG")] // Wrong letter
            public void FromHex_ShouldThrowException(string hex)
            {
                Assert.Throws<ArgumentException>(() => Color.FromHex(hex));
            }
        }

        public class ToHexTests
        {
            [Test]
            [TestCase("000000")]
            [TestCase("FEDCBA")]
            [TestCase("123456")]
            public void ToHex_ShouldWork(string hex)
            {
                var color = Color.FromHex(hex);
                Assert.That(color.ToHex().ToUpper(), Is.EqualTo(hex));
            }
        }

        public class EqualsTests
        {
            [Test]
            public void Equals_ShouldBeEqual()
            {
                Assert.That(new Color(10, 20, 30), Is.EqualTo(new Color(10, 20, 30)));
                Assert.That(new Color(11, 22, 33), Is.EqualTo(new Color(11, 22, 33)));
            }

            [Test]
            public void Equals_ShouldNotBeEqual()
            {
                Assert.That(new Color(10, 20, 30), !Is.EqualTo(new Color(11, 20, 30)));
                Assert.That(new Color(10, 20, 30), !Is.EqualTo(new Color(10, 22, 30)));
                Assert.That(new Color(10, 20, 30), !Is.EqualTo(new Color(10, 20, 33)));
            }
        }

        public class GetHashCodeTests
        {
            [Test]
            public void GetHashCode_ShouldBeEqual()
            {
                Assert.That(Color.FromRgb(0x102030).GetHashCode(),
                    Is.EqualTo(new Color(0x10, 0x20, 0x30).GetHashCode()));
                Assert.That(Color.FromRgb(0x112233).GetHashCode(),
                    Is.EqualTo(new Color(0x11, 0x22, 0x33).GetHashCode()));
            }

            [Test]
            public void GetHashCode_ShouldNotBeEqual()
            {
                Assert.That(new Color(10, 20, 30).GetHashCode(), !Is.EqualTo(Color.FromRgb(0x123456).GetHashCode()));
            }
        }

        public class ToStringTests
        {
            [Test]
            public void ToString_ShouldBeEqual()
            {
                Assert.That(Color.FromHex("FDB975").ToString().ToUpper(), Is.EqualTo("FDB975"));
            }
        }

        public class ColorConstantsTests
        {
            [Test]
            public void White()
            {
                Assert.That(Color.White.ToString().ToUpper(), Is.EqualTo("FFFFFF"));
            }

            [Test]
            public void Silver()
            {
                Assert.That(Color.Silver.ToString().ToUpper(), Is.EqualTo("C0C0C0"));
            }

            [Test]
            public void Gray()
            {
                Assert.That(Color.Gray.ToString().ToUpper(), Is.EqualTo("808080"));
            }

            [Test]
            public void Black()
            {
                Assert.That(Color.Black.ToString().ToUpper(), Is.EqualTo("000000"));
            }

            [Test]
            public void Red()
            {
                Assert.That(Color.Red.ToString().ToUpper(), Is.EqualTo("FF0000"));
            }

            [Test]
            public void Maroon()
            {
                Assert.That(Color.Maroon.ToString().ToUpper(), Is.EqualTo("800000"));
            }

            [Test]
            public void Yellow()
            {
                Assert.That(Color.Yellow.ToString().ToUpper(), Is.EqualTo("FFFF00"));
            }

            [Test]
            public void Olive()
            {
                Assert.That(Color.Olive.ToString().ToUpper(), Is.EqualTo("808000"));
            }

            [Test]
            public void Lime()
            {
                Assert.That(Color.Lime.ToString().ToUpper(), Is.EqualTo("00FF00"));
            }

            [Test]
            public void Green()
            {
                Assert.That(Color.Green.ToString().ToUpper(), Is.EqualTo("008000"));
            }

            [Test]
            public void Cyan()
            {
                Assert.That(Color.Cyan.ToString().ToUpper(), Is.EqualTo("00FFFF"));
            }

            [Test]
            public void Teal()
            {
                Assert.That(Color.Teal.ToString().ToUpper(), Is.EqualTo("008080"));
            }

            [Test]
            public void Blue()
            {
                Assert.That(Color.Blue.ToString().ToUpper(), Is.EqualTo("0000FF"));
            }

            [Test]
            public void Navy()
            {
                Assert.That(Color.Navy.ToString().ToUpper(), Is.EqualTo("000080"));
            }

            [Test]
            public void Fuchsia()
            {
                Assert.That(Color.Fuchsia.ToString().ToUpper(), Is.EqualTo("FF00FF"));
            }

            [Test]
            public void Purple()
            {
                Assert.That(Color.Purple.ToString().ToUpper(), Is.EqualTo("800080"));
            }

            [Test]
            public void Orange()
            {
                Assert.That(Color.Orange.ToString().ToUpper(), Is.EqualTo("FF8000"));
            }
        }
    }
}