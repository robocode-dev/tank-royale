package dev.robocode.tankroyale.botapi.mapper;

import dev.robocode.tankroyale.botapi.InitialPosition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitialPositionMapperTest {

    @Test
    @DisplayName("TR-API-VAL-004 InitialPosition mapping round-trip: mapper preserves values")
    @Tag("VAL")
    @Tag("TR-API-VAL-004")
    void test_TR_API_VAL_004_mapper_preserves_values() {
        // Arrange
        var ip = InitialPosition.fromString("11, 22, 33");

        // Act
        var schemaIp = InitialPositionMapper.map(ip);

        // Assert
        assertThat(schemaIp).isNotNull();
        assertThat(schemaIp.getX()).isEqualTo(11.0);
        assertThat(schemaIp.getY()).isEqualTo(22.0);
        assertThat(schemaIp.getDirection()).isEqualTo(33.0);

        // Round-trip back via values to InitialPosition (like-for-like)
        var ip2 = InitialPosition.fromString(
                schemaIp.getX() + ", " + schemaIp.getY() + ", " + schemaIp.getDirection());
        assertThat(ip2).isNotNull();
        assertThat(ip2.getX()).isEqualTo(ip.getX());
        assertThat(ip2.getY()).isEqualTo(ip.getY());
        assertThat(ip2.getDirection()).isEqualTo(ip.getDirection());
    }

    @Test
    @DisplayName("TR-API-VAL-004 InitialPosition mapping handles null and partial values")
    @Tag("VAL")
    @Tag("TR-API-VAL-004")
    void test_TR_API_VAL_004_mapper_handles_nulls() {
        // Null source -> null schema
        var schemaNull = InitialPositionMapper.map(null);
        assertThat(schemaNull).isNull();

        // Partial values preserved
        var ipXOnly = InitialPosition.fromString("50");
        var schemaXOnly = InitialPositionMapper.map(ipXOnly);
        assertThat(schemaXOnly).isNotNull();
        assertThat(schemaXOnly.getX()).isEqualTo(50.0);
        assertThat(schemaXOnly.getY()).isNull();
        assertThat(schemaXOnly.getDirection()).isNull();
    }
}
