package fi.livi.digitraffic.tie.converter.waze;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractSpringJUnitTest;
import fi.livi.digitraffic.tie.helper.WazeDatex2MessageConverter;

/**
 * Pins the Waze description that {@link WazeDatex2MessageConverter} produces for every
 * {@code situationRecord} type TLOIK actually sends.
 * <p>
 * The fixtures are real Datex II 3.7 exports, one per record type, so every {@code accept(...)}
 * overload is exercised. Their validity times are replaced by placeholders because the converter
 * only describes records that are currently active.
 * <p>
 * This replaces the {@code datex2MessageConversion} test that was dropped together with Datex II
 * 2.2.3. Without it the 2.2.3 -> 3.7 port of this class was untested, which is how
 * {@code TransitInformation} came to render as a wrapper class name and hash code.
 */
public class WazeDatex2MessageConverterTest extends AbstractSpringJUnitTest {

    private static final Map<String, String> CASES = new LinkedHashMap<>();

    static {
        CASES.put("AbnormalTraffic_42572.xml",
            "Accident. Lane closures. Slow traffic.");
        CASES.put("Accident_42573.xml",
            "Accident.");
        CASES.put("AnimalPresenceObstruction_42476.xml",
            "Animals on the road.");
        CASES.put("AuthorityOperation_42291.xml",
            ".");
        CASES.put("ConstructionWorks_42589.xml",
            "Temporary speed limit of 50 km/h. Contraflow. Lane closures. Construction work. Temporary speed limit of 30 km/h. Lane closures. Road or carriageway or lane management. Construction work. Temporary speed limit of 50 km/h. Resurfacing work. Intermittent short term closures. Construction work. Construction work. Temporary speed limit of 50 km/h. Contraflow. Lane closures. Construction work. Lane closures. Road or carriageway or lane management. Construction work.");
        CASES.put("DisturbanceActivity_38005.xml",
            "Demonstration.");
        CASES.put("EnvironmentalObstruction_41368.xml",
            "Flooding.");
        CASES.put("EquipmentOrSystemFault_42215.xml",
            "Traffic light sets repaired.");
        CASES.put("GeneralNetworkManagement_42583.xml",
            "Temporary traffic lights. Temporary speed limit of 50 km/h. Roadworks. Single alternate line traffic.");
        CASES.put("GeneralObstruction_42560.xml",
            "Accident. Lane closures. Rescue and recovery work. Traffic building up.");
        CASES.put("InfrastructureDamageObstruction_36952.xml",
            "Lane closures. Fallen power cables.");
        CASES.put("NonWeatherRelatedRoadConditions_42107.xml",
            "Oil on road.");
        CASES.put("PoorEnvironmentConditions_40076.xml",
            "Smoke hazard.");
        CASES.put("PublicEvent_41349.xml",
            "Public event: major event.");
        CASES.put("ReroutingManagement_42385.xml",
            "Follow local diversion.");
        CASES.put("TransitInformation_42397.xml",
            "Ferry: service not operating.");
        CASES.put("VehicleObstruction_42585.xml",
            "Broken down vehicle.");
        CASES.put("WeatherRelatedRoadConditions_42344.xml",
            "Surface water.");
    }

    @Autowired
    private WazeDatex2MessageConverter converter;

    @Test
    public void everyRecordTypeGetsItsDescription() throws IOException {
        for (final Map.Entry<String, String> testCase : CASES.entrySet()) {
            assertEquals(testCase.getValue(), export(testCase.getKey()), testCase.getKey());
        }
    }

    /**
     * Guards the wrapper unwrapping: in Datex II 3.7 these fields are {@code _XxxEnum} wrappers whose
     * {@code toString()} is the default one, so a naive port leaks
     * "fi.livi.digitraffic.tie.datex2.v3_7._TransitServiceTypeEnum@1a2b3c" into the public feed.
     */
    @Test
    public void transitInformationIsReadableRatherThanAWrapperToString() throws IOException {
        final String description = export("TransitInformation_42397.xml");

        assertEquals("Ferry: service not operating.", description);
    }

    private String export(final String fixture) throws IOException {
        final Instant now = Instant.now();
        final String datex2Message = readResourceContent("classpath:wazefeed/v37/" + fixture)
            .replace("OVERALL_START_TIME", now.minus(1, ChronoUnit.HOURS).toString())
            .replace("OVERALL_END_TIME", now.plus(1, ChronoUnit.HOURS).toString());

        return converter.export("GUID", datex2Message);
    }

    /**
     * The Waze CIFS feed publishes planned closures in advance: Waze schedules them itself from the
     * {@code starttime} we send, and {@code WazeFeedService} deliberately queries a window reaching
     * 3650 days into the future. A record whose validity has not started yet must therefore still be
     * described.
     * <p>
     * Measured against production data on 2026-10-14, requiring the record to have started dropped
     * 31 of 560 active situations (5.5 %) from the feed entirely, because
     * {@code WazeDatex2Converter.isActiveSituationRecord} also gates
     * {@code WazeDatex2Converter.isValidWazeEntry}.
     */
    @Test
    public void recordWhoseValidityHasNotStartedYetIsStillDescribed() throws IOException {
        assertEquals("Accident.", exportWithValidity(5, 8));
    }

    /** Counterpart of the above: a record that has already ended must not be described. */
    @Test
    public void recordThatHasAlreadyEndedIsNotDescribed() throws IOException {
        assertEquals(".", exportWithValidity(-8, -5));
    }

    /**
     * A record without an end time is valid indefinitely, so it has not ended and must be described
     * regardless of its start time. This input made the pre-migration code throw a
     * {@code NullPointerException} from {@code now.isBefore(null)}.
     */
    @Test
    public void recordWithoutEndTimeIsDescribedEvenBeforeItStarts() throws IOException {
        final String datex2Message = readResourceContent("classpath:wazefeed/v37/Accident_42573.xml")
            .replace("OVERALL_START_TIME", Instant.now().plus(5, ChronoUnit.DAYS).toString())
            .replaceAll("<com:overallEndTime>[^<]*</com:overallEndTime>", "");

        assertEquals("Accident.", converter.export("GUID", datex2Message));
    }

    /**
     * Datex II 2.2.3 had a single value {@code brokenDownHeavyLorry}. In 3.7 that value is gone and
     * the same situation arrives as {@code brokenDownVehicle} plus {@code involvedVehicleType}, so
     * both fields have to be read to keep the description as accurate as it was.
     */
    @Test
    public void brokenDownHeavyVehicleKeepsItsVehicleType() throws IOException {
        assertEquals("Broken down heavy lorry.",
            exportFixture("VehicleObstruction_42585.xml",
                message -> message.replace("</sit:vehicleObstructionType>",
                    "</sit:vehicleObstructionType><sit:involvedVehicleType>heavyVehicle</sit:involvedVehicleType>")));
    }

    /**
     * Datex II 2.2.3 had a Finnish extension value {@code repaired} that overrode the fault type.
     * 3.7 dropped it and its fault types only describe a fault, so the record still names the
     * original fault after it has been fixed and the description turns into its opposite. This
     * record is a traffic light set that has been repaired while its fault type still reads
     * {@code outOfService}.
     */
    @Test
    public void repairedEquipmentIsNotAnnouncedAsBroken() throws IOException {
        assertEquals("Traffic light sets repaired.",
            exportFixture("EquipmentOrSystemFault_42215.xml", UnaryOperator.identity()));
    }

    /**
     * The word alone must not count: it also appears mid-sentence about something other than the
     * equipment, so only a sentence of its own means the equipment was repaired. The record's own
     * repair sentence is replaced here, leaving only the mid-sentence use.
     */
    @Test
    public void repairedIsReadOnlyAsASentenceOfItsOwn() throws IOException {
        assertEquals("Traffic light sets out of service.",
            exportFixture("EquipmentOrSystemFault_42215.xml",
                message -> message.replace("Korjattu.", "Lisätieto: Sortuma korjattu")));
    }

    private String exportFixture(final String fixture, final UnaryOperator<String> changeMessage) throws IOException {
        final Instant now = Instant.now();
        final String datex2Message = changeMessage.apply(readResourceContent("classpath:wazefeed/v37/" + fixture))
            .replace("OVERALL_START_TIME", now.minus(1, ChronoUnit.HOURS).toString())
            .replace("OVERALL_END_TIME", now.plus(1, ChronoUnit.HOURS).toString());

        return converter.export("GUID", datex2Message);
    }

    private String exportWithValidity(final int startOffsetDays, final int endOffsetDays) throws IOException {
        final Instant now = Instant.now();
        final String datex2Message = readResourceContent("classpath:wazefeed/v37/Accident_42573.xml")
            .replace("OVERALL_START_TIME", now.plus(startOffsetDays, ChronoUnit.DAYS).toString())
            .replace("OVERALL_END_TIME", now.plus(endOffsetDays, ChronoUnit.DAYS).toString());

        return converter.export("GUID", datex2Message);
    }
}
