import pytest

from robocode_tank_royale.bot_api import BotInfo


class TestBotInfo:
    def test_TR_API_VAL_001_required_fields(self):
        """TR-API-VAL-001 BotInfo required fields"""
        # Arrange
        name = "MyBot"
        version = "1.0"
        authors = ["Author 1", "Author 2"]

        # Act
        info = BotInfo(name=name, version=version, authors=authors)

        # Assert
        assert info.name == name
        assert info.version == version
        assert info.authors == ["Author 1", "Author 2"]
        # Optional fields default/processing
        assert info.description is None
        assert info.homepage is None
        assert isinstance(info.country_codes, list)
        assert info.country_codes == []
        assert isinstance(info.game_types, set)
        assert info.game_types == set()
        assert isinstance(info.platform, str) and len(info.platform) > 0
        assert info.programming_lang is None
        assert info.initial_position is None

    @pytest.mark.parametrize("field,value,err_contains", [
        ("name", "", "name"),
        ("name", "   ", "name"),
        ("version", "", "version"),
        ("version", "   ", "version"),
    ])
    def test_TR_API_VAL_002_invalid_fields_validation_required_non_blank(self, field, value, err_contains):
        """TR-API-VAL-002 BotInfo validation: invalid fields raise/throw"""
        kwargs = dict(name="Bot", version="1.0", authors=["A"])  # valid base
        kwargs[field] = value
        with pytest.raises(ValueError) as exc:
            BotInfo(**kwargs)
        assert err_contains in str(exc.value)

    def test_TR_API_VAL_002_invalid_fields_validation_authors_rules(self):
        """TR-API-VAL-002 BotInfo validation: authors list rules"""
        # Empty authors list
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=[])

        # Blank author element
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=[" "])

        # Too many authors (MAX_NUMBER_OF_AUTHORS = 5)
        too_many = ["a1", "a2", "a3", "a4", "a5", "a6"]
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=too_many)

    def test_TR_API_VAL_002_invalid_fields_validation_max_lengths(self):
        """TR-API-VAL-002 BotInfo validation: max lengths respected"""
        # Name
        name_ok = "x" * BotInfo.MAX_NAME_LENGTH
        info = BotInfo(name=name_ok, version="1.0", authors=["A"])  # should not throw
        assert info.name == name_ok
        name_bad = "x" * (BotInfo.MAX_NAME_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name=name_bad, version="1.0", authors=["A"])  

        # Version
        version_ok = "x" * BotInfo.MAX_VERSION_LENGTH
        info = BotInfo(name="Bot", version=version_ok, authors=["A"])  
        assert info.version == version_ok
        version_bad = "x" * (BotInfo.MAX_VERSION_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version=version_bad, authors=["A"])  

        # Author length
        author_ok = "x" * BotInfo.MAX_AUTHOR_LENGTH
        info = BotInfo(name="Bot", version="1.0", authors=[author_ok])
        assert info.authors == [author_ok]
        author_bad = "x" * (BotInfo.MAX_AUTHOR_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=[author_bad])

        # Description length
        desc_ok = "x" * BotInfo.MAX_DESCRIPTION_LENGTH
        info = BotInfo(name="Bot", version="1.0", authors=["A"], description=desc_ok)
        assert info.description == desc_ok
        desc_bad = "x" * (BotInfo.MAX_DESCRIPTION_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], description=desc_bad)

        # Homepage length
        home_ok = "x" * BotInfo.MAX_HOMEPAGE_LENGTH
        info = BotInfo(name="Bot", version="1.0", authors=["A"], homepage=home_ok)
        assert info.homepage == home_ok
        home_bad = "x" * (BotInfo.MAX_HOMEPAGE_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], homepage=home_bad)

        # Game type length and count
        gt_ok = {"x" * BotInfo.MAX_GAME_TYPE_LENGTH}
        info = BotInfo(name="Bot", version="1.0", authors=["A"], game_types=gt_ok)
        assert info.game_types == gt_ok
        gt_bad = {"x" * (BotInfo.MAX_GAME_TYPE_LENGTH + 1)}
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], game_types=gt_bad)

        max_gts = {f"gt{i}" for i in range(BotInfo.MAX_NUMBER_OF_GAME_TYPES)}
        info = BotInfo(name="Bot", version="1.0", authors=["A"], game_types=max_gts)
        assert info.game_types == max_gts
        too_many_gts = {f"gt{i}" for i in range(BotInfo.MAX_NUMBER_OF_GAME_TYPES + 1)}
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], game_types=too_many_gts)

        # Country codes count
        max_cc = [f"c{i}" for i in range(BotInfo.MAX_NUMBER_OF_COUNTRY_CODES)]
        info = BotInfo(name="Bot", version="1.0", authors=["A"], country_codes=max_cc)
        assert info.country_codes == max_cc
        too_many_cc = [f"c{i}" for i in range(BotInfo.MAX_NUMBER_OF_COUNTRY_CODES + 1)]
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], country_codes=too_many_cc)

        # Platform max length
        platform_ok = "x" * BotInfo.MAX_PLATFORM_LENGTH
        info = BotInfo(name="Bot", version="1.0", authors=["A"], platform=platform_ok)
        assert info.platform == platform_ok
        platform_bad = "x" * (BotInfo.MAX_PLATFORM_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], platform=platform_bad)

        # Programming language max length
        pl_ok = "x" * BotInfo.MAX_PROGRAMMING_LANG_LENGTH
        info = BotInfo(name="Bot", version="1.0", authors=["A"], programming_lang=pl_ok)
        assert info.programming_lang == pl_ok
        pl_bad = "x" * (BotInfo.MAX_PROGRAMMING_LANG_LENGTH + 1)
        with pytest.raises(ValueError):
            BotInfo(name="Bot", version="1.0", authors=["A"], programming_lang=pl_bad)
