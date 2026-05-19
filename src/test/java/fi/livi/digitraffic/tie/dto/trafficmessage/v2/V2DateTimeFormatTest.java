package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;

public class V2DateTimeFormatTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    static class TestDto {
        @JsonSerialize(using = V2DateTimeFormat.Serializer.class)
        @JsonDeserialize(using = V2DateTimeFormat.Deserializer.class)
        public Instant time;
    }

    @ParameterizedTest
    @CsvSource({
            // input, expected Instant ISO string
            "2026-05-12T07:20:00Z,            2026-05-12T07:20:00Z",
            "2026-05-12T07:20:00.0Z,          2026-05-12T07:20:00Z",
            "2026-05-12T07:20:00.00Z,         2026-05-12T07:20:00Z",
            "2026-05-12T07:20:00.000Z,        2026-05-12T07:20:00Z",
            "2026-05-12T07:20:00.5Z,          2026-05-12T07:20:00.500Z",
            "2026-05-12T07:20:00.12Z,         2026-05-12T07:20:00.120Z",
            "2026-05-12T07:20:00.123Z,        2026-05-12T07:20:00.123Z",
            "2026-05-12T07:20:00.1234Z,       2026-05-12T07:20:00.123400Z",
            "2026-05-12T07:20:00.123456789Z,  2026-05-12T07:20:00.123456789Z",
    })
    void testDeserialization(final String input, final String expectedInstant) {
        final String json = "{\"time\":\"" + input.trim() + "\"}";
        final TestDto dto = MAPPER.readValue(json, TestDto.class);
        Assertions.assertEquals(Instant.parse(expectedInstant.trim()), dto.time);
    }

    @ParameterizedTest
    @CsvSource({
            // input Instant ISO string, expected serialized output
            "2026-05-12T07:20:00Z,           2026-05-12T07:20:00.000Z",
            "2026-05-12T07:20:00.500Z,       2026-05-12T07:20:00.500Z",
            "2026-05-12T07:20:00.123Z,       2026-05-12T07:20:00.123Z",
            "2026-05-12T07:20:00.123456789Z, 2026-05-12T07:20:00.123Z",
    })
    void testSerialization(final String inputInstant, final String expectedOutput) {
        final TestDto dto = new TestDto();
        dto.time = Instant.parse(inputInstant.trim());
        final String json = MAPPER.writeValueAsString(dto);
        Assertions.assertTrue(json.contains("\"" + expectedOutput.trim() + "\""),
                "Expected " + expectedOutput.trim() + " in: " + json);
    }

    @Test
    void testSerializationAlwaysHasMillis() {
        final TestDto dto = new TestDto();
        dto.time = Instant.parse("2026-05-12T07:20:00Z");
        final String json = MAPPER.writeValueAsString(dto);
        Assertions.assertTrue(json.contains(".000Z"),
                "Output should always contain .000Z, got: " + json);
    }
}
