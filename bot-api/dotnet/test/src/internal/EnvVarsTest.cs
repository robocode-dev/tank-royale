using System.Linq;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal;
using Robocode.TankRoyale.BotApi.Util;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariables;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

class EnvVarsTest
{
    const string MissingEnvVarText = "Missing environment variable: ";

    [SetUp]
    public void SetUp()
    {
        SetAllEnvVarsToDefaultValues();
    }

    [TestFixture]
    class GetBotInfo : EnvVarsTest
    {
        [TestFixture]
        class Name : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetName_thenReturnedNameIsTheSame()
            {
                SetEnvVar(BotName, "MyBot");
                Assert.That(EnvVars.GetBotInfo().Name, Is.EqualTo("MyBot"));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetName_thenThrowBotExceptionWithMissingEnvInfo()
            {
                ClearEnvVar(BotName);
                var exception = Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
                Assert.That(exception?.Message, Is.EqualTo(MissingEnvVarText + BotName));
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetName_thenThrowBotExceptionWithMissingEnvInfo()
            {
                SetEnvVar(BotName, "  \t");
                var exception = Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
                Assert.That(exception?.Message, Is.EqualTo(MissingEnvVarText + BotName));
            }
        }

        [TestFixture]
        class Version : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetVersion_thenReturnedVersionIsTheSame()
            {
                SetEnvVar(BotName, "1.0");
                Assert.That(EnvVars.GetBotInfo().Name, Is.EqualTo("1.0"));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetVersion_thenThrowBotExceptionWithMissingEnvInfo()
            {
                ClearEnvVar(BotVersion);
                var exception = Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
                Assert.That(exception?.Message, Is.EqualTo(MissingEnvVarText + BotVersion));
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetVersion_thenThrowBotExceptionWithMissingEnvInfo()
            {
                SetEnvVar(BotVersion, "  \t");
                var exception = Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
                Assert.That(exception?.Message, Is.EqualTo(MissingEnvVarText + BotVersion));
            }
        }

        [TestFixture]
        class Authors : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetAuthors_thenReturnedListContainsSameAuthors()
            {
                SetEnvVar(BotAuthors, "Author 1, Author 2");
                Assert.That(EnvVars.GetBotInfo().Authors, Is.EquivalentTo(new[] { "Author 1", "Author 2" }));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetAuthors_thenThrowBotExceptionWithMissingEnvInfo()
            {
                ClearEnvVar(BotAuthors);
                var exception = Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
                Assert.That(exception?.Message, Is.EqualTo(MissingEnvVarText + BotAuthors));
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetAuthors_thenThrowBotExceptionWithMissingEnvInfo()
            {
                SetEnvVar(BotAuthors, "  \t");
                var exception = Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
                Assert.That(exception?.Message, Is.EqualTo(MissingEnvVarText + BotAuthors));
            }
        }

        [TestFixture]
        class GameTypes : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetGameTypes_thenReturnedListContainsSameGameTypes()
            {
                SetEnvVar(BotGameTypes, "classic, 1v1, melee");
                Assert.That(EnvVars.GetBotInfo().GameTypes, Is.EquivalentTo(new[] { "classic", "1v1", "melee" }));
            }
        }

        [TestFixture]
        class Description : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetProgrammingLang_thenReturnedDescriptionIsTheSame()
            {
                SetEnvVar(BotDescription, "description");
                Assert.That(EnvVars.GetBotInfo().Description, Is.EqualTo("description"));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetProgrammingLang_thenReturnNull()
            {
                ClearEnvVar(BotDescription);
                Assert.That(EnvVars.GetBotInfo().Description, Is.Null);
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetProgrammingLang_thenReturnNull()
            {
                SetEnvVar(BotDescription, "  \t");
                Assert.That(EnvVars.GetBotInfo().Description, Is.Null);
            }
        }

        [TestFixture]
        class Homepage : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetHomepage_thenReturnedHomepageIsTheSame()
            {
                SetEnvVar(BotHomepage, "https://robocode.dev");
                Assert.That(EnvVars.GetBotInfo().Homepage, Is.EqualTo("https://robocode.dev"));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetHomepage_thenReturnNull()
            {
                ClearEnvVar(BotHomepage);
                Assert.That(EnvVars.GetBotInfo().Homepage, Is.Null);
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetHomepage_thenReturnNull()
            {
                SetEnvVar(BotHomepage, "  \t");
                Assert.That(EnvVars.GetBotInfo().Homepage, Is.Null);
            }
        }

        [TestFixture]
        class CountryCodes : GetBotInfo
        {
            [Test]
            public void
                GivenValidEnvVar_whenCallingGetCountryCodes_thenReturnedListContainsSameCountryCodesInUpperCase()
            {
                SetEnvVar(BotCountryCodes, "dk, us");
                var countryCode = EnvVars.GetBotInfo().CountryCodes;
                Assert.That(countryCode, Is.EquivalentTo(new[] { "DK", "US" }));
            }

            [Test]
            public void GivenInvalidEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode()
            {
                SetEnvVar(BotCountryCodes, "xyz");
                var countryCode = EnvVars.GetBotInfo().CountryCodes.First();
                Assert.That(countryCode, Is.EqualTo(CountryCode.GetLocalCountryCode()));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode()
            {
                ClearEnvVar(BotCountryCodes);
                var countryCode = EnvVars.GetBotInfo().CountryCodes;
                Assert.That(countryCode, Is.EquivalentTo(new[] { CountryCode.GetLocalCountryCode() }));
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode()
            {
                SetEnvVar(BotCountryCodes, "  \t");
                var countryCode = EnvVars.GetBotInfo().CountryCodes;
                Assert.That(countryCode, Is.EquivalentTo(new[] { CountryCode.GetLocalCountryCode() }));
            }
        }

        [TestFixture]
        class Platform : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetPlatform_thenReturnedPlatformIsTheSame()
            {
                SetEnvVar(BotPlatform, ".Net");
                Assert.That(EnvVars.GetBotInfo().Platform, Is.EqualTo(".Net"));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetPlatform_thenReturnedPlatformMustContainDotnet()
            {
                ClearEnvVar(BotPlatform);
                Assert.That(EnvVars.GetBotInfo().Platform.Contains(".NETCoreApp"), Is.True);
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetPlatform_thenReturnedPlatformMustContainDotnet()
            {
                SetEnvVar(BotPlatform, "  \t");
                Assert.That(EnvVars.GetBotInfo().Platform.Contains(".NETCoreApp"), Is.True);
            }
        }

        [TestFixture]
        class ProgrammingLang : GetBotInfo
        {
            [Test]
            public void GivenValidEnvVar_whenCallingGetProgrammingLang_thenReturnedProgrammingLangIsTheSame()
            {
                SetEnvVar(BotProgrammingLang, "C# 11");
                Assert.That(EnvVars.GetBotInfo().ProgrammingLang, Is.EqualTo("C# 11"));
            }

            [Test]
            public void GivenMissingEnvVar_whenCallingGetProgrammingLang_thenReturnNull()
            {
                ClearEnvVar(BotProgrammingLang);
                Assert.That(EnvVars.GetBotInfo().ProgrammingLang, Is.Null);
            }

            [Test]
            public void GivenBlankEnvVar_whenCallingGetProgrammingLang_thenReturnNull()
            {
                SetEnvVar(BotProgrammingLang, "  \t");
                Assert.That(EnvVars.GetBotInfo().ProgrammingLang, Is.Null);
            }
        }

        [TestFixture]
        class InitialPosition : GetBotInfo
        {
            [Test]
            public void GivenValidPositionEnvVar_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame()
            {
                SetEnvVar(BotInitialPosition, "50, 100, 45");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.EqualTo(100));
                Assert.That(pos.Direction, Is.EqualTo(45));
            }

            [Test]
            public void
                GivenValidXCoordinateOnly_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXCoordinate()
            {
                SetEnvVar(BotInitialPosition, "  50 ");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.Null);
                Assert.That(pos.Direction, Is.Null);
            }

            [Test]
            public void
                GivenValidXCoordinateWithCommaAfter_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXCoordinate()
            {
                SetEnvVar(BotInitialPosition, "  50, ");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.Null);
                Assert.That(pos.Direction, Is.Null);
            }

            [Test]
            public void
                GivenValidXAndYCoordinateOnlyWithNoCommaInBetween_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate()
            {
                SetEnvVar(BotInitialPosition, "  50 70.0");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.EqualTo(70));
                Assert.That(pos.Direction, Is.Null);
            }

            [Test]
            public void
                GivenValidXAndYCoordinateOnlyWithACommaInBetween_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate()
            {
                SetEnvVar(BotInitialPosition, "  50.0, 70");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.EqualTo(70));
                Assert.That(pos.Direction, Is.Null);
            }

            [Test]
            public void
                GivenValidXAndYCoordinateOnlyWithACommaInBetweenAndAfter_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate()
            {
                SetEnvVar(BotInitialPosition, "  50, 70.0 ,");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.EqualTo(70));
                Assert.That(pos.Direction, Is.Null);
            }

            [Test]
            public void
                GivenValidCoordinatesWithCommaSeparators_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame()
            {
                SetEnvVar(BotInitialPosition, "  50.0, 70, 100");
                var pos = EnvVars.GetBotInfo().InitialPosition;
                Assert.That(pos.X, Is.EqualTo(50));
                Assert.That(pos.Y, Is.EqualTo(70));
                Assert.That(pos.Direction, Is.EqualTo(100));
            }
            
            [Test]
            public void GivenEmptyPosition_whenCallingGetInitialPosition_thenReturnNull()
            {
                ClearEnvVar(BotInitialPosition);
                Assert.That(EnvVars.GetBotInfo().InitialPosition, Is.Null);
            }

            [Test]
            public void GivenBlankPosition_whenCallingGetInitialPosition_thenReturnNull()
            {
                SetEnvVar(BotInitialPosition, "  \t");
                Assert.That(EnvVars.GetBotInfo().InitialPosition, Is.Null);
            }
        }
    }
}