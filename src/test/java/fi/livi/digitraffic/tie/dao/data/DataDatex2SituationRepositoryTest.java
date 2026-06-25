package fi.livi.digitraffic.tie.dao.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.DataDatex2SituationTestHelper;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties.SituationType;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;

/**
 * DAO-level tests for {@link DataDatex2SituationRepository#findMessagesByType},
 * {@link DataDatex2SituationRepository#findLatestMessagesBySituationId} and
 * {@link DataDatex2SituationRepository#findAllMessagesBySituationId}.
 * <p>
 * The queries use an {@code is_latest_version} flag (maintained by a DB trigger) to ensure
 * only the latest version per {@code situation_id} is considered before the time filter is applied.
 */
public class DataDatex2SituationRepositoryTest extends AbstractJpaTest {

    /**
     * Year ~3000 — mirrors the TIME_END sentinel used in DatexIIService.
     */
    private static final Instant FAR_FUTURE = Instant.ofEpochMilli(32503683600000L);

    private static final String MSG_35 = "<d2:payload/>";
    private static final String MSG_37 = "<d2:payload/>";

    @Autowired
    private DataDatex2SituationRepository dataDatex2SituationRepository;

    private DataDatex2SituationTestHelper helper;

    @BeforeEach
    void setUpHelper() {
        helper = new DataDatex2SituationTestHelper(dataDatex2SituationRepository);
    }

    // -------------------------------------------------------------------------
    // findMessagesByType — time-window and is_latest_version behaviour
    // -------------------------------------------------------------------------

    /**
     * When the latest version has an expired end_time the situation must not be returned,
     * even if an older version for the same situation_id has no end_time.
     */
    @Test
    public void findMessagesByType_latestVersionWithExpiredEndTime_isNotReturned() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-001", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        helper.insertSituation("GUID-DAO-001", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, Instant.now().minus(1, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(),
                "Latest version has expired end_time — situation must not be returned.");
    }

    /**
     * Single version with no end_time is returned as active.
     */
    @Test
    public void findMessagesByType_singleVersionNoEndTime_isReturned() throws Exception {
        helper.insertSituation("GUID-DAO-002", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(5, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages.size(), "A situation with no end_time should be returned as active.");
    }

    /**
     * Single version with an expired end_time is not returned.
     */
    @Test
    public void findMessagesByType_singleVersionWithExpiredEndTime_isNotReturned() throws Exception {
        helper.insertSituation("GUID-DAO-003", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(5, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(), "A situation with an expired end_time must not be returned.");
    }

    /**
     * When the latest version has a future end_time, exactly one message is returned
     * regardless of older versions.
     */
    @Test
    public void findMessagesByType_latestVersionWithFutureEndTime_isReturned() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-004", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        helper.insertSituation("GUID-DAO-004", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, Instant.now().plus(1, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages.size(),
                "Latest version has a future end_time — exactly one message should be returned.");
    }

    /**
     * Two distinct active situations each produce one message.
     */
    @Test
    public void findMessagesByType_twoDistinctActiveSituations_bothReturned() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-005a", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        helper.insertSituation("GUID-DAO-005b", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, Instant.now().plus(2, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(2, messages.size(), "Two distinct active situations should produce two messages.");
    }

    /**
     * Only situations matching the requested situation type are returned.
     */
    @Test
    public void findMessagesByType_situationTypeFilterIsRespected() throws Exception {
        helper.insertSituation("GUID-DAO-006", 1L, SituationType.ROAD_WORK,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(5, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(),
                "A ROAD_WORK situation must not appear in TRAFFIC_ANNOUNCEMENT results.");
    }

    /**
     * Only messages with the requested messageVersion are returned; other versions are excluded.
     */
    @Test
    public void findMessagesByType_messageVersionFilterIsRespected() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-007", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        final var messages37 = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_7.version);

        assertEquals(0, messages37.size(), "A 3.5 message must not be returned when requesting 3.7.");

        final var messages35 = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages35.size(), "A 3.5 message must be returned when requesting 3.5.");
    }

    /**
     * Null messageVersion matches all versions.
     */
    @Test
    public void findMessagesByType_nullMessageVersion_matchesAllVersions() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-008a", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-008b", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_7.version, MSG_37,
                startTime, null);

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX_2.value(), null);

        assertEquals(2, messages.size(), "Null messageVersion should match messages of any version.");
    }

    // -------------------------------------------------------------------------
    // findLatestMessagesBySituationId
    // -------------------------------------------------------------------------

    /**
     * Returns only the message from the latest situation version, not older versions.
     */
    @Test
    public void findLatestMessagesBySituationId_returnsLatestVersionOnly() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-010", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-010", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationId(
                "GUID-DAO-010", MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages.size(), "Should return only the message from the latest version.");
    }

    /**
     * Returns empty when the messageVersion doesn't match.
     */
    @Test
    public void findLatestMessagesBySituationId_wrongVersion_returnsEmpty() throws Exception {
        helper.insertSituation("GUID-DAO-011", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(1, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationId(
                "GUID-DAO-011", MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_7.version);

        assertEquals(0, messages.size(), "Wrong version should return empty.");
    }

    /**
     * Returns empty when the situationId doesn't exist.
     */
    @Test
    public void findLatestMessagesBySituationId_unknownSituationId_returnsEmpty() {
        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationId(
                "UNKNOWN-ID", MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(), "Unknown situationId should return empty.");
    }

    // -------------------------------------------------------------------------
    // findAllMessagesBySituationId
    // -------------------------------------------------------------------------

    /**
     * Returns messages from all historical versions, newest first.
     */
    @Test
    public void findAllMessagesBySituationId_returnsAllVersions() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-020", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-020", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-020", 3L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationId(
                "GUID-DAO-020", MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(3, messages.size(), "Should return messages from all 3 versions.");
    }

    /**
     * Returns empty for an unknown situationId.
     */
    @Test
    public void findAllMessagesBySituationId_unknownSituationId_returnsEmpty() {
        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationId(
                "UNKNOWN-ID", MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(), "Unknown situationId should return empty.");
    }

    /**
     * Messages from wrong messageVersion are excluded.
     */
    @Test
    public void findAllMessagesBySituationId_wrongVersion_returnsEmpty() throws Exception {
        helper.insertSituation("GUID-DAO-021", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX_2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(1, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationId(
                "GUID-DAO-021", MessageTypeEnum.DATEX_2.value(), Datex2Version.V_3_7.version);

        assertEquals(0, messages.size(), "Wrong version should return empty.");
    }
}
