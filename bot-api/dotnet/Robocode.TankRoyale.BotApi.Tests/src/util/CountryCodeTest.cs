using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi.Tests.Util
{
    public class CountryCodeTest
    {
        [Test]
        [TestCase("uk")]
        [TestCase("UK")]
        [TestCase("GB")]
        [TestCase("gb")]
        [TestCase("NL")]
        [TestCase("nl")]
        [TestCase("DK")]
        [TestCase("dk")]
        [TestCase("US")]
        [TestCase("us")]
        public void IsCountryCodeValid_ShouldWork(string countryCode)
        {
            Assert.That(CountryCode.IsCountryCodeValid(countryCode), Is.True);
        } 
    }
}