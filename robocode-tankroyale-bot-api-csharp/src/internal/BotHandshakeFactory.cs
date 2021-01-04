using System.Collections.Generic;
using Robocode.TankRoyale.BotApi.Util;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi
{
  internal sealed class BotHandshakeFactory
  {
    internal static BotHandshake Create(BotInfo botInfo)
    {
      var handshake = new BotHandshake();
      handshake.Type = EnumUtil.GetEnumMemberAttrValue(MessageType.BotHandshake);
      handshake.Name = botInfo.Name;
      handshake.Version = botInfo.Version;
      handshake.Author = botInfo.Author;
      handshake.Description = botInfo.Description;
      handshake.Url = botInfo.Url;
      handshake.CountryCode = (botInfo.CountryCode);
      handshake.GameTypes = new List<string>(botInfo.GameTypes);
      handshake.Platform = botInfo.Platform;
      handshake.ProgrammingLang = botInfo.ProgrammingLang;
      return handshake;
    }
  }
}