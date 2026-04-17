import pytest

from robocode_tank_royale.bot_api.util import CountryCodeUtil


def test_TR_API_UTL_003_country_code_valid_examples():
    """TR-API-UTL-003 CountryCode utility: validation/normalization of known codes"""
    for code in ["GB", "gb", "dk", "us", "no", "SE", "FI"]:
        country = CountryCodeUtil.to_country_code(code)
        assert country is not None, f"Expected valid country for code={code}"
        assert country.alpha_2.upper() == code.strip().upper()


def test_TR_API_UTL_003_country_code_local_detection():
    """TR-API-UTL-003 CountryCode utility: local country code is recognized"""
    local = CountryCodeUtil.local_country_code()
    if local is not None:
        # If environment provides a locale with territory, ensure it's a valid code
        assert len(local) == 2
        assert CountryCodeUtil.to_country_code(local) is not None
    else:
        # If no local code resolvable, ensure API returns None (acceptable per semantics)
        assert local is None
