using System;
using System.Drawing;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Tests.Util;

public class ColorUtilTest
{
    [TestFixture]
    public class FromStringUtilTests: ColorUtilTest
    {
        [Test]
        [TestCase("#000000", 0x00, 0x00, 0x00)]
        [TestCase("#000", 0x00, 0x00, 0x00)]
        [TestCase("#FfFfFf", 0xFF, 0xFF, 0xFF)]
        [TestCase("#fFF", 0xFF, 0xFF, 0xFF)]
        [TestCase("#1199cC", 0x11, 0x99, 0xCC)]
        [TestCase("#19C", 0x11, 0x99, 0xCC)]
        [TestCase("  #123456", 0x12, 0x34, 0x56)] // White spaces
        [TestCase("#789aBc\t", 0x78, 0x9A, 0xBC)] // White space
        [TestCase(" #123\t", 0x11, 0x22, 0x33)] // White spaces
        [TestCase("#AbC\t", 0xAA, 0xBB, 0xCC)] // White space
        public void GivenValidRgbString_whenCallingFromString_thenCreatedColorMustContainTheSameRedGreenBlue(string str, int expectedRed, int expectedGreen, int expectedBlue)
        {
            var color = ColorUtil.FromString(str);

            Assert.That(color.Value.R, Is.EqualTo(expectedRed));
            Assert.That(color.Value.G, Is.EqualTo(expectedGreen));
            Assert.That(color.Value.B, Is.EqualTo(expectedBlue));
        }

        [Test]
        [TestCase("#00000")] // Too short
        [TestCase("#0000000")] // Too long
        [TestCase("#0000 00")] // White space
        [TestCase("#xxxxxx")] // Wrong letters
        [TestCase("#abcdeG")] // Wrong letter
        [TestCase("000000")] // Missing hashing sign
        public void GivenInvalidRgbString_whenCallingFromString_thenThrowIllegalArgumentException(string str)
        {
            Assert.Throws<ArgumentException>(() => ColorUtil.FromString(str));
        }
    }

    [TestFixture]
    public class FromHexUtilTests: ColorUtilTest
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
        [TestCase(" 123\t", 0x11, 0x22, 0x33)] // White spaces
        [TestCase("AbC\t", 0xAA, 0xBB, 0xCC)] // White space
        public void GivenValidRgbHexString_whenCallingFromHex_thenCreatedColorMustContainTheSameRedGreenBlue(string hex, int expectedRed, int expectedGreen, int expectedBlue)
        {
            var color = ColorUtil.FromHex(hex);

            Assert.That(color.R, Is.EqualTo(expectedRed));
            Assert.That(color.G, Is.EqualTo(expectedGreen));
            Assert.That(color.B, Is.EqualTo(expectedBlue));
        }

        [Test]
        [TestCase("00000")] // Too short
        [TestCase("0000000")] // Too long
        [TestCase("0000 00")] // White space
        [TestCase("xxxxxx")] // Wrong letters
        [TestCase("abcdeG")] // Wrong letter
        public void GivenInvalidRgbHexString_whenCallingFromHex_thenThrowIllegalArgumentException(string hex)
        {
            Assert.Throws<ArgumentException>(() => ColorUtil.FromHex(hex));
        }
    }

    [TestFixture]
    public class ToHexUtilTests: ColorUtilTest
    {
        [Test]
        [TestCase("000000")]
        [TestCase("FEDCBA")]
        [TestCase("123456")]
        public void GivenValidRgbHexString_whenCallingToHex_thenReturnedHexStringMustBeTheSame(string hex)
        {
            var color = ColorUtil.FromHex(hex);
            Assert.That(ColorUtil.ToHex(color).ToUpper(), Is.EqualTo(hex));
        }
    }

    [TestFixture]
    public class EqualsUtilTests: ColorUtilTest
    {
        [Test]
        public void GivenTwoCreatedColorsWithSameRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustBeEqual()
        {
            Assert.That(Color.FromArgb(10, 20, 30), Is.EqualTo(Color.FromArgb(10, 20, 30)));
            Assert.That(Color.FromArgb(11, 22, 33), Is.EqualTo(Color.FromArgb(11, 22, 33)));
        }

        [Test]
        public void GivenTwoCreatedColorsWithDifferentRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustNotBeEqual()
        {
            Assert.That(Color.FromArgb(10, 20, 30), Is.Not.EqualTo(Color.FromArgb(11, 20, 30)));
            Assert.That(Color.FromArgb(10, 20, 30), Is.Not.EqualTo(Color.FromArgb(10, 22, 30)));
            Assert.That(Color.FromArgb(10, 20, 30), Is.Not.EqualTo(Color.FromArgb(10, 20, 33)));
        }
    }

    [TestFixture]
    public class GetHashCodeUtilTests: ColorUtilTest
    {
        [Test]
        public void GivenTwoEqualColorsCreatedDifferently_whenCallingHashCodeOnEachColor_thenTheHashCodesMustBeEqual()
        {
            Assert.That(ColorUtil.FromString("#102030").GetHashCode(),
                Is.EqualTo(Color.FromArgb(0x10, 0x20, 0x30).GetHashCode()));
            Assert.That(ColorUtil.FromString("#112233").GetHashCode(),
                Is.EqualTo(Color.FromArgb(0x11, 0x22, 0x33).GetHashCode()));
        }

        [Test]
        public void GivenTwoDifferentColors_whenCallingHashCodeOnEachColor_thenTheHashCodesMustNotBeEqual()
        {
            Assert.That(Color.FromArgb(10, 20, 30).GetHashCode(), !Is.EqualTo(ColorUtil.FromString("#123456").GetHashCode()));
        }
    }

    [TestFixture]
    public class ToStringUtilTests: ColorUtilTest
    {
        [Test]
        public void GivenColorWithSpeficHexValue_whenCallingToString_thenReturnedStringMustBeSameHexValue()
        {
            Assert.That(ColorUtil.FromHex("FDB975"), Is.EqualTo(Color.FromArgb(0xFD, 0xB9, 0x75)));
        }
    }
}