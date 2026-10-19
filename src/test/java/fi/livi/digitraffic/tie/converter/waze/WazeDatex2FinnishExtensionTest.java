package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.Contact;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementProperties;
import fi.livi.digitraffic.tie.dto.wazefeed.WazeDatex2FeatureDto;
import fi.livi.digitraffic.tie.helper.WazeDatex2MessageConverter;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII37XmlMarshaller;

/**
 * Covers the situations that used to depend on the Finnish Datex II 2.2.3 extensions, now that the
 * Waze feed is served Datex II 3.7 only
 * (see {@code DataDatex2SituationRepository#findActiveSituationMessages}).
 * <p>
 * How the dropped extensions are expressed in 3.7, established from real TLOIK exports:
 * <ul>
 * <li><b>Ice road</b> — 3.7 cannot express it at all: the value is absent from
 * {@code RoadOrCarriagewayOrLaneManagementTypeEnum} and the {@code _extendedValue} escape hatch is
 * itself restricted to a set that does not include it. Both states therefore arrive as {@code other}
 * with no extension value, and only the Finnish free text ("Jäätie auki." / "Jäätie suljettu.")
 * tells them apart. Note that the word "jäätie" alone appears in the location text of both.</li>
 * <li><b>Repaired fault</b> — the base value stays {@code outOfService} and the repair is signalled
 * by an {@code overallEndTime} in the past, so ordinary validity filtering handles it.</li>
 * <li><b>trafficMayBuildUp</b> — needed no replacement. The paired 2.2.3/3.7 exports of the same
 * situation both carry the plain {@code trafficBuildingUp} trend with
 * {@code probabilityOfOccurrence=riskOf} and no extension, so the extension branch was already dead
 * in 2.2.3.</li>
 * </ul>
 * All payloads are real TLOIK 3.7 exports parsed with the real marshaller, so the tests exercise the
 * actual filtering decisions rather than the fixture text.
 */
public class WazeDatex2FinnishExtensionTest extends AbstractSpringJUnitTest {

    private static final String ICE_ROAD_OPEN = "TrafficSituationIceRoadOpen.xml";
    private static final String ICE_ROAD_CLOSED = "TrafficSituationIceRoadClosed.xml";
    private static final String REPAIRED = "TrafficSituationEquipmentOrSystemFaultRepaired.xml";

    private static final String REPAIRED_END_TIME = "<com:overallEndTime>2025-12-22T16:49:53.748Z</com:overallEndTime>";

    /** What a {@code RoadOrCarriagewayOrLaneManagement} with the base value {@code other} falls back to. */
    private static final String GENERIC_MANAGEMENT_DESCRIPTION = "Road or carriageway or lane management";

    private static final String ICE_ROAD_CLOSED_END_TIME = "<com:overallEndTime>2026-03-23T11:00:00.000Z</com:overallEndTime>";

    @Autowired
    private DatexII37XmlMarshaller datexII37XmlMarshaller;

    @Autowired
    private WazeDatex2MessageConverter messageConverter;

    private static String read(final String fixture) throws IOException {
        return readResourceContent("classpath:wazefeed/" + fixture);
    }

    private WazeDatex2FeatureDto toFeatureDto(final String datex2Message) {
        final SituationPublication publication = datexII37XmlMarshaller.convertToObject(null, datex2Message);

        final TrafficAnnouncementProperties properties = new TrafficAnnouncementProperties(
            "GUID50460693", 1, SituationType.TRAFFIC_ANNOUNCEMENT, null, Instant.now(), Instant.now(),
            new ArrayList<>(), new Contact());

        return new WazeDatex2FeatureDto("GUID50460693", SituationType.TRAFFIC_ANNOUNCEMENT, datex2Message, publication,
            new TrafficAnnouncementFeature(new Point(29.9, 63.1), properties));
    }

    private WazeDatex2FeatureDto featureDtoFor(final String fixture) throws IOException {
        return toFeatureDto(read(fixture));
    }


    // --- Ice road -----------------------------------------------------------------------------

    /**
     * An open ice road is a service announcement rather than an incident, so it must stay out of the
     * Waze feed. This replaces the Datex II 2.2.3 {@code iceRoadOpen} extension check.
     */
    @Test
    public void openIceRoadIsExcludedFromTheWazeFeed() throws IOException {
        final WazeDatex2FeatureDto feature = featureDtoFor(ICE_ROAD_OPEN);

        assertTrue(WazeDatex2Converter.hasIceRoadOpenRecord(feature));
        assertFalse(WazeDatex2Converter.isValidWazeEntry(feature),
            "an open ice road is not an incident");
    }

    /**
     * A closed ice road stays in the feed, matching the Datex II 2.2.3 behaviour where only
     * {@code iceRoadOpen} was filtered out. It also guards the detection against matching the word
     * "jäätie" alone, which appears in the location text of both messages.
     */
    @Test
    public void closedIceRoadIsNotTreatedAsAnOpenIceRoad() throws IOException {
        final WazeDatex2FeatureDto feature = featureDtoFor(ICE_ROAD_CLOSED);

        assertFalse(WazeDatex2Converter.hasIceRoadOpenRecord(feature),
            "'Jäätie suljettu' must not be read as an open ice road");
    }

    /**
     * Datex II 2.2.3 produced the description from the extension value. With the extension gone the
     * description has to come from the Finnish free text, otherwise both ice road states would fall
     * back to the generic "Road or carriageway or lane management" wording.
     */
    @Test
    public void iceRoadDescriptionsAreDerivedFromTheFinnishText() throws IOException {
        final String open = messageConverter.export("GUID50460693", read(ICE_ROAD_OPEN));
        // the closed fixture's validity period has already lapsed, so extend it to get a description
        final String closed = messageConverter.export("GUID50460693",
            read(ICE_ROAD_CLOSED).replace(ICE_ROAD_CLOSED_END_TIME,
                "<com:overallEndTime>" + Instant.now().plus(1, ChronoUnit.DAYS) + "</com:overallEndTime>"));

        assertTrue(open.contains("Ice road open"), open);
        assertTrue(closed.contains("Ice road closed"), closed);
        assertFalse(open.contains(GENERIC_MANAGEMENT_DESCRIPTION), "the generic fallback must not be used: " + open);
        assertFalse(closed.contains(GENERIC_MANAGEMENT_DESCRIPTION), "the generic fallback must not be used: " + closed);
    }

    // --- Repaired equipment fault -------------------------------------------------------------

    /**
     * Datex II 2.2.3 marked a fixed fault with the Finnish extension value {@code repaired}. Datex II
     * 3.7 has no such value: TLOIK keeps the base value {@code outOfService} and signals the repair by
     * closing the validity period, so the record is simply no longer active.
     * <p>
     * This also pins {@code isActiveSituationRecord}: it keys on the end time alone, so a record
     * whose validity has been closed must drop out of the feed even though its fault value still
     * reads {@code outOfService}.
     */
    @Test
    public void repairedFaultIsExcludedFromTheWazeFeed() throws IOException {
        final WazeDatex2FeatureDto feature = featureDtoFor(REPAIRED);

        assertFalse(WazeDatex2Converter.hasActiveSituationRecords(feature),
            "a repaired fault has an overallEndTime in the past");
        assertFalse(WazeDatex2Converter.isValidWazeEntry(feature));
    }

    /**
     * The same fault while it is still broken. Together with
     * {@link #repairedFaultIsExcludedFromTheWazeFeed} this shows that the exclusion is driven by the
     * ended validity and not by the record type or the fault value.
     */
    @Test
    public void ongoingFaultStaysInTheWazeFeed() throws IOException {
        final String stillBroken = read(REPAIRED).replace(REPAIRED_END_TIME,
            "<com:overallEndTime>" + Instant.now().plus(30, ChronoUnit.DAYS) + "</com:overallEndTime>");
        assertFalse(stillBroken.contains(REPAIRED_END_TIME), "the end time should have been moved into the future");

        final WazeDatex2FeatureDto feature = toFeatureDto(stillBroken);

        assertTrue(WazeDatex2Converter.hasActiveSituationRecords(feature));
        assertTrue(WazeDatex2Converter.isValidWazeEntry(feature));
    }
}
