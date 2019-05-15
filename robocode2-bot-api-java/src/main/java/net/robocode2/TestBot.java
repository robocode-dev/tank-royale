package net.robocode2;

import net.robocode2.events.ConnectedEvent;
import net.robocode2.events.DisconnectedEvent;

import java.net.URI;
import java.util.Collections;

public class TestBot extends Bot {

  private static final BotInfo botInfo =
      BotInfo.builder()
          .name("Test")
          .version("1")
          .author("fnl")
          .gameTypes(Collections.singletonList(GameType.MELEE.toString()))
          .build();

  private TestBot() throws Exception {
    super(botInfo, new URI("ws://localhost:55000"));
  }

  public static void main(String[] args) throws Exception {
    new TestBot().run();
  }

  @Override
  public void onConnected(ConnectedEvent event) {
    System.out.println("onConnected");
  }

  @Override
  public void onDisconnected(DisconnectedEvent event) {
    System.out.println("onDisconnected");
  }
}
