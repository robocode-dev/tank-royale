package test_utils;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.InitialPosition;

import java.util.Collection;
import java.util.List;

public final class BotInfoBuilder {

    private String name;
    private String version;
    private List<String> authors;
    private String description;
    private String homepage;
    private List<String> countryCodes;
    private Collection<String> gameTypes;
    private String platform;
    private String programmingLang;
    private InitialPosition initialPosition;

    public BotInfoBuilder(BotInfo botInfo) {
        name = botInfo.getName();
        version = botInfo.getVersion();
        authors = botInfo.getAuthors();
        description = botInfo.getDescription();
        homepage = botInfo.getHomepage();
        countryCodes = botInfo.getCountryCodes();
        gameTypes = botInfo.getGameTypes();
        platform = botInfo.getPlatform();
        programmingLang = botInfo.getProgrammingLang();
        initialPosition = botInfo.getInitialPosition();
    }

    public BotInfo build() {
        return new BotInfo(name, version, authors, description, homepage, countryCodes, gameTypes, platform,
                programmingLang, initialPosition);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public void setCountryCodes(List<String> countryCodes) {
        this.countryCodes = countryCodes;
    }

    public void setGameTypes(List<String> gameTypes) {
        this.gameTypes = gameTypes;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setProgrammingLang(String programmingLang) {
        this.programmingLang = programmingLang;
    }

    public void setInitialPosition(InitialPosition initialPosition) {
        this.initialPosition = initialPosition;
    }
}