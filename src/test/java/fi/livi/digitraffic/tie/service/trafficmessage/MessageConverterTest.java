package fi.livi.digitraffic.tie.service.trafficmessage;

import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;
import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.readStaticImsJmessageResourceContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.controller.trafficmessage.MessageConverter;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.RoadWorkPhase;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.WeekdayTimePeriod;
import fi.livi.digitraffic.tie.dto.trafficmessage.v2.WorkType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests the v2 deserialization path (MessageConverter.convertToFeature) which reads
 * raw stored SIMPPELI JSON and deserializes directly to v2 DTOs without any pre-processing.
 * This is the v2 equivalent of the road work phase assertions in TrafficMessageImsJsonConverterV1Test.
 * Key regression: The source system sends "worktypes" (all lowercase) but the Java field
 * is "workTypes" (camelCase). Ensure the DTO deserialization accepts the "worktypes" JSON key
 * so the data is not silently dropped, while still serializing the API response as "workTypes".
 */
public class MessageConverterTest extends AbstractServiceTest {

    @Autowired
    private MessageConverter messageConverter;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that the v2 deserialization path correctly maps all road work phase fields,
     * including "worktypes" (lowercase JSON key) → workTypes (camelCase field).
     * This covers the v2 API path (MessageConverter → DatexIIService.convertSimppeli),
     * which reads the raw stored SIMPPELI JSON without any pre-processing.
     */
    @Test
    void roadWorkPhaseFieldsAreCorrectlyDeserializedInV2Path() throws Exception {
        final String json = readStaticImsJmessageResourceContent(
                ImsJsonVersion.getLatestVersion(),
                SituationType.ROAD_WORK.name(),
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(1, ChronoUnit.HOURS),
                false
        );

        final TrafficAnnouncementFeature feature = messageConverter.convertToFeature(json, false);
        final RoadWorkPhase phase = feature.getProperties().announcements.getFirst().roadWorkPhases.getFirst();

        // "worktypes" (lowercase) from source must deserialize into workTypes, while API output remains "workTypes"
        assertFalse(phase.workTypes.isEmpty(),
                "workTypes must not be empty — \"worktypes\" JSON key was not mapped to RoadWorkPhase.workTypes.");
        assertEquals(WorkType.Type.LIGHTING, phase.workTypes.getFirst().type);
        assertEquals(WorkType.Type.CULVERT_REPLACEMENT, phase.workTypes.get(1).type);

        // location and locationDetails
        assertNotNull(phase.location);
        assertNotNull(phase.locationDetails);
        assertNotNull(phase.locationDetails.roadAddressLocation);

        // severity
        assertNotNull(phase.severity);
        assertEquals(RoadWorkPhase.Severity.HIGH, phase.severity);

        // workingHours — verify LocalTime parsing works with source time strings (HH:mm, HH:mm:ss, HH:mm:ss.SSS are all valid ISO 8601)
        assertFalse(phase.workingHours.isEmpty());
        final WeekdayTimePeriod firstWorkingHour = phase.workingHours.getFirst();
        assertEquals(WeekdayTimePeriod.Weekday.MONDAY, firstWorkingHour.weekday);
        assertEquals(LocalTime.of(9, 30, 0), firstWorkingHour.startTime);
        assertEquals(LocalTime.of(15, 0, 0), firstWorkingHour.endTime);

        // slowTrafficTimes and queuingTrafficTimes
        assertFalse(phase.slowTrafficTimes.isEmpty());
        assertEquals(WeekdayTimePeriod.Weekday.TUESDAY, phase.slowTrafficTimes.getFirst().weekday);
        assertFalse(phase.queuingTrafficTimes.isEmpty());
        assertEquals(WeekdayTimePeriod.Weekday.WEDNESDAY, phase.queuingTrafficTimes.getFirst().weekday);

        // timeAndDuration
        assertNotNull(phase.timeAndDuration);
        assertNotNull(phase.timeAndDuration.startTime);
    }

    /**
     * Confirms that WeekdayTimePeriod:
     * - Deserializes all valid ISO 8601 local time variants: HH:mm, HH:mm:ss, HH:mm:ss.SSS
     * - Serializes as "HH:mm" (no seconds) via HHmmLocalTimeSerializer
     */
    @Test
    void weekdayTimePeriodLocalTimeSerializationAndDeserialization() throws tools.jackson.core.JacksonException {
        final LocalTime expectedTime = LocalTime.of(9, 30);

        // All ISO 8601 local time variants must deserialize correctly
        for (final String timeString : List.of("09:30", "09:30:00", "09:30:00.000")) {
            final String json = "{\"weekday\":\"Monday\",\"startTime\":\"" + timeString + "\",\"endTime\":\"15:00\"}";
            final WeekdayTimePeriod period = objectMapper.readerFor(WeekdayTimePeriod.class).readValue(json);
            assertEquals(expectedTime, period.startTime,
                    "Failed to deserialize startTime from: " + timeString);
        }

        // Serialization must use HH:mm — no seconds in API output
        final WeekdayTimePeriod period = new WeekdayTimePeriod(
                WeekdayTimePeriod.Weekday.MONDAY,
                LocalTime.of(9, 30, 10),
                LocalTime.of(15, 0, 10));
        final String serialized = objectMapper.writeValueAsString(period);
        assertTrue(serialized.contains("\"09:30\""),
                "startTime should serialize as \"09:30\" (no seconds), got: " + serialized);
        assertTrue(serialized.contains("\"15:00\""),
                "endTime should serialize as \"15:00\" (no seconds), got: " + serialized);
    }

    /**
     * Confirms that v1 WeekdayTimePeriod:
     * - Deserializes all valid ISO 8601 local time variants: HH:mm, HH:mm:ss, HH:mm:ss.SSS
     * - Serializes as "HH:mm:ss" via HHmmssLocalTimeSerializer
     */
    @Test
    void v1WeekdayTimePeriodLocalTimeSerializationAndDeserialization() throws tools.jackson.core.JacksonException {
        final LocalTime expectedTime = LocalTime.of(9, 30);

        // All ISO 8601 local time variants must deserialize correctly
        for (final String timeString : List.of("09:30", "09:30:00", "09:30:00.000")) {
            final String json = "{\"weekday\":\"MONDAY\",\"startTime\":\"" + timeString + "\",\"endTime\":\"15:00\"}";
            final fi.livi.digitraffic.tie.dto.trafficmessage.v1.WeekdayTimePeriod period =
                    objectMapper.readerFor(fi.livi.digitraffic.tie.dto.trafficmessage.v1.WeekdayTimePeriod.class).readValue(json);
            assertEquals(expectedTime, period.startTime,
                    "Failed to deserialize v1 startTime from: " + timeString);
        }

        // Serialization must use HH:mm:ss
        final fi.livi.digitraffic.tie.dto.trafficmessage.v1.WeekdayTimePeriod period =
                new fi.livi.digitraffic.tie.dto.trafficmessage.v1.WeekdayTimePeriod(
                        fi.livi.digitraffic.tie.dto.trafficmessage.v1.WeekdayTimePeriod.Weekday.MONDAY,
                        LocalTime.of(9, 30, 0),
                        LocalTime.of(15, 0, 0));
        final String serialized = objectMapper.writeValueAsString(period);
        assertTrue(serialized.contains("\"09:30:00\""),
                "v1 startTime should serialize as \"09:30:00\", got: " + serialized);
        assertTrue(serialized.contains("\"15:00:00\""),
                "v1 endTime should serialize as \"15:00:00\", got: " + serialized);
    }
}
