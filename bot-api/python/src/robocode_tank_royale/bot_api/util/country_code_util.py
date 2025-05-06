import pycountry
import locale


class CountryCodeUtil:
    """Country code utility class."""

    @staticmethod
    def get_local_country_code():
        try:
            # Get the current locale's language and territory
            current_locale = locale.getlocale()[0]
            if current_locale and '_' in current_locale:
                country_code = current_locale.split('_')[1]
                # Verify it's a valid country code
                if pycountry.countries.get(alpha_2=country_code):
                    return country_code
        except (IndexError, AttributeError):
            pass
        return None

    @staticmethod
    def to_country_code(code):
        if not code or not isinstance(code, str) or len(code.strip()) != 2:
            return None

        try:
            return pycountry.countries.get(alpha_2=code.strip().upper())
        except (KeyError, AttributeError):
            return None
