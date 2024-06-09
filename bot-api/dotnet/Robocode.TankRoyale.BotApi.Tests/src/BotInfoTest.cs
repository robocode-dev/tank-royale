using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Microsoft.Extensions.Configuration;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Util;

using static Robocode.TankRoyale.BotApi.BotInfo;
using static Robocode.TankRoyale.BotApi.Util.CountryCode;

namespace Robocode.TankRoyale.BotApi.Tests;

public class BotInfoTest
{
    static readonly string Name = "  TestBot  ";
    static readonly string Version = "  1.0  ";
    static readonly List<string> Authors = new List<string> { " Author 1  ", " Author 2 " };
    static readonly string Description = "  short description ";
    static readonly string Homepage = " https://testbot.robocode.dev ";
    static readonly List<string> CountryCodes = new() { " gb ", "  US " };
    static readonly ISet<string> GameTypes = new HashSet<string> { " classic ", " melee ", " 1v1 " };
    static readonly string Platform = " .Net 6 ";
    static readonly string ProgrammingLang = " C# 11 ";
    static readonly InitialPosition InitialPosition = InitialPosition.FromString("  10, 20, 30  ");

    [TestFixture]
    public class NameTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithNameSet_whenGettingNameFromBotInfo_thenTrimmedNameIsReturned()
        {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.Name, Is.EqualTo(Name.Trim()));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenNameIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(string name)
        {
            var builder = PrefilledBuilder().SetName(name);
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower(), Is.EqualTo("'name' cannot be null, empty or blank"));
        }

        [Test]
        public void GivenNameOfMaxLength_whenConstructingBotInfo_thenReturnTheSameName()
        {
            var name = StringOfLength(MaxNameLength);
            var botInfo = PrefilledBuilder().SetName(name).Build();
            Assert.That(botInfo.Name, Is.EqualTo(name));
        }

        [Test]
        public void GivenNameOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().SetName(StringOfLength(MaxNameLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'name' length exceeds"), Is.True);
        }
    }

    [TestFixture]
    private class VersionTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithVersionSet_whenGettingVersionFromBotInfo_thenTrimmedVersionIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.Version, Is.EqualTo(Version.Trim()));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenVersionIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(string version)
        {
            var builder = PrefilledBuilder().SetVersion(version);
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower(), Is.EqualTo("'version' cannot be null, empty or blank"));
        }
    
        [Test]
        public void GivenVersionOfMaxLength_whenConstructingBotInfo_thenReturnTheSameVersion()
        {
            var version = StringOfLength(MaxVersionLength);
            var botInfo = PrefilledBuilder().SetVersion(version).Build();
            Assert.That(botInfo.Version, Is.EqualTo(version));
        }
    
        [Test]
        public void GivenVersionOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().SetVersion(StringOfLength(MaxVersionLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'version' length exceeds"), Is.True);
        }
    }

    [TestFixture]
    private class AuthorsTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithAuthorsSet_whenGettingAuthorsFromBotInfo_thenTrimmedAuthorsCollectionIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.Authors, Is.EqualTo(Authors.ConvertAll(str => str.Trim())));
        }
        
        [Test]
        [TestCaseSource(nameof(ListOfEmptyOrBlanks))]
        public void GivenEmptyOrBlankAuthors_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(List<string> authors)
        {
            var builder = PrefilledBuilder().SetAuthors(authors);
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower(), Is.EqualTo("'authors' cannot be null or empty or contain blanks"));
        }

        [Test]
        public void GivenSingleAuthorOfMaxLength_whenConstructingBotInfo_thenReturnTheSameAuthor()
        {
            var author = StringOfLength(MaxAuthorLength);
            var botInfo = PrefilledBuilder().SetAuthors(new List<string> { author }).Build();
            Assert.That(botInfo.Authors[0], Is.EqualTo(author));
        }
    
        [Test]
        public void GivenSingleAuthorOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().AddAuthor(StringOfLength(MaxAuthorLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'authors' length exceeds"), Is.True);
        }
    
        [Test]
        public void GivenMaxNumberOfAuthors_whenConstructingBotInfo_thenReturnAuthorsWithTheSameMaxCount() {
            var builder = PrefilledBuilder().SetAuthors(null);
            for (var i = 0; i < MaxNumberOfAuthors; i++) {
                builder.AddAuthor(Authors[0]);
            }
            Assert.That(builder.Build().Authors.Count, Is.EqualTo(MaxNumberOfAuthors));
        }

        [Test]
        public void GivenOneMoreThanMaxNumberOfAuthors_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo()
        {
            var builder = PrefilledBuilder().SetAuthors(null);
            for (var i = 0; i < MaxNumberOfAuthors + 1; i++) {
                builder.AddAuthor(Authors[0]);
            }
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("size of 'authors' exceeds the maximum"), Is.True);
        }
    }

    [TestFixture]
    private class DescriptionTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithDescriptionSet_whenGettingDescriptionFromBotInfo_thenTrimmedDescriptionIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.Description, Is.EqualTo(Description.Trim()));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenDescriptionIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(string description)
        {
            var builder = PrefilledBuilder().SetDescription(description);
            var botInfo = builder.Build();
            Assert.That(botInfo.Description, Is.Null);
        }

        [Test]
        public void GivenDescriptionOfMaxLength_whenConstructingBotInfo_thenReturnTheSameVersion()
        {
            var description = StringOfLength(MaxDescriptionLength);
            var botInfo = PrefilledBuilder().SetDescription(description).Build();
            Assert.That(botInfo.Description, Is.EqualTo(description));
        }
    
        [Test]
        public void GivenDescriptionOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().SetDescription(StringOfLength(MaxDescriptionLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'description' length exceeds"), Is.True);
        }
    }

    [TestFixture]
    private class HomepageTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithHomepageSet_whenGettingHomepageFromBotInfo_thenTrimmedHomepageIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.Homepage, Is.EqualTo(Homepage.Trim()));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenHomepageIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(string homepage)
        {
            var builder = PrefilledBuilder().SetHomepage(homepage);
            var botInfo = builder.Build();
            Assert.That(botInfo.Homepage, Is.Null);
        }

        [Test]
        public void GivenHomepageOfMaxLength_whenConstructingBotInfo_thenReturnTheSameHomepage()
        {
            var homepage = StringOfLength(MaxHomepageLength);
            var botInfo = PrefilledBuilder().SetHomepage(homepage).Build();
            Assert.That(botInfo.Homepage, Is.EqualTo(homepage));
        }
    
        [Test]
        public void GivenHomepageOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().SetHomepage(StringOfLength(MaxHomepageLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'homepage' length exceeds"), Is.True);
        }
    }

    [TestFixture]
    private class CountryCodesTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithCountryCodesSet_whenGettingCountryCodesFromBotInfo_thenTrimmedCountryCodesCollectionIsReturned()
        {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.CountryCodes.ToList().ConvertAll(str => str.ToUpper()),
                Is.EqualTo(CountryCodes.ConvertAll(str => str.ToUpper()).ConvertAll(str => str.Trim())));
        }
        
        [Test]
        [TestCaseSource(nameof(ListOfEmptyOrBlanks))]
        public void GivenEmptyOrBlankCountryCodes_whenConstructingBotInfo_thenListOfDefaultLocalCountryCodeReturned(List<string> countryCodes)
        {
            var builder = PrefilledBuilder().SetCountryCodes(countryCodes);
            var botInfo = builder.Build();
            Assert.That(botInfo.CountryCodes, Is.EqualTo(new List<string> { GetLocalCountryCode() }));
        }

        [Test]
        public void GivenListOfValidCountryCodes_whenCallingSetCountryCodes_thenReturnListOfSameCountryCodes()
        {
            var botInfo = PrefilledBuilder().SetCountryCodes(new List<string> { "dk" }).Build();
            Assert.That(botInfo.CountryCodes[0], Is.EqualTo("DK"));
        }

        [Test]
        [TestCase("d")]
        [TestCase("dnk")]
        [TestCase("xx")]
        public void GivenListOfInvalidCountryCodes_whenCallingSetCountryCodes_thenListOfDefaultLocalCountryCodeReturned(string countryCode)
        {
            var botInfo = PrefilledBuilder().SetCountryCodes(null).AddCountryCode(countryCode).Build();
            Assert.That(botInfo.CountryCodes[0].ToLower(), Is.EqualTo(GetLocalCountryCode().ToLower()));
        }
    
        [Test]
        public void GivenMaxNumberOfCountryCodes_whenConstructingBotInfo_thenReturnCountryCodesWithTheSameMaxCount() {
            var builder = PrefilledBuilder().SetCountryCodes(null);
            for (var i = 0; i < MaxNumberOfCountryCodes; i++) {
                builder.AddCountryCode(CountryCodes[0]);
            }
            Assert.That(builder.Build().CountryCodes.Count, Is.EqualTo(MaxNumberOfCountryCodes));
        }

        [Test]
        public void GivenOneMoreThanMaxNumberOfCountryCodes_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo()
        {
            var builder = PrefilledBuilder().SetCountryCodes(null);
            for (var i = 0; i < MaxNumberOfCountryCodes + 1; i++) {
                builder.AddCountryCode(CountryCodes[0]);
            }
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("size of 'countrycodes' exceeds the maximum"), Is.True);
        }
    }

    [TestFixture]
    private class GameTypesTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithGameTypesSet_whenGettingGameTypesFromBotInfo_thenTrimmedGameTypesCollectionIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.GameTypes, Is.EqualTo(GameTypes.ToList().ConvertAll(str => str.Trim())));
        }
        
        [Test]
        [TestCaseSource(nameof(SetOfEmptyOrBlanks))]
        public void GivenEmptyOrBlankGameTypes_whenConstructingBotInfo_thenEmptyListIsReturned(ISet<string> gameTypes)
        {
            var botInfo = PrefilledBuilder().SetGameTypes(gameTypes).Build();
            Assert.That(botInfo.GameTypes.Count, Is.Zero);
        }

        [Test]
        public void GivenGameTypeOfMaxLength_whenConstructingBotInfo_thenReturnTheSameGameType()
        {
            var gameType = StringOfLength(MaxGameTypeLength);
            var botInfo = PrefilledBuilder().SetGameTypes(new HashSet<string> { gameType }).Build();
            Assert.That(botInfo.GameTypes.Contains(gameType), Is.True);
        }
    
        [Test]
        public void GivenGameTypeOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().AddGameType(StringOfLength(MaxGameTypeLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'gametypes' length exceeds"), Is.True);
        }
    
        [Test]
        public void GivenMaxNumberOfGameTypes_whenConstructingBotInfo_thenReturnCountryCodesWithTheSameMaxCount() {
            var builder = PrefilledBuilder().SetCountryCodes(null);
            for (var i = 0; i < MaxNumberOfCountryCodes; i++) {
                builder.AddCountryCode(CountryCodes[0]);
            }
            Assert.That(builder.Build().CountryCodes.Count, Is.EqualTo(MaxNumberOfCountryCodes));
        }

        [Test]
        public void GivenOneMoreThanMaxNumberOfGameTypes_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo()
        {
            var builder = PrefilledBuilder().SetCountryCodes(null);
            for (var i = 0; i < MaxNumberOfCountryCodes + 1; i++) {
                builder.AddCountryCode(CountryCodes[0]);
            }
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("size of 'countrycodes' exceeds the maximum"), Is.True);
        }
    }

    [TestFixture]
    private class PlatformTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithPlatformSet_whenGettingPlatformFromBotInfo_thenTrimmedPlatformIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.Platform, Is.EqualTo(Platform.Trim()));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenPlatformIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenReturnStringWithJreAndVersion(string platform)
        {
            var botInfo = PrefilledBuilder().SetPlatform(platform).Build();
            Assert.That(botInfo.Platform, Is.EqualTo(PlatformUtil.GetPlatformName()));
        }

        [Test]
        public void GivenPlatformOfMaxLength_whenConstructingBotInfo_thenReturnTheSamePlatform() {
            var platform = StringOfLength(MaxPlatformLength);
            var botInfo = PrefilledBuilder().SetPlatform(platform).Build();
            Assert.That(botInfo.Platform, Is.EqualTo(platform));
        }

        [Test]
        public void GivenPlatformOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().SetPlatform(StringOfLength(MaxPlatformLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'platform' length exceeds"), Is.True);
        }
    }
    
    [TestFixture]
    private class ProgrammingLangTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithProgrammingLangSet_whenGettingProgrammingLangFromBotInfo_thenTrimmedProgrammingLangIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.ProgrammingLang, Is.EqualTo(ProgrammingLang.Trim()));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenProgrammingLangIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenReturnNull(string platform)
        {
            var builder = PrefilledBuilder().SetProgrammingLang(platform);
            var botInfo = builder.Build();
            Assert.That(botInfo.ProgrammingLang, Is.Null);
        }

        [Test]
        public void GivenProgrammingLangOfMaxLength_whenConstructingBotInfo_thenReturnTheSameProgrammingLang() {
            var programmingLang = StringOfLength(MaxProgrammingLangLength);
            var botInfo = PrefilledBuilder().SetProgrammingLang(programmingLang).Build();
            Assert.That(botInfo.ProgrammingLang, Is.EqualTo(programmingLang));
        }

        [Test]
        public void GivenProgrammingLangOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = PrefilledBuilder().SetProgrammingLang(StringOfLength(MaxProgrammingLangLength + 1));
            var exception = Assert.Throws<ArgumentException>(() => builder.Build());
            Assert.That(exception?.Message.ToLower().Contains("'programminglang' length exceeds"), Is.True);
        }
    }

    [TestFixture]
    private class InitialPositionTest : BotInfoTest
    {
        [Test]
        public void GivenPrefilledBotInfoWithProgrammingLangSet_whenGettingProgrammingLangFromBotInfo_thenTrimmedProgrammingLangIsReturned() {
            var botInfo = CreateBotInfo();
            Assert.That(botInfo.InitialPosition, Is.EqualTo(InitialPosition));
        }
        
        [Test]
        [TestCase(null)]
        [TestCase("")]
        [TestCase("  ")]
        [TestCase("\t ")]
        [TestCase("\n")]
        public void GivenInitialPositionIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenReturnNull(string initialPosition)
        {
            var builder = PrefilledBuilder().SetInitialPosition(InitialPosition.FromString(initialPosition));
            var botInfo = builder.Build();
            Assert.That(botInfo.InitialPosition, Is.Null);
        }
    }

    [TestFixture]
    private class FromFileTest : BotInfoTest
    {
        [Test]
        public void GivenValidFilePath_whenCallingFromFile_thenBotInfoIsRead()
        {
            var filePath = Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources/TestBot.json");
            var botInfo = FromFile(filePath);
            Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
            Assert.That(botInfo.Version, Is.EqualTo("1.0"));
        }

        [Test]
        public void GivenValidFileAndBasePath_whenCallingFromFile_thenBotInfoIsRead()
        {
            var basePath = Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources");
            var botInfo = FromFile("TestBot.json", basePath);
            Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
            Assert.That(botInfo.Version, Is.EqualTo("1.0"));
        }

        [Test]
        public void GivenNonExistingFileName_whenCallingFromFile_thenThrowFileBotFoundException()
        {
            const string filename = "non-existing-filename";
            Assert.Throws<FileNotFoundException>(() => FromFile(filename));
        }
    }

    [TestFixture]
    private class FromConfigurationTest : BotInfoTest
    {
        [Test]
        public void GivenValidFilePath_whenCallingConfigurationBuilder_thenBotInfoIsRead()
        {
            var configBuilder = new ConfigurationBuilder()
                .SetBasePath(Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources"))
                .AddJsonFile("TestBot.json");

            var botInfo = FromConfiguration(configBuilder.Build());
            Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
            Assert.That(botInfo.Version, Is.EqualTo("1.0"));
        }

        [Test]
        public void GivenEmptyConfiguration_whenCallingConfigurationBuilder_thenThrowException()
        {
            Assert.That(() => FromConfiguration(new ConfigurationBuilder().Build()), Throws.Exception);
        }
    }

    private static readonly object[] ListOfEmptyOrBlanks =
    {
        new object[] { new List<string>() },
        new object[] { new List<string> { "" } },
        new object[] { new List<string> { "\t" } },
        new object[] { new List<string> { " \n" } },
        new object[] { new List<string> { " ", "" } }
    };

    private static readonly object[] SetOfEmptyOrBlanks =
    {
        new object[] { new HashSet<string>() },
        new object[] { new HashSet<string> { "" } },
        new object[] { new HashSet<string> { "\t" } },
        new object[] { new HashSet<string> { " \n" } },
        new object[] { new HashSet<string> { " ", "" } }
    };

    private static IBuilder PrefilledBuilder()
    {
        return Builder().Copy(new BotInfo(Name, Version, Authors, Description, Homepage, CountryCodes,
            GameTypes, Platform, ProgrammingLang, InitialPosition));
    }

    private static string StringOfLength(int length) => new string('x', length);
    
    private static BotInfo CreateBotInfo()
    {
        return new BotInfo(Name, Version, Authors, Description, Homepage, CountryCodes, GameTypes, Platform,
            ProgrammingLang, InitialPosition);
    }
}