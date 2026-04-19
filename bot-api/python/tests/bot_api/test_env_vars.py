import pytest
import os
from robocode_tank_royale.bot_api.internal.env_vars import EnvVars

@pytest.fixture
def clean_env():
    """Clear all bot-related environment variables before each test."""
    bot_vars = [
        "SERVER_URL", "SERVER_SECRET", "BOT_NAME", "BOT_VERSION", "BOT_AUTHORS",
        "BOT_DESCRIPTION", "BOT_HOMEPAGE", "BOT_COUNTRY_CODES", "BOT_GAME_TYPES",
        "BOT_PLATFORM", "BOT_PROG_LANG", "BOT_INITIAL_POS", "TEAM_ID", "TEAM_NAME",
        "TEAM_VERSION", "BOT_BOOTED"
    ]
    old_values = {var: os.environ.get(var) for var in bot_vars}
    for var in bot_vars:
        if var in os.environ:
            del os.environ[var]
    yield
    for var, val in old_values.items():
        if val is not None:
            os.environ[var] = val
        elif var in os.environ:
            del os.environ[var]

# TR-API-BOT-001a: Constructor reads env vars and applies defaults

def test_TR_API_BOT_001_get_server_url(clean_env):
    os.environ["SERVER_URL"] = "ws://localhost:7654"
    assert EnvVars.get_server_url() == "ws://localhost:7654"

def test_TR_API_BOT_001_get_server_url_missing(clean_env):
    assert EnvVars.get_server_url() is None

def test_TR_API_BOT_001_get_server_secret(clean_env):
    os.environ["SERVER_SECRET"] = "s3cr3t"
    assert EnvVars.get_server_secret() == "s3cr3t"

def test_TR_API_BOT_001_get_bot_name(clean_env):
    os.environ["BOT_NAME"] = "MyBot"
    assert EnvVars.get_bot_name() == "MyBot"

def test_TR_API_BOT_001_get_bot_version(clean_env):
    os.environ["BOT_VERSION"] = "2.0"
    assert EnvVars.get_bot_version() == "2.0"

def test_TR_API_BOT_001_get_bot_description_missing(clean_env):
    assert EnvVars.get_bot_description() is None

def test_TR_API_BOT_001_get_bot_homepage_missing(clean_env):
    assert EnvVars.get_bot_homepage() is None

def test_TR_API_BOT_001_get_bot_platform_missing(clean_env):
    assert EnvVars.get_bot_platform() is None

def test_TR_API_BOT_001_get_bot_programming_lang_missing(clean_env):
    assert EnvVars.get_bot_programming_lang() is None

def test_TR_API_BOT_001_is_bot_booted_false(clean_env):
    assert EnvVars.is_bot_booted() is False

def test_TR_API_BOT_001_is_bot_booted_true(clean_env):
    os.environ["BOT_BOOTED"] = "1"
    assert EnvVars.is_bot_booted() is True

def test_TR_API_BOT_001_get_bot_authors_list(clean_env):
    os.environ["BOT_AUTHORS"] = "Alice,Bob,Carol"
    assert EnvVars.get_bot_authors() == ["Alice", "Bob", "Carol"]

def test_TR_API_BOT_001_get_bot_authors_trim(clean_env):
    os.environ["BOT_AUTHORS"] = "Alice , Bob , Carol"
    assert EnvVars.get_bot_authors() == ["Alice", "Bob", "Carol"]

def test_TR_API_BOT_001_get_bot_authors_missing(clean_env):
    assert EnvVars.get_bot_authors() == []

def test_TR_API_BOT_001_get_bot_authors_blank(clean_env):
    os.environ["BOT_AUTHORS"] = "   "
    assert EnvVars.get_bot_authors() == []

def test_TR_API_BOT_001_get_bot_country_codes(clean_env):
    os.environ["BOT_COUNTRY_CODES"] = "US,GB,DE"
    assert EnvVars.get_bot_country_codes() == ["US", "GB", "DE"]

def test_TR_API_BOT_001_get_bot_game_types(clean_env):
    os.environ["BOT_GAME_TYPES"] = "classic , melee"
    assert EnvVars.get_bot_game_types() == {"classic", "melee"}

def test_TR_API_BOT_001_get_team_id_missing(clean_env):
    assert EnvVars.get_team_id() is None

def test_TR_API_BOT_001_get_team_id_blank(clean_env):
    os.environ["TEAM_ID"] = "  "
    assert EnvVars.get_team_id() is None

def test_TR_API_BOT_001_get_team_id_valid(clean_env):
    os.environ["TEAM_ID"] = "42"
    assert EnvVars.get_team_id() == 42

def test_TR_API_BOT_001_get_team_id_trim(clean_env):
    os.environ["TEAM_ID"] = "  7  "
    assert EnvVars.get_team_id() == 7

def test_TR_API_BOT_001_get_bot_initial_position_missing(clean_env):
    assert EnvVars.get_bot_initial_position() is None

def test_TR_API_BOT_001_get_bot_initial_position_valid(clean_env):
    os.environ["BOT_INITIAL_POS"] = "100,200,90"
    pos = EnvVars.get_bot_initial_position()
    assert pos.x == 100
    assert pos.y == 200
    assert pos.direction == 90

def test_TR_API_BOT_001_get_bot_info_full(clean_env):
    os.environ["BOT_NAME"] = "MyBot"
    os.environ["BOT_VERSION"] = "1.0"
    os.environ["BOT_AUTHORS"] = "Alice,Bob"
    os.environ["BOT_DESCRIPTION"] = "A test bot"
    os.environ["BOT_HOMEPAGE"] = "https://example.com"
    os.environ["BOT_COUNTRY_CODES"] = "US,GB"
    os.environ["BOT_GAME_TYPES"] = "classic"
    os.environ["BOT_PLATFORM"] = "Python"
    os.environ["BOT_PROG_LANG"] = "Python 3"

    info = EnvVars.get_bot_info()
    assert info.name == "MyBot"
    assert info.version == "1.0"
    assert info.authors == ["Alice", "Bob"]
    assert info.description == "A test bot"
    assert info.homepage == "https://example.com"
    assert info.country_codes == ["US", "GB"]
    assert info.game_types == {"classic"}
    assert info.platform == "Python"
    assert info.programming_lang == "Python 3"

def test_TR_API_BOT_001_get_bot_info_missing_required(clean_env):
    os.environ["BOT_VERSION"] = "1.0"
    os.environ["BOT_AUTHORS"] = "Alice"
    # BOT_NAME is missing
    info = EnvVars.get_bot_info()
    assert info.name is None
    assert info.version == "1.0"

def test_TR_API_BOT_001_get_bot_info_blank_required(clean_env):
    os.environ["BOT_NAME"] = "  "
    os.environ["BOT_VERSION"] = "1.0"
    os.environ["BOT_AUTHORS"] = "Alice"
    info = EnvVars.get_bot_info()
    assert info.name is None
