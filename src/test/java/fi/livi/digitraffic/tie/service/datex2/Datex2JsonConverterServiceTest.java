package fi.livi.digitraffic.tie.service.datex2;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType.TRAFFIC_INCIDENT;
import static fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement.EarlyClosing.CANCELED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2DetailedMessageType;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.RoadWorkPhase;
import fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementProperties;

@Import({ Datex2JsonConverterService.class, JacksonAutoConfiguration.class })
public class Datex2JsonConverterServiceTest extends AbstractServiceTest {

    private static final String ILLEGAL_DURATION = "Pt6H";
    private static final String FEATURE_NAME_V2 = "Huono ajokeli";
    private final static String FEATURE_NAME_V3 = "Nopeusrajoitus";
    private final static double FEATURE_QUANTITY_V3 = 80.0;
    private final static String FEATURE_UNIT_V3 = "km/h";
    public static final String MAX_DURATION = "PT8H";
    public static final String MIN_DURATION = "PT6H";
    public static final String WORK_PHASE_ID = "WP1";

    @Autowired
    private Datex2JsonConverterService datex2JsonConverterService;

    @Autowired
    protected ObjectMapper objectMapper;

    public static final ZonedDateTime DATE_TIME = ZonedDateTime.parse("2020-01-02T14:43:18.388Z");

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4, TRAFFIC_INCIDENT);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V2, TRAFFIC_INCIDENT);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_4, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V2, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertLastActiveItinerarySegmentV3(feature, false);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature imsV0_2_6 =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(imsV0_2_6);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_6, TRAFFIC_INCIDENT);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V3, TRAFFIC_INCIDENT);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6, Datex2DetailedMessageType.ROADWORK);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V3, Datex2DetailedMessageType.ROADWORK);
        assertLastActiveItinerarySegmentV3(feature, true);
        assertV0_2_6Properties(feature);
    }

    @Test
    public void convertImsJsonV0_2_8ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_8.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_8.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_8 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_8, TRAFFIC_INCIDENT);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V3, TRAFFIC_INCIDENT);
    }

    @Test
    public void convertImsJsonV0_2_8ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_8.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_8.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_8 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_8, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V3, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertLastActiveItinerarySegmentV3(feature, true);
        assertV0_2_8Properties(feature);
    }

    private void assertV0_2_6Properties(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement ta = feature.getProperties().announcements.get(0);
        assertNull(ta.earlyClosing);
        assertEquals(WORK_PHASE_ID, ta.roadWorkPhases.get(0).id);
        assertNull(ta.roadWorkPhases.get(0).severity);
    }

    private void assertV0_2_8Properties(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature) {
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncement ta = feature.getProperties().announcements.get(0);
        assertEquals(CANCELED, ta.earlyClosing);
        assertEquals(WORK_PHASE_ID, ta.roadWorkPhases.get(0).id);
        assertEquals(RoadWorkPhase.Severity.HIGH, ta.roadWorkPhases.get(0).severity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_4_illegalDuration = StringUtils.replace(imsJsonV0_2_4, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4_illegalDuration, TRAFFIC_INCIDENT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_6_illegalDuration = StringUtils.replace(imsJsonV0_2_6, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6_illegalDuration, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertImsJsonV0_2_8ToGeoJsonFeatureObjectV3WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_8.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_8.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_6_illegalDuration = StringUtils.replace(imsJsonV0_2_6, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6_illegalDuration, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithoutDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        ims.getProperties().getAnnouncements().forEach(a -> a.setTimeAndDuration(null));
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJson, TRAFFIC_INCIDENT);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V2, TRAFFIC_INCIDENT);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV3WithoutDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        ims.getProperties().getAnnouncements().forEach(a -> a.setTimeAndDuration(null));
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V2, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertLastActiveItinerarySegmentV3(feature, false);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalProperties() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalProperties = StringUtils.replace(imsJson, "\"properties\"", "\"propertypos\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonIllegalProperties, TRAFFIC_INCIDENT);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalProperties() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalProperties = StringUtils.replace(imsJson, "\"properties\"", "\"propertypos\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonIllegalProperties, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_8ToGeoJsonFeatureObjectV3WithIllegalProperties() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_8.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_8.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalProperties = StringUtils.replace(imsJson, "\"properties\"", "\"propertypos\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonIllegalProperties, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalField() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalType = StringUtils.replace(imsJson, "\"type\"", "\"skype\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonIllegalType, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalGeometryType() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalGeometryType = StringUtils.replace(imsJson, "\"Point\"", "\"Joint\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonIllegalGeometryType, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_8ToGeoJsonFeatureObjectV3WithIllegalField() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_8.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_8.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalType = StringUtils.replace(imsJson, "\"type\"", "\"skype\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonIllegalType, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_8ToGeoJsonFeatureObjectV3WithIllegalGeometryType() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_8.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_8.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonIllegalGeometryType = StringUtils.replace(imsJson, "\"Point\"", "\"Joint\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonIllegalGeometryType, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithoutAnnouncements() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        ims.getProperties().setAnnouncements(Collections.emptyList());
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4, TRAFFIC_INCIDENT);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithoutAnnouncements() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        ims.getProperties().setAnnouncements(Collections.emptyList());
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test(expected = JsonParseException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalJson() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_4.createJsonMessage(DATE_TIME, FEATURE_NAME_V2);
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV3("{\n" + imsJsonV0_2_4, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test(expected = JsonParseException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalJson() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV3("{\n" + imsJsonV0_2_6, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV2WithoutLastActiveItinerarySegment() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        ims.getProperties().getAnnouncements().get(0).withLastActiveItinerarySegment(null);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_6, TRAFFIC_INCIDENT);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V3, TRAFFIC_INCIDENT);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithoutLastActiveItinerarySegment() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims =
            ImsJsonMessageFactoryV0_2_6.createJsonMessage(DATE_TIME, FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3);
        ims.getProperties().getAnnouncements().get(0).withLastActiveItinerarySegment(null);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V3, Datex2DetailedMessageType.TRAFFIC_ANNOUNCEMENT);
        assertLastActiveItinerarySegmentV3(feature, false);
    }

    private static final String SITUATION_ID1 = "GUID00000001";
    private static final String SITUATION_ID2 = "GUID00000002";
    private static final String FEATURE =
        "{\n" +
            "  \"type\" : \"Feature\",\n" +
            "  \"geometry\" : {\n" +
            "    \"type\" : \"Point\",\n" +
            "    \"coordinates\" : [ 23.77474, 61.50221 ]\n" +
            "  },\n" +
            "  \"properties\" : {\n" +
            "    \"situationId\" : \"GUID00000001\",\n" +
            "    \"version\" : 1,\n" +
            "    \"releaseTime\" : \"2019-12-13T14:43:18.388+02:00\",\n" +
            "    \"locationToDisplay\" : {\n" +
            "      \"e\" : 331529.0,\n" +
            "      \"n\" : 6805963.0\n" +
            "    },\n" +
            "    \"announcements\" : [ ],\n" +
            "    \"contact\" : {\n" +
            "      \"phone\" : \"12341234\",\n" +
            "      \"fax\" : \"43214321\",\n" +
            "      \"email\" : \"paivystys@liikenne.fi\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String FEATURE_COLLECTION =
        "{\n" +
            "  \"type\": \"FeatureCollection\",\n" +
            "  \"features\": [\n" +
            "    FEATURES \n" +
            "  ]\n" +
            "}";

    @Test
    public void parseFeatureJsonsFromImsJson_Feature() throws JsonProcessingException {
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE);
        assertEquals(1, jsons.size());
        final ObjectReader reader = objectMapper.reader();
        final JsonNode original = reader.readTree(FEATURE);
        final JsonNode parsed = reader.readTree(jsons.get(SITUATION_ID1));
        assertEquals(original, parsed);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollection() throws JsonProcessingException {
        // Create feature collection with two features (just situationId differs)
        final String feature2 = changeSituationIdInFeature(FEATURE, SITUATION_ID1, SITUATION_ID2);
        final String featureCollection = createFeatureCollectionWithSituations(FEATURE, feature2);

        // parse features from collection and test src == result
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(2, jsons.size());

        final ObjectReader reader = objectMapper.reader();
        final JsonNode original1 = reader.readTree(FEATURE);
        final JsonNode original2 = reader.readTree(feature2);
        assertNotEquals("Originals should differ with situationId", original1, original2);

        final JsonNode parsed1 = reader.readTree(jsons.get(SITUATION_ID1));
        final JsonNode parsed2 = reader.readTree(jsons.get(SITUATION_ID2));
        assertEquals(original1, parsed1);
        assertEquals(original2, parsed2);
        assertNotEquals(parsed1, parsed2);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureEmptySituationId() {
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE.replace(SITUATION_ID1, ""));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureNoSituationId() {
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE.replace("situationId", "situationI"));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_InvalidJson() {
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE_COLLECTION);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionEmptySituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace(SITUATION_ID1, ""));
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE_COLLECTION);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionNoSituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace("situationId", "situationsId"));
        final Map<String, String> jsons = datex2JsonConverterService.parseFeatureJsonsFromImsJson(FEATURE_COLLECTION);
        assertEquals(0, jsons.size());
    }

    private void assertAnnouncementFeaturesV2(final TrafficAnnouncementFeature feature, final String featureName,
                                              Datex2MessageType type) {
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementProperties p = feature.getProperties();
        AssertHelper.assertCollectionSize(1, p.announcements);
        assertEquals(type, p.getMessageType());
        assertEquals(1, p.announcements.get(0).features.size());
        assertEquals(featureName, p.announcements.get(0).features.get(0));
    }

    private String changeSituationIdInFeature(final String featureToEdit, final String situationIdToReplace, final String replacementSituationId) {
        return StringUtils.replace(featureToEdit, situationIdToReplace, replacementSituationId);
    }

    private String createFeatureCollectionWithSituations(final String... feature) {
        final String features = StringUtils.joinWith(", ", feature);
        return StringUtils.replace(FEATURE_COLLECTION, "FEATURES", features);
    }

    private void assertAnnouncementFeaturesV3(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature,
                                              final String featureName,
                                              Datex2DetailedMessageType type) {
        final TrafficAnnouncementProperties p = feature.getProperties();
        AssertHelper.assertCollectionSize(1, feature.getProperties().announcements);
        assertEquals(type.getDatex2MessageType(), p.getMessageType());
        assertEquals(type, p.detailedMessageType);
        assertEquals(1, p.announcements.get(0).features.size());
        assertEquals(featureName, p.announcements.get(0).features.get(0).name);
    }

    private void assertLastActiveItinerarySegmentV3(
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature,
        final boolean shouldExist) {
        if (shouldExist) {
            AssertHelper.assertCollectionSize(1, feature.getProperties().announcements);
            final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.LastActiveItinerarySegment lais =
                feature.getProperties().announcements.get(0).lastActiveItinerarySegment;
            assertNotNull(lais);
            assertNotNull(lais.startTime);
            assertNotNull(lais.endTime);
            assertNotNull(lais.legs);
            AssertHelper.assertCollectionSize(1, lais.legs);
            final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.ItineraryLeg leg = lais.legs.get(0);
            assertNotNull(leg.streetName);
            assertNotNull(leg.roadLeg);
            assertNotNull(leg.roadLeg.roadName);
            assertNotNull(leg.roadLeg.endArea);
            assertNotNull(leg.roadLeg.startArea);
            assertNotNull(leg.roadLeg.roadNumber);
        } else {
            if (feature.getProperties().announcements != null && feature.getProperties().announcements.size() > 0) {
                assertNull(feature.getProperties().announcements.get(0).lastActiveItinerarySegment);
            }
        }
    }

}
