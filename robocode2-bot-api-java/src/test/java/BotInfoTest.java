import com.neovisionaries.i18n.CountryCode;
import net.robocode2.BotInfo;
import net.robocode2.GameType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotInfoTest {

  @Test
  void whenAllParamsIsDefinedInConstructor_thenTheSameParamsCanBeReadOut() {
    BotInfo botInfo =
        new BotInfo(
            "MyBot",
            "1.0",
            "John Doe",
            "Sample bot",
            "dk",
            Arrays.asList(GameType.MELEE.toString(), GameType.TWIN_DUAL.toString()),
            "Java");

    assertEquals("MyBot", botInfo.getName());
    assertEquals("1.0", botInfo.getVersion());
    assertEquals("John Doe", botInfo.getAuthor());
    assertEquals("Sample bot", botInfo.getDescription());
    assertEquals("DK", botInfo.getCountryCode().toUpperCase());
    assertThat(botInfo.getGameTypes())
        .asList()
        .hasSize(2)
        .contains(GameType.MELEE.toString(), GameType.TWIN_DUAL.toString());
    assertEquals("Java", botInfo.getProgrammingLang());
  }

  @Test
  void whenNameIsNullInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                null,
                "1.0",
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenNameIsEmptyInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "",
                "1.0",
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenNameIsBlankInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                " \r\n",
                "1.0",
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenVersionIsNullInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot",
                null,
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenVersionIsEmptyInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot",
                "",
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenVersionIsBlankInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot",
                " \r\n",
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenAuthorIsNullInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot",
                "1.0",
                null,
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenAuthorIsEmptyInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot",
                "1.0",
                "",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenAuthorIsBlankInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot",
                "1.0",
                " \r\n",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java"));
  }

  @Test
  void whenCountryCodeIsNullInConstructor_thenCountryCodeIsSetToDefaultLocale() {
    String defaultLocateCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    assertEquals(
        defaultLocateCountryCode.toUpperCase(),
        new BotInfo(
                "MyBot",
                "1.0",
                "John Doe",
                "Sample bot",
                null,
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java")
            .getCountryCode()
            .toUpperCase());
  }

  @Test
  void whenCountryCodeIsEmptyInConstructor_thenCountryCodeIsSetToDefaultLocale() {
    String defaultLocateCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    assertEquals(
        defaultLocateCountryCode.toUpperCase(),
        new BotInfo(
                "MyBot",
                "1.0",
                "John Doe",
                "Sample bot",
                "",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java")
            .getCountryCode()
            .toUpperCase());
  }

  @Test
  void whenCountryCodeIsBlankInConstructor_thenCountryCodeIsSetToDefaultLocale() {
    String defaultLocateCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
    assertEquals(
        defaultLocateCountryCode.toUpperCase(),
        new BotInfo(
                "MyBot",
                "1.0",
                "John Doe",
                "Sample bot",
                " \r\n",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java")
            .getCountryCode()
            .toUpperCase());
  }

  @Test
  void whenCountryCodeIsDKInConstructor_thenCountryCodeIsSetToDK() {
    assertEquals(
        "DK",
        new BotInfo(
                "MyBot",
                "1.0",
                "John Doe",
                "Sample bot",
                "dk",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java")
            .getCountryCode()
            .toUpperCase());
  }

  @Test
  void whenCountryCodeIsDNKInConstructor_thenCountryCodeIsSetToDK() {
    assertEquals(
        "DK",
        new BotInfo(
                "MyBot",
                "1.0",
                "John Doe",
                "Sample bot",
                "DNK",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java")
            .getCountryCode()
            .toUpperCase());
  }

  @Test
  void whenCountryCodeIs208InConstructor_thenCountryCodeIsSetToDK() {
    assertEquals(
        "DK",
        new BotInfo(
                "MyBot",
                "1.0",
                "John Doe",
                "Sample bot",
                "208",
                Arrays.asList(GameType.MELEE.toString(), GameType.ONE_VS_ONE.toString()),
                "Java")
            .getCountryCode()
            .toUpperCase());
  }

  @Test
  void whenGameTypesIsNullInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new BotInfo("MyBot", "1.0", "John Doe", "Sample bot", "dk", null, "Java"));
  }

  @Test
  void whenGameTypesIsEmptyInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot", "1.0", "John Doe", "Sample bot", "dk", Collections.emptyList(), "Java"));
  }

  @Test
  void whenGameTypesHasNoGamesInConstructor_thenThrowIllegalArgumentException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BotInfo(
                "MyBot", "1.0", "John Doe", "Sample bot", "dk", Arrays.asList(null, ""), "Java"));
  }

  @Test
  void whenGameTypesIsUntrimmedInConstructor_thenTrimGameTypes() {
    BotInfo botInfo =
        new BotInfo(
            "MyBot",
            "1.0",
            "John Doe",
            "Sample bot",
            "dk",
            Arrays.asList("  melee  ", "  1v1  "),
            "Java");

    List<String> gameTypes = botInfo.getGameTypes();

    assertThat(gameTypes).asList().hasSize(2).contains("melee", "1v1");
  }
}
