package fi.livi.digitraffic.tie.dao.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractJpaTest;
import fi.livi.digitraffic.tie.DataDatex2SituationTestHelper;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.TrafficAnnouncementProperties.SituationType;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;
import fi.livi.digitraffic.tie.model.data.SituationMessage;
import fi.livi.digitraffic.tie.model.data.SituationMqttMessage;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2Version;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SimppeliVersion;
import fi.livi.digitraffic.tie.service.data.DatexIIService;

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
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        helper.insertSituation("GUID-DAO-001", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, Instant.now().minus(1, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(),
                "Latest version has expired end_time — situation must not be returned.");
    }

    /**
     * Single version with no end_time is returned as active.
     */
    @Test
    public void findMessagesByType_singleVersionNoEndTime_isReturned() throws Exception {
        helper.insertSituation("GUID-DAO-002", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(5, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages.size(), "A situation with no end_time should be returned as active.");
    }

    /**
     * Single version with an expired end_time is not returned.
     */
    @Test
    public void findMessagesByType_singleVersionWithExpiredEndTime_isNotReturned() throws Exception {
        helper.insertSituation("GUID-DAO-003", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(5, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

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
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        helper.insertSituation("GUID-DAO-004", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, Instant.now().plus(1, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

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
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        helper.insertSituation("GUID-DAO-005b", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, Instant.now().plus(2, ChronoUnit.HOURS));

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(2, messages.size(), "Two distinct active situations should produce two messages.");
    }

    /**
     * Only situations matching the requested situation type are returned.
     */
    @Test
    public void findMessagesByType_situationTypeFilterIsRespected() throws Exception {
        helper.insertSituation("GUID-DAO-006", 1L, SituationType.ROAD_WORK,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(5, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

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
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        final var messages37 = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_7.version);

        assertEquals(0, messages37.size(), "A 3.5 message must not be returned when requesting 3.7.");

        final var messages35 = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages35.size(), "A 3.5 message must be returned when requesting 3.5.");
    }

    /**
     * Null messageVersion matches all versions.
     */
    @Test
    public void findMessagesByType_nullMessageVersion_matchesAllVersions() throws Exception {
        final Instant startTime = Instant.now().minus(5, ChronoUnit.HOURS);

        helper.insertSituation("GUID-DAO-008a", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-008b", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37,
                startTime, null);

        final var messages = dataDatex2SituationRepository.findMessagesByType(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.DATEX2.value(), null);

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
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-010", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationId(
                "GUID-DAO-010", MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(1, messages.size(), "Should return only the message from the latest version.");
    }

    /**
     * Returns empty when the messageVersion doesn't match.
     */
    @Test
    public void findLatestMessagesBySituationId_wrongVersion_returnsEmpty() throws Exception {
        helper.insertSituation("GUID-DAO-011", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(1, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationId(
                "GUID-DAO-011", MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_7.version);

        assertEquals(0, messages.size(), "Wrong version should return empty.");
    }

    /**
     * Returns empty when the situationId doesn't exist.
     */
    @Test
    public void findLatestMessagesBySituationId_unknownSituationId_returnsEmpty() {
        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationId(
                "UNKNOWN-ID", MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

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
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-020", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);
        helper.insertSituation("GUID-DAO-020", 3L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                startTime, null);

        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationId(
                "GUID-DAO-020", MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(3, messages.size(), "Should return messages from all 3 versions.");
    }

    /**
     * Returns empty for an unknown situationId.
     */
    @Test
    public void findAllMessagesBySituationId_unknownSituationId_returnsEmpty() {
        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationId(
                "UNKNOWN-ID", MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_5.version);

        assertEquals(0, messages.size(), "Unknown situationId should return empty.");
    }

    /**
     * Messages from wrong messageVersion are excluded.
     */
    @Test
    public void findAllMessagesBySituationId_wrongVersion_returnsEmpty() throws Exception {
        helper.insertSituation("GUID-DAO-021", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35,
                Instant.now().minus(1, ChronoUnit.HOURS), null);

        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationId(
                "GUID-DAO-021", MessageTypeEnum.DATEX2.value(), Datex2Version.V_3_7.version);

        assertEquals(0, messages.size(), "Wrong version should return empty.");
    }

    // -------------------------------------------------------------------------
    // findActiveSituationMessages — SIMPPELI + DATEX2 3.7 pairing (Waze feed)
    // -------------------------------------------------------------------------

    /**
     * An active situation carrying both a SIMPPELI and a DATEX2 3.7 message returns both rows.
     * <p>
     * This also verifies that the SpEL expressions in the query resolve to the values actually
     * stored in the database, which a wrong constant name would not: the expression is a string,
     * so it compiles and runs either way and simply matches no rows.
     */
    @Test
    public void findActiveSituationMessages_pairedSimppeliAndDatex37_returnsBoth() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-030", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_VERSION,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37)));

        final var messages = dataDatex2SituationRepository.findActiveSituationMessages(
                List.of(SituationType.TRAFFIC_ANNOUNCEMENT.name()), Instant.now(), FAR_FUTURE, DatexIIService.MAX_SIMPPELI_VERSION);

        assertEquals(2, messages.size(), "Both SIMPPELI and DATEX2 3.7 messages should be returned.");
        assertEquals(Set.of(MessageTypeEnum.SIMPPELI.value(), MessageTypeEnum.DATEX2.value()),
                messages.stream().map(SituationMessage::getMessageType).collect(Collectors.toSet()),
                "Query must match the message_type values stored in the database.");
    }

    /**
     * DATEX2 messages of other versions (e.g. 2.2.3, 3.5) must be filtered out, while the
     * SIMPPELI message of the same situation is still returned.
     */
    @Test
    public void findActiveSituationMessages_otherDatex2Versions_areExcluded() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-031", 1L, SituationType.ROAD_WORK,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_VERSION,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_2_2_3.version, MSG_35),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35)));

        final var messages = dataDatex2SituationRepository.findActiveSituationMessages(
                List.of(SituationType.ROAD_WORK.name()), Instant.now(), FAR_FUTURE, DatexIIService.MAX_SIMPPELI_VERSION);

        assertEquals(1, messages.size(), "Only the SIMPPELI message should be returned.");
        assertEquals(MessageTypeEnum.SIMPPELI.value(), messages.getFirst().getMessageType());
    }

    /**
     * Situations whose latest version has an expired end_time are not returned.
     */
    @Test
    public void findActiveSituationMessages_expiredSituation_isNotReturned() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-032", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(5, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS),
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_VERSION,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37)));

        final var messages = dataDatex2SituationRepository.findActiveSituationMessages(
                List.of(SituationType.TRAFFIC_ANNOUNCEMENT.name()), Instant.now(), FAR_FUTURE, DatexIIService.MAX_SIMPPELI_VERSION);

        assertEquals(0, messages.size(), "Expired situation must not be returned.");
    }

    /**
     * Situation types outside the requested list are excluded.
     */
    @Test
    public void findActiveSituationMessages_otherSituationType_isExcluded() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-033", 1L, SituationType.ROAD_WORK,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                        MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37)));

        final var messages = dataDatex2SituationRepository.findActiveSituationMessages(
                List.of(SituationType.TRAFFIC_ANNOUNCEMENT.name()), Instant.now(), FAR_FUTURE, DatexIIService.MAX_SIMPPELI_VERSION);

        assertEquals(0, messages.size(), "ROAD_WORK must not be returned when only TRAFFIC_ANNOUNCEMENT is requested.");
    }

    /**
     * A situation is commonly stored with several SIMPPELI versions, all written in the same transaction
     * and therefore sharing an identical {@code modified_at}. Ordering by time alone is a tie, which would
     * leave the caller picking a version at random, so the newest allowed version must come first.
     */
    @Test
    public void findActiveSituationMessages_multipleSimppeliVersions_newestFirst() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-034", 1L, SituationType.ROAD_WORK,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, SimppeliVersion.V_0_2_17.version,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, SimppeliVersion.V_0_2_18.version,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37)));

        final var messages = dataDatex2SituationRepository.findActiveSituationMessages(
                List.of(SituationType.ROAD_WORK.name()), Instant.now(), FAR_FUTURE, DatexIIService.MAX_SIMPPELI_VERSION);

        final var simppeliVersions = messages.stream()
                .filter(m -> MessageTypeEnum.SIMPPELI.value().equals(m.getMessageType()))
                .map(SituationMessage::getMessageVersion)
                .toList();

        assertEquals(List.of(SimppeliVersion.V_0_2_18.version, SimppeliVersion.V_0_2_17.version), simppeliVersions,
                "The newest SIMPPELI version must come first so the caller's first-wins pick is deterministic.");
    }

    // -------------------------------------------------------------------------
    // MQTT queries — SIMPPELI version deduplication
    // -------------------------------------------------------------------------

    /**
     * A situation stored with several SIMPPELI versions must yield exactly one MQTT message,
     * the newest version up to the given maximum. Without this the same situation would be
     * published twice to the same MQTT topic.
     */
    @Test
    public void findMessagesForMqtt_versionsAboveMax_areExcluded() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-041", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, SimppeliVersion.V_0_2_17.version,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, SimppeliVersion.V_0_2_18.version,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE)));

        final var messages = dataDatex2SituationRepository.findMessagesForMqtt(
                Instant.now().minus(1, ChronoUnit.HOURS), MessageTypeEnum.SIMPPELI.value(),
                SimppeliVersion.V_0_2_17.version);

        assertEquals(1, messages.size(), "Only one SIMPPELI message per situation should be returned.");
        assertEquals(SimppeliVersion.V_0_2_17.version, messages.getFirst().getMessageVersion(),
                "Versions newer than the maximum must be excluded.");
    }

    /**
     * The combined MQTT query deduplicates SIMPPELI versions but returns every DATEX2 version,
     * because DATEX2 messages are published to version-specific topics.
     */
    @Test
    public void findMessagesForMqtt_deduplicatesSimppeliButKeepsAllDatex2Versions() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-042", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, SimppeliVersion.V_0_2_17.version,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, SimppeliVersion.V_0_2_18.version,
                                DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_5.version, MSG_35),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37)));

        final var messages = dataDatex2SituationRepository.findMessagesForMqtt(
                Instant.now().minus(1, ChronoUnit.HOURS), MessageTypeEnum.SIMPPELI.value(),
                SimppeliVersion.V_0_2_18.version);

        final var simppeliVersions = messages.stream()
                .filter(m -> MessageTypeEnum.SIMPPELI.value().equals(m.getMessageType()))
                .map(SituationMqttMessage::getMessageVersion)
                .collect(Collectors.toSet());
        final var datex2Versions = messages.stream()
                .filter(m -> MessageTypeEnum.DATEX2.value().equals(m.getMessageType()))
                .map(SituationMqttMessage::getMessageVersion)
                .collect(Collectors.toSet());

        assertEquals(Set.of(SimppeliVersion.V_0_2_18.version), simppeliVersions,
                "Only the newest SIMPPELI version should be published.");
        assertEquals(Set.of(Datex2Version.V_3_5.version, Datex2Version.V_3_7.version), datex2Versions,
                "All supported DATEX2 versions should be published, they use version-specific topics.");
    }

    /**
     * Datex II 2.2.3 has its own MQTT topic, so it is published alongside the newer versions.
     */
    @Test
    public void findMessagesForMqtt_datex2Version223_isReturned() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-043", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_2_2_3.version, MSG_35),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37)));

        final var messages = dataDatex2SituationRepository.findMessagesForMqtt(
                Instant.now().minus(1, ChronoUnit.HOURS), MessageTypeEnum.SIMPPELI.value(),
                SimppeliVersion.V_0_2_18.version);

        assertEquals(Set.of(Datex2Version.V_2_2_3.version, Datex2Version.V_3_7.version),
                messages.stream()
                        .filter(m -> MessageTypeEnum.DATEX2.value().equals(m.getMessageType()))
                        .map(SituationMqttMessage::getMessageVersion)
                        .collect(Collectors.toSet()),
                "Every Datex II version should be published, they use version-specific topics.");
    }

    // -------------------------------------------------------------------------
    // getLastModified — feeds the data-updated API
    // -------------------------------------------------------------------------

    /**
     * Situations of another type must not be reported as an update of the requested type,
     * otherwise the data-updated API would claim data changed when it did not.
     */
    @Test
    public void getLastModified_otherSituationType_isExcluded() throws Exception {
        helper.insertSituation("GUID-DAO-060", 1L, SituationType.ROAD_WORK,
                MessageTypeEnum.DATEX2, Datex2Version.V_3_7.version, MSG_37,
                Instant.now().minus(1, ChronoUnit.HOURS), null);

        assertNull(dataDatex2SituationRepository.getLastModified(SituationType.TRAFFIC_ANNOUNCEMENT.name()),
                "A ROAD_WORK situation must not be reported as a TRAFFIC_ANNOUNCEMENT update.");
        assertNotNull(dataDatex2SituationRepository.getLastModified(SituationType.ROAD_WORK.name()),
                "A ROAD_WORK situation must be reported for ROAD_WORK.");
    }

    // -------------------------------------------------------------------------
    // REST Simppeli queries — SIMPPELI version deduplication
    // Mirrors the MQTT deduplication: only the newest version up to the maximum
    // may be returned, otherwise the same situation appears twice in the response.
    // -------------------------------------------------------------------------

    /**
     * A situation stored with several SIMPPELI versions must produce exactly one feature,
     * the newest version up to the maximum.
     */
    @Test
    public void findMessagesByTypeUpToVersion_multipleSimppeliVersions_returnsNewestOnly() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-050", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.17", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.18", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE)));

        final var messages = dataDatex2SituationRepository.findMessagesByTypeUpToVersion(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.SIMPPELI.value(), "0.2.18");

        assertEquals(1, messages.size(), "A situation with two SIMPPELI versions must yield exactly one message.");
    }

    /**
     * SIMPPELI versions above the maximum must be ignored, so an unsupported newer version
     * does not reach the conversion code.
     */
    @Test
    public void findMessagesByTypeUpToVersion_versionsAboveMax_areExcluded() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-051", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                        MessageTypeEnum.SIMPPELI, "0.2.19", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE)));

        final var messages = dataDatex2SituationRepository.findMessagesByTypeUpToVersion(
                SituationType.TRAFFIC_ANNOUNCEMENT.name(), Instant.now(), FAR_FUTURE, null,
                MessageTypeEnum.SIMPPELI.value(), "0.2.18");

        assertEquals(0, messages.size(), "Versions above the maximum must not be returned.");
    }

    /**
     * The single situation endpoint must also collapse multiple SIMPPELI versions into one.
     */
    @Test
    public void findLatestMessagesBySituationIdUpToVersion_multipleSimppeliVersions_returnsNewestOnly() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-052", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.17", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.18", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE)));

        final var messages = dataDatex2SituationRepository.findLatestMessagesBySituationIdUpToVersion(
                "GUID-DAO-052", MessageTypeEnum.SIMPPELI.value(), "0.2.18");

        assertEquals(1, messages.size(), "Two SIMPPELI versions of the latest situation version must yield one message.");
    }

    /**
     * The history endpoint returns every situation version, but only one message per version.
     */
    @Test
    public void findAllMessagesBySituationIdUpToVersion_multipleSimppeliVersions_returnsOnePerSituationVersion() throws Exception {
        helper.insertSituationWithMessages("GUID-DAO-053", 1L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(2, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.17", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.18", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE)));
        helper.insertSituationWithMessages("GUID-DAO-053", 2L, SituationType.TRAFFIC_ANNOUNCEMENT,
                Instant.now().minus(1, ChronoUnit.HOURS), null,
                List.of(new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.17", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE),
                        new DataDatex2SituationTestHelper.MessageSpec(
                                MessageTypeEnum.SIMPPELI, "0.2.18", DataDatex2SituationTestHelper.DEFAULT_SIMPPELI_MESSAGE)));

        final var messages = dataDatex2SituationRepository.findAllMessagesBySituationIdUpToVersion(
                "GUID-DAO-053", MessageTypeEnum.SIMPPELI.value(), "0.2.18");

        assertEquals(2, messages.size(),
                "Two situation versions with two SIMPPELI versions each must yield one message per situation version.");
    }
}
