package fi.livi.digitraffic.tie.service.v3.datex2.datex2;

import static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TrafficAnnouncement.Language.FI;
import static fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2HelperServiceTest.DATE_TIME;
import static fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2HelperServiceTest.FEATURE;
import static fi.livi.digitraffic.tie.service.v2.datex2.V2Datex2HelperServiceTest.createJsonMessageV0_2_4;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.datex2.Accident;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.datex2.SituationRecord;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.AlertCLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Contact;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.EstimatedDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Feature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.JsonMessage;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Location;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.LocationDetails;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.LocationToDisplay;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadAddress;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadAddressLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadPoint;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TimeAndDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TrafficAnnouncement;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.service.v3.datex2.V3Datex2HelperService;

@Import({ V3Datex2HelperService.class, JacksonAutoConfiguration.class })
public class V3Datex2HelperServiceTest extends AbstractServiceTest {

    public final static String FEATURE_NAME = "Nopeusrajoitus";
    private final static double FEATURE_QUANTITY = 80.0;
    private final static String FEATURE_UNIT = "km/h";

    @Autowired
    private V3Datex2HelperService v3Datex2HelperService;

    @Autowired
    protected ObjectMapper objectMapper;

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        TrafficAnnouncementFeature feature =
            v3Datex2HelperService.convertToFeatureJsonObjectV3(imsJsonV0_2_4);
        Assert.assertEquals(1, feature.getProperties().announcements.size());
        Assert.assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        Assert.assertEquals(FEATURE, feature.getProperties().announcements.get(0).features.get(0).name);
    }

    @Test
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        TrafficAnnouncementFeature feature =
            v3Datex2HelperService.convertToFeatureJsonObjectV3(imsJsonV0_2_5);
        Assert.assertEquals(1, feature.getProperties().announcements.size());
        Assert.assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        Assert.assertEquals(FEATURE_NAME, feature.getProperties().announcements.get(0).features.get(0).name);
    }

    private static Situation creatSituationWithRecordsVersionTimes(Instant...versionTimes) {
        final List<SituationRecord> records = Arrays.stream(versionTimes).map(V3Datex2HelperServiceTest::createSituationRecord).collect(Collectors.toList());
        return new Situation().withSituationRecords(records);
    }

    private static SituationRecord createSituationRecord(final Instant versionTime) {
        return new Accident().withSituationRecordVersionTime(versionTime);
    }

    private static D2LogicalModel createD2LogicalModelWithSituationPublications(Situation...situations) {
        final SituationPublication sp = new SituationPublication();
        sp.withSituations(situations);
        return new D2LogicalModel().withPayloadPublication(sp);
    }

    public static ImsGeoJsonFeature createJsonMessageV0_2_5() {
        final JsonMessage properties = new JsonMessage()
            .withVersion(1)
            .withSituationId("GUID123456")
            .withReleaseTime(DATE_TIME)
            .withAnnouncements(Collections.singletonList(
                new TrafficAnnouncement()
                    .withLanguage(FI)
                    .withTitle("Title")
                    .withLocation(createLocation())
                    .withLocationDetails(createLocationDetails())
                    .withFeatures(Collections.singletonList(new Feature(FEATURE_NAME, FEATURE_QUANTITY, FEATURE_UNIT)))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDuration())
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
            ))
            .withLocationToDisplay(new LocationToDisplay(1.0, 2.0))
            .withContact(new Contact("123456789", "987654321", "helsinki@liikennekeskus.fi"));
        return new ImsGeoJsonFeature()
            .withType(ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(new Point(23.774741, 61.502211));
    }

    private static TimeAndDuration createTimeAndDuration() {
        return new TimeAndDuration(DATE_TIME, DATE_TIME.plusHours(2), new EstimatedDuration().withInformal("Yli 6 tuntia").withMaximum("PT8H").withMinimum("PT6H"));
    }

    private static LocationDetails createLocationDetails() {
        return new LocationDetails()
            .withRoadAddressLocation(
                new RoadAddressLocation(
                    createRoadPoint(1), createRoadPoint(2), RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
                );
    }

    private static RoadPoint createRoadPoint(final int id) {
        return new RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new RoadAddress(130, 24, 4000),
            "Tie 123", new AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static Location createLocation() {
        return new Location(358, 10, "1.1.1", "Location description");
    }
}
