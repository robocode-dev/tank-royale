using System;
using NUnit.Framework;

namespace Robocode.TankRoyale.BotApi.Tests;

public class ColorTest
{
    [TestFixture]
    public class ConstructorTests: ColorTest
    {
        [Test]
        [TestCase(0x00, 0x00, 0x00)]
        [TestCase(0xFF, 0xFF, 0xFF)]
        [TestCase(0x13, 0x9A, 0xF7)]
        public void GivenValidRedGreenBlue_whenCreatingColor_thenCreatedColorMustContainTheSameRedGreenBlue(int red, int green, int blue)
        {
            var color = new Color(red, green, blue);

            Assert.That(color.RedValue, Is.EqualTo(red));
            Assert.That(color.GreenValue, Is.EqualTo(green));
            Assert.That(color.BlueValue, Is.EqualTo(blue));
        }

        [Test]
        [TestCase(-1, 70, 100)] // negative number (1st param)
        [TestCase(50, -100, 100)] // negative number (2nd param)
        [TestCase(50, 70, -100)] // negative number (3rd param)
        [TestCase(256, 255, 255)] // number too big  (1st param)
        [TestCase(255, 1000, 0)] // number too big  (2nd param)
        [TestCase(50, 100, 300)] // number too big  (3rd param)
        public void GivenInvalidRedGreenBlue_whenCreatingColor_thenThrowIllegalArgumentException(int red, int green, int blue)
        {
            Assert.Throws<ArgumentException>(() => new Color(red, green, blue));
        }
    }

    [TestFixture]
    public class FromStringTests: ColorTest
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
            var color = Color.FromString(str);

            Assert.That(color.RedValue, Is.EqualTo(expectedRed));
            Assert.That(color.GreenValue, Is.EqualTo(expectedGreen));
            Assert.That(color.BlueValue, Is.EqualTo(expectedBlue));
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
            Assert.Throws<ArgumentException>(() => Color.FromString(str));
        }
    }

    [TestFixture]
    public class FromHexTests: ColorTest
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
        public void GivenInvalidRgbHexString_whenCallingFromHex_thenThrowIllegalArgumentException(string hex)
        {
            Assert.Throws<ArgumentException>(() => Color.FromHex(hex));
        }
    }

    [TestFixture]
    public class ToHexTests: ColorTest
    {
        [Test]
        [TestCase("000000")]
        [TestCase("FEDCBA")]
        [TestCase("123456")]
        public void GivenValidRgbHexString_whenCallingToHex_thenReturnedHexStringMustBeTheSame(string hex)
        {
            var color = Color.FromHex(hex);
            Assert.That(color.ToHex().ToUpper(), Is.EqualTo(hex));
        }
    }

    [TestFixture]
    public class EqualsTests: ColorTest
    {
        [Test]
        public void GivenTwoCreatedColorsWithSameRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustBeEqual()
        {
            Assert.That(new Color(10, 20, 30), Is.EqualTo(new Color(10, 20, 30)));
            Assert.That(new Color(11, 22, 33), Is.EqualTo(new Color(11, 22, 33)));
        }

        [Test]
        public void GivenTwoCreatedColorsWithDifferentRgbValues_whenCallingIsEqualTo_thenTheTwoColorsMustNotBeEqual()
        {
            Assert.That(new Color(10, 20, 30), Is.Not.EqualTo(new Color(11, 20, 30)));
            Assert.That(new Color(10, 20, 30), Is.Not.EqualTo(new Color(10, 22, 30)));
            Assert.That(new Color(10, 20, 30), Is.Not.EqualTo(new Color(10, 20, 33)));
        }
    }

    [TestFixture]
    public class GetHashCodeTests: ColorTest
    {
        [Test]
        public void GivenTwoEqualColorsCreatedDifferently_whenCallingHashCodeOnEachColor_thenTheHashCodesMustBeEqual()
        {
            Assert.That(Color.FromString("#102030").GetHashCode(),
                Is.EqualTo(new Color(0x10, 0x20, 0x30).GetHashCode()));
            Assert.That(Color.FromString("#112233").GetHashCode(),
                Is.EqualTo(new Color(0x11, 0x22, 0x33).GetHashCode()));
        }

        [Test]
        public void GivenTwoDifferentColors_whenCallingHashCodeOnEachColor_thenTheHashCodesMustNotBeEqual()
        {
            Assert.That(new Color(10, 20, 30).GetHashCode(), !Is.EqualTo(Color.FromString("#123456").GetHashCode()));
        }
    }

    [TestFixture]
    public class ToStringTests: ColorTest
    {
        [Test]
        public void GivenColorWithSpeficHexValue_whenCallingToString_thenReturnedStringMustBeSameHexValue()
        {
            Assert.That(Color.FromHex("FDB975").ToString().ToUpper(), Is.EqualTo("FDB975"));
        }
    }

    [TestFixture]
    public class ColorConstantsTests: ColorTest
    {
        [Test]
        public void GivenWhiteColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.White.ToString().ToUpper(), Is.EqualTo("FFFFFF"));
        }

        [Test]
        public void GivenSilverColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Silver.ToString().ToUpper(), Is.EqualTo("C0C0C0"));
        }

        [Test]
        public void GivenGrayColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Gray.ToString().ToUpper(), Is.EqualTo("808080"));
        }

        [Test]
        public void GivenBlackColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Black.ToString().ToUpper(), Is.EqualTo("000000"));
        }

        [Test]
        public void GivenRedColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Red.ToString().ToUpper(), Is.EqualTo("FF0000"));
        }

        [Test]
        public void GivenMaroonColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Maroon.ToString().ToUpper(), Is.EqualTo("800000"));
        }

        [Test]
        public void GivenYellowColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Yellow.ToString().ToUpper(), Is.EqualTo("FFFF00"));
        }

        [Test]
        public void GivenOliveColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Olive.ToString().ToUpper(), Is.EqualTo("808000"));
        }

        [Test]
        public void GivenLimeColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Lime.ToString().ToUpper(), Is.EqualTo("00FF00"));
        }

        [Test]
        public void GivenGreenColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Green.ToString().ToUpper(), Is.EqualTo("008000"));
        }

        [Test]
        public void GivenCyanColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Cyan.ToString().ToUpper(), Is.EqualTo("00FFFF"));
        }

        [Test]
        public void GivenTealColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Teal.ToString().ToUpper(), Is.EqualTo("008080"));
        }

        [Test]
        public void GivenBlueColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Blue.ToString().ToUpper(), Is.EqualTo("0000FF"));
        }

        [Test]
        public void GivenNavyColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Navy.ToString().ToUpper(), Is.EqualTo("000080"));
        }

        [Test]
        public void GivenFuchsiaColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Fuchsia.ToString().ToUpper(), Is.EqualTo("FF00FF"));
        }

        [Test]
        public void GivenPurpleColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Purple.ToString().ToUpper(), Is.EqualTo("800080"));
        }

        [Test]
        public void GivenOrangeColorConstant_whenGettingTheColorValue_thenTheColorValueIsCorrect()
        {
            Assert.That(Color.Orange.ToString().ToUpper(), Is.EqualTo("FF8000"));
        }
    }
}