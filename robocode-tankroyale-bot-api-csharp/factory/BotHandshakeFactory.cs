using System.Collections.Generic;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi
{
  public class BotHandshakeFactory
  {
    public static BotHandshake Create(BotInfo botInfo)
    {
      var handshake = new BotHandshake();
      handshake.Type = MessageType.BotHandshake;
      handshake.Name = botInfo.Name;
      handshake.Version = botInfo.Version;
      handshake.Author = botInfo.Author;
      handshake.CountryCode = (botInfo.CountryCode);
      handshake.GameTypes = new List<string>(botInfo.GameTypes);
      handshake.ProgrammingLang = botInfo.ProgrammingLang;
      return handshake;
    }
  }
}