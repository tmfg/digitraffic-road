package fi.livi.digitraffic.tie.service.v2.datex2;

import static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TrafficAnnouncement.Language.FI;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.AlertCLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.Contact;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.EstimatedDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.JsonMessage;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.Location;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.LocationDetails;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.LocationToDisplay;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.RoadAddress;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.RoadAddressLocation;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.RoadPoint;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TimeAndDuration;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TrafficAnnouncement;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.service.datex2.Datex2JsonConverterService;

@Import({ Datex2JsonConverterService.class, JacksonAutoConfiguration.class })
public class Datex2JsonConverterServiceTest extends AbstractServiceTest {

    @Autowired
    private Datex2JsonConverterService datex2JsonConverterService;

    @Autowired
    protected ObjectMapper objectMapper;

    public static final ZonedDateTime DATE_TIME = ZonedDateTime.parse("2020-01-02T14:43:18.388Z");
    public static final Instant TIME_NOW = Instant.now().with(ChronoField.MILLI_OF_SECOND, 123);
    public static final ZonedDateTime TIME_NOW_ZONED = TIME_NOW.atZone(ZoneOffset.UTC);
    public static final Instant TIME_MILLIS_IN_FUTURE = TIME_NOW.with(ChronoField.MILLI_OF_SECOND, 321);
    public static final Instant TIME_SECONDS_IN_FUTURE = Instant.ofEpochMilli(TIME_NOW.toEpochMilli()+1000); // +1s
    public static final Instant TIME_SECONDS_IN_PAST = Instant.ofEpochMilli(TIME_NOW.toEpochMilli()-1000); // +1s

    private static final String FEATURE_v2 = "Huono ajokeli";
    private final static String FEATURE_NAME_V3 = "Nopeusrajoitus";
    private final static double FEATURE_QUANTITY_V3 = 80.0;
    private final static String FEATURE_UNIT_V3 = "km/h";

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4);
        assertEquals(1, feature.getProperties().announcements.size());
        assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        assertEquals(FEATURE_v2, feature.getProperties().announcements.get(0).features.get(0));
    }
    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_4);
        Assert.assertEquals(1, feature.getProperties().announcements.size());
        Assert.assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        Assert.assertEquals(FEATURE_v2, feature.getProperties().announcements.get(0).features.get(0).name);
    }

    @Test
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature imsV0_2_5 = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(imsV0_2_5);
        fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_5);
        assertEquals(1, feature.getProperties().announcements.size());
        assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        assertEquals(FEATURE_NAME_V3, feature.getProperties().announcements.get(0).features.get(0));
    }
    @Test
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_5);
        Assert.assertEquals(1, feature.getProperties().announcements.size());
        Assert.assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        Assert.assertEquals(FEATURE_NAME_V3, feature.getProperties().announcements.get(0).features.get(0).name);
    }

    public static ImsGeoJsonFeature createJsonMessageV0_2_4() {
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
                    .withFeatures(Collections.singletonList(FEATURE_v2))
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

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature createJsonMessageV0_2_5() {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.JsonMessage
            properties = new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.JsonMessage()
            .withVersion(1)
            .withSituationId("GUID123456")
            .withReleaseTime(DATE_TIME)
            .withAnnouncements(Collections.singletonList(
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TrafficAnnouncement()
                    .withLanguage(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TrafficAnnouncement.Language.FI)
                    .withTitle("Title")
                    .withLocation(createLocationV0_2_5())
                    .withLocationDetails(createLocationDetailsV0_2_5())
                    .withFeatures(Collections.singletonList(new Feature(FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3)))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDurationV0_2_5())
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
            ))
            .withLocationToDisplay(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.LocationToDisplay(1.0, 2.0))
            .withContact(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Contact("123456789", "987654321", "helsinki@liikennekeskus.fi"));
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature()
            .withType(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(new Point(23.774741, 61.502211));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TimeAndDuration createTimeAndDurationV0_2_5() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TimeAndDuration(DATE_TIME, DATE_TIME.plusHours(2), new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.EstimatedDuration().withInformal("Yli 6 tuntia").withMaximum("PT8H").withMinimum("PT6H"));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.LocationDetails createLocationDetailsV0_2_5() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.LocationDetails()
            .withRoadAddressLocation(
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadAddressLocation(
                    createRoadPointV0_2_5(1), createRoadPointV0_2_5(2), fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
            );
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadPoint createRoadPointV0_2_5(final int id) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.RoadAddress(130, 24, 4000),
            "Tie 123", new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Location createLocationV0_2_5() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.Location(358, 10, "1.1.1", "Location description");
    }

}
