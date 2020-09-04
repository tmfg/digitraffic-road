package fi.livi.digitraffic.tie.service.datex2;

import static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TrafficAnnouncement.Language.FI;
import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;

@Import({ Datex2JsonConverterService.class, JacksonAutoConfiguration.class })
public class Datex2JsonConverterServiceTest extends AbstractServiceTest {

    private static final String MAX_DURATION = "PT8H";
    private static final String MIN_DURATION = "PT6H";
    private static final String ILLEGAL_DURATION = "Pt6H";
    private static final String FEATURE_v2 = "Huono ajokeli";
    private final static String FEATURE_NAME_V3 = "Nopeusrajoitus";
    private final static double FEATURE_QUANTITY_V3 = 80.0;
    private final static String FEATURE_UNIT_V3 = "km/h";

    @Autowired
    private Datex2JsonConverterService datex2JsonConverterService;

    @Autowired
    protected ObjectMapper objectMapper;

    public static final ZonedDateTime DATE_TIME = ZonedDateTime.parse("2020-01-02T14:43:18.388Z");


    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4);
        assertAnnouncementFeaturesV2(feature, FEATURE_v2);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_4);
        assertAnnouncementFeaturesV3(feature, FEATURE_v2);
    }

    @Test
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature imsV0_2_5 = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(imsV0_2_5);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_5);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V3);
    }

    @Test
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_5);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V3);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_5_illegalDuration = StringUtils.replace(imsJsonV0_2_4, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_5_illegalDuration);
    }


    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV3WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_5_illegalDuration = StringUtils.replace(imsJsonV0_2_5, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_5_illegalDuration);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV3WithIllegalProperties() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_5_illegalProperties = StringUtils.replace(imsJsonV0_2_5, "\"properties\"", "\"propertypos\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_5_illegalProperties);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV3WithIllegalField() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_5_illegalType = StringUtils.replace(imsJsonV0_2_5, "\"type\"", "\"skype\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_5_illegalType);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_5ToGeoJsonFeatureObjectV3WithIllegalGeometryType() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.ImsGeoJsonFeature ims = createJsonMessageV0_2_5();
        final String imsJsonV0_2_5 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_5_illegalGeometryType = StringUtils.replace(imsJsonV0_2_5, "\"Point\"", "\"Joint\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_5_illegalGeometryType);
    }

    private void assertAnnouncementFeaturesV2(final TrafficAnnouncementFeature feature, final String featureName) {
        assertEquals(1, feature.getProperties().announcements.size());
        assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        assertEquals(featureName, feature.getProperties().announcements.get(0).features.get(0));
    }

    private void assertAnnouncementFeaturesV3(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature,
                                              final String featureName) {
        Assert.assertEquals(1, feature.getProperties().announcements.size());
        Assert.assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        Assert.assertEquals(featureName, feature.getProperties().announcements.get(0).features.get(0).name);
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
                    .withLocation(createLocationV0_2_4())
                    .withLocationDetails(createLocationDetailsV0_2_4())
                    .withFeatures(Collections.singletonList(FEATURE_v2))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDurationV0_2_4())
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

    private static TimeAndDuration createTimeAndDurationV0_2_4() {
        return new TimeAndDuration(DATE_TIME, DATE_TIME.plusHours(2), new EstimatedDuration().withInformal("Yli 6 tuntia").withMaximum(MAX_DURATION).withMinimum(MIN_DURATION));
    }

    private static LocationDetails createLocationDetailsV0_2_4() {
        return new LocationDetails()
            .withRoadAddressLocation(
                new RoadAddressLocation(
                    createRoadPointV0_2_4(1), createRoadPointV0_2_4(2), RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
                );
    }

    private static RoadPoint createRoadPointV0_2_4(final int id) {
        return new RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new RoadAddress(130, 24, 4000),
            "Tie 123", new AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static Location createLocationV0_2_4() {
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
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.TimeAndDuration(
            DATE_TIME, DATE_TIME.plusHours(2),
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_5.EstimatedDuration().withInformal("Yli 6 tuntia")
                .withMaximum(MAX_DURATION).withMinimum(MIN_DURATION));
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
