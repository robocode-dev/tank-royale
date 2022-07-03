using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Tests.Util
{
    public class CountryCodeTest
    {
        [Test]
        [TestCase("GB")]
        [TestCase("gb")]
        [TestCase("dk")]
        [TestCase("us")]
        [TestCase("no")]
        [TestCase("SE")]
        [TestCase("FI")]
        public void IsCountryCodeValid_ReturnsTrueForGivenCountryCodes(string countryCode)
        {
            Assert.That(CountryCode.IsCountryCodeValid(countryCode), Is.True);
        }

        [Test]
        public void GetLocalCountryCode_ReturnValidCountryCode()
        {
            var countryCode = CountryCode.GetLocalCountryCode();
            Assert.That(CountryCode.IsCountryCodeValid(countryCode), Is.True);
        }
    }
}