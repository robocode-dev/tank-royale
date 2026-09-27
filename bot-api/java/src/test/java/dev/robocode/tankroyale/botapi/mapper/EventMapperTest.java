package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.botapi.events.TeamMessageEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("Unit")
class EventMapperTest {

    @Test
    void preservesOrderedDuplicateTeamMessagesFromATick() {
        var first = teamMessage("first");
        var duplicate = teamMessage("first");
        var last = teamMessage("last");
        var tick = new dev.robocode.tankroyale.schema.TickEventForBot();
        tick.setTurnNumber(7);
        tick.setRoundNumber(1);
        tick.setBulletStates(List.of());
        tick.setEvents(List.of(first, duplicate, last));

        var mapped = EventMapper.map(tick, new BaseBot() {});

        assertThat(mapped.getEvents()).hasSize(3)
                .allMatch(TeamMessageEvent.class::isInstance)
                .extracting(event -> ((TeamMessageEvent) event).getMessage())
                .containsExactly("first", "first", "last");
    }

    private static dev.robocode.tankroyale.schema.TeamMessageEvent teamMessage(String message) {
        var event = new dev.robocode.tankroyale.schema.TeamMessageEvent();
        event.setTurnNumber(7);
        event.setSenderId(3);
        event.setMessageType(String.class.getName());
        event.setMessage('"' + message + '"');
        return event;
    }
}
