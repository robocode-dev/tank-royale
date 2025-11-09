package dev.robocode.tankroyale.botapi.internal.json;

import dev.robocode.tankroyale.schema.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TR-API-UTL-002 JsonUtil serialization + schema compliance")
class JsonConverterTest {

    @Test
    @Tag("UTL")
    @Tag("TR-API-UTL-002")
    void givenScannedBotEvent_whenSerializedAndDeserialized_thenFieldsArePreserved() {
        // Arrange
        var evt = new ScannedBotEvent();
        evt.setType(Message.Type.SCANNED_BOT_EVENT);
        evt.setTurnNumber(1);
        evt.setScannedByBotId(2);
        evt.setScannedBotId(3);
        evt.setEnergy(100.0);
        evt.setX(10.0);
        evt.setY(20.0);
        evt.setDirection(90.0);
        evt.setSpeed(5.0);

        // Act
        var json = JsonConverter.toJson(evt);
        var deserialized = JsonConverter.fromJson(json, ScannedBotEvent.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getType()).isEqualTo(evt.getType());
        assertThat(deserialized.getTurnNumber()).isEqualTo(evt.getTurnNumber());
        assertThat(deserialized.getScannedByBotId()).isEqualTo(evt.getScannedByBotId());
        assertThat(deserialized.getScannedBotId()).isEqualTo(evt.getScannedBotId());
        assertThat(deserialized.getEnergy()).isEqualTo(evt.getEnergy());
        assertThat(deserialized.getX()).isEqualTo(evt.getX());
        assertThat(deserialized.getY()).isEqualTo(evt.getY());
        assertThat(deserialized.getDirection()).isEqualTo(evt.getDirection());
        assertThat(deserialized.getSpeed()).isEqualTo(evt.getSpeed());
    }

    @Test
    @Tag("UTL")
    @Tag("TR-API-UTL-002")
    void givenBulletFiredEventWithNestedBulletState_whenSerializedAndDeserialized_thenNestedFieldsArePreserved() {
        // Arrange
        var bullet = new BulletState();
        bullet.setBulletId(1);
        bullet.setOwnerId(2);
        bullet.setPower(2.5);
        bullet.setX(100.0);
        bullet.setY(200.0);
        bullet.setDirection(45.0);
        bullet.setColor("#FF0000");

        var evt = new BulletFiredEvent();
        evt.setType(Message.Type.BULLET_FIRED_EVENT);
        evt.setTurnNumber(7);
        evt.setBullet(bullet);

        // Act
        var json = JsonConverter.toJson(evt);
        var deserialized = JsonConverter.fromJson(json, BulletFiredEvent.class);

        // Assert
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getType()).isEqualTo(evt.getType());
        assertThat(deserialized.getTurnNumber()).isEqualTo(evt.getTurnNumber());
        assertThat(deserialized.getBullet()).isNotNull();
        assertThat(deserialized.getBullet().getBulletId()).isEqualTo(bullet.getBulletId());
        assertThat(deserialized.getBullet().getOwnerId()).isEqualTo(bullet.getOwnerId());
        assertThat(deserialized.getBullet().getPower()).isEqualTo(bullet.getPower());
        assertThat(deserialized.getBullet().getX()).isEqualTo(bullet.getX());
        assertThat(deserialized.getBullet().getY()).isEqualTo(bullet.getY());
        assertThat(deserialized.getBullet().getDirection()).isEqualTo(bullet.getDirection());
        assertThat(deserialized.getBullet().getColor()).isEqualTo(bullet.getColor());
    }
}
