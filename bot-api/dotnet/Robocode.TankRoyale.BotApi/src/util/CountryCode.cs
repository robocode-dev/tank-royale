using System.Globalization;
using System.Linq;

namespace Robocode.TankRoyale.BotApi.Util;

public static class CountryCode
{
    public static bool IsCountryCodeValid(string countryCode)
    {
        if (string.IsNullOrWhiteSpace(countryCode)) return false;
        if (!(countryCode is { Length: 2 })) return false;
        if (countryCode.ToUpper() == "UK") countryCode = "GB"; // UK is not supported in .Net, so convert it to GB

        return CultureInfo.GetCultures(CultureTypes.SpecificCultures)
            .Select(culture => new RegionInfo(culture.LCID))
            .Any(region => region.TwoLetterISORegionName == countryCode.ToUpper());
    }
}