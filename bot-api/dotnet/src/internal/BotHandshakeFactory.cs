using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Internal
{
  internal sealed class BotHandshakeFactory
  {
    internal static BotHandshake Create(BotInfo botInfo, string serverSecret)
    {
      var handshake = new BotHandshake();
      handshake.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotHandshake);
      handshake.Name = botInfo.Name;
      handshake.Version = botInfo.Version;
      handshake.Authors = new List<string>(botInfo.Authors);
      handshake.Description = botInfo.Description;
      handshake.Homepage = botInfo.Homepage;
      handshake.CountryCodes = botInfo.CountryCodes != null ? new List<string>(botInfo.CountryCodes) : new List<string>();
      handshake.GameTypes = botInfo.GameTypes != null ? new HashSet<string>(botInfo.GameTypes) : new HashSet<string>();
      handshake.Platform = botInfo.Platform;
      handshake.ProgrammingLang = botInfo.ProgrammingLang;
      handshake.Secret = serverSecret;
      return handshake;
    }
  }
}