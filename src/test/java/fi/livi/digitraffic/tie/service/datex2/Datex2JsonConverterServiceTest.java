package fi.livi.digitraffic.tie.service.datex2;

import static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.TrafficAnnouncement.Language.FI;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
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
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.Feature;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ItineraryLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ItineraryRoadLeg;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.LastActiveItinerarySegment;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature;

@Import({ Datex2JsonConverterService.class, JacksonAutoConfiguration.class })
public class Datex2JsonConverterServiceTest extends AbstractServiceTest {

    private static final String MAX_DURATION = "PT8H";
    private static final String MIN_DURATION = "PT6H";
    private static final String ILLEGAL_DURATION = "Pt6H";
    private static final String FEATURE_V2 = "Huono ajokeli";
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
        assertAnnouncementFeaturesV2(feature, FEATURE_V2);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature imsV0_2_4 = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(imsV0_2_4);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_4);
        assertAnnouncementFeaturesV3(feature, FEATURE_V2);
        assertLastActiveItinerarySegmentV3(feature, false);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV2() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature imsV0_2_6 = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(imsV0_2_6);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_6);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V3);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V3);
        assertLastActiveItinerarySegmentV3(feature, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_4_illegalDuration = StringUtils.replace(imsJsonV0_2_4, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4_illegalDuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_6_illegalDuration = StringUtils.replace(imsJsonV0_2_6, MIN_DURATION, ILLEGAL_DURATION);
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6_illegalDuration);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithoutDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        ims.getProperties().getAnnouncements().forEach(a -> a.setTimeAndDuration(null));
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJson);
        assertAnnouncementFeaturesV2(feature, FEATURE_V2);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV3WithoutDuration() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        ims.getProperties().getAnnouncements().forEach(a -> a.setTimeAndDuration(null));
        final String imsJson = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJson);
        assertAnnouncementFeaturesV3(feature, FEATURE_V2);
        assertLastActiveItinerarySegmentV3(feature, false);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalProperties() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_4_illegalProperties = StringUtils.replace(imsJsonV0_2_4, "\"properties\"", "\"propertypos\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4_illegalProperties);
    }

    @Test(expected = IllegalStateException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalProperties() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_6_illegalProperties = StringUtils.replace(imsJsonV0_2_6, "\"properties\"", "\"propertypos\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6_illegalProperties);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalField() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_6_illegalType = StringUtils.replace(imsJsonV0_2_6, "\"type\"", "\"skype\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6_illegalType);
    }

    @Test(expected = JsonMappingException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalGeometryType() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final String imsJsonV0_2_6_illegalGeometryType = StringUtils.replace(imsJsonV0_2_6, "\"Point\"", "\"Joint\"");
        datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6_illegalGeometryType);
    }

    @Test
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithoutAnnouncements() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        ims.getProperties().setAnnouncements(Collections.emptyList());
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v2.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_4);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithoutAnnouncements() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        ims.getProperties().setAnnouncements(Collections.emptyList());
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test(expected = JsonParseException.class)
    public void convertImsJsonV0_2_4ToGeoJsonFeatureObjectV2WithIllegalJson() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_4.ImsGeoJsonFeature ims = createJsonMessageV0_2_4();
        final String imsJsonV0_2_4 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV3("{\n" + imsJsonV0_2_4);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test(expected = JsonParseException.class)
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithIllegalJson() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature f =
            datex2JsonConverterService.convertToFeatureJsonObjectV3("{\n" + imsJsonV0_2_6);
        assertCollectionSize(0, f.getProperties().announcements);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV2WithoutLastActiveItinerarySegment() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        ims.getProperties().getAnnouncements().get(0).withLastActiveItinerarySegment(null);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV2(imsJsonV0_2_6);
        assertAnnouncementFeaturesV2(feature, FEATURE_NAME_V3);
    }

    @Test
    public void convertImsJsonV0_2_6ToGeoJsonFeatureObjectV3WithoutLastActiveItinerarySegment() throws JsonProcessingException {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature ims = createJsonMessageV0_2_6();
        ims.getProperties().getAnnouncements().get(0).withLastActiveItinerarySegment(null);
        final String imsJsonV0_2_6 = objectMapper.writer().writeValueAsString(ims);
        final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature =
            datex2JsonConverterService.convertToFeatureJsonObjectV3(imsJsonV0_2_6);
        assertAnnouncementFeaturesV3(feature, FEATURE_NAME_V3);
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
        // Create feature collection with two features
        final String feature2 = FEATURE.replace(SITUATION_ID1, SITUATION_ID2);
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE + ", " + feature2);

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

    private void assertAnnouncementFeaturesV2(final TrafficAnnouncementFeature feature, final String featureName) {
        AssertHelper.assertCollectionSize(1, feature.getProperties().announcements);
        assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        assertEquals(featureName, feature.getProperties().announcements.get(0).features.get(0));
    }

    private void assertAnnouncementFeaturesV3(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature,
                                              final String featureName) {
        AssertHelper.assertCollectionSize(1, feature.getProperties().announcements);
        Assert.assertEquals(1, feature.getProperties().announcements.get(0).features.size());
        Assert.assertEquals(featureName, feature.getProperties().announcements.get(0).features.get(0).name);
    }

    private void assertLastActiveItinerarySegmentV3(final fi.livi.digitraffic.tie.model.v3.geojson.trafficannouncement.TrafficAnnouncementFeature feature,
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
                    .withFeatures(Collections.singletonList(FEATURE_V2))
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

    public static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature createJsonMessageV0_2_6() {
        final fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.JsonMessage
            properties = new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.JsonMessage()
            .withVersion(1)
            .withSituationId("GUID123456")
            .withReleaseTime(DATE_TIME)
            .withAnnouncements(Collections.singletonList(
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.TrafficAnnouncement()
                    .withLanguage(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.TrafficAnnouncement.Language.FI)
                    .withTitle("Title")
                    .withLocation(createLocationV0_2_6())
                    .withLocationDetails(createLocationDetailsV0_2_6())
                    // V0.2.5
                    .withFeatures(Collections.singletonList(new Feature(FEATURE_NAME_V3, FEATURE_QUANTITY_V3, FEATURE_UNIT_V3)))
                    .withComment("TEST")
                    .withTimeAndDuration(createTimeAndDurationV0_2_6())
                    .withAdditionalInformation("Liikenne- ja kelitiedot verkossa: http://liikennetilanne.tmfg.fi/")
                    .withSender("Tieliikennekeskus Helsinki")
                    // V0.2.6
                    .withLastActiveItinerarySegment(
                        new LastActiveItinerarySegment()
                            .withEndTime(DATE_TIME)
                            .withStartTime(DATE_TIME.minusHours(1))
                            .withLegs(Collections.singletonList(
                                new ItineraryLeg()
                                    .withRoadLeg(
                                        new ItineraryRoadLeg()
                                            .withRoadName("Groom Lake Road")
                                            .withRoadNumber(123)
                                            .withEndArea("Area 51")
                                            .withStartArea("Area 52"))
                                    .withStreetName("Groom Lake Road"))))
            ))
            .withLocationToDisplay(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.LocationToDisplay(1.0, 2.0))
            .withContact(new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.Contact("123456789", "987654321", "helsinki@liikennekeskus.fi"));
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature()
            .withType(fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.ImsGeoJsonFeature.Type.FEATURE)
            .withProperties(properties)
            .withGeometry(new Point(23.774741, 61.502211));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.TimeAndDuration createTimeAndDurationV0_2_6() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.TimeAndDuration(
            DATE_TIME, DATE_TIME.plusHours(2),
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.EstimatedDuration().withInformal("Yli 6 tuntia")
                .withMaximum(MAX_DURATION).withMinimum(MIN_DURATION));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.LocationDetails createLocationDetailsV0_2_6() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.LocationDetails()
            .withRoadAddressLocation(
                new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.RoadAddressLocation(
                    createRoadPointV0_2_6(1), createRoadPointV0_2_6(2), fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.RoadAddressLocation.Direction.POS, "Marjamäen suuntaan")
            );
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.RoadPoint createRoadPointV0_2_6(final int id) {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.RoadPoint(
            "Lempäälä" + id, "Pirkanmaa", "Suomi",
            new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.RoadAddress(130, 24, 4000),
            "Tie 123", new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.AlertCLocation(37128, "Marjamäki", 2000));
    }

    private static fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.Location createLocationV0_2_6() {
        return new fi.livi.digitraffic.tie.external.tloik.ims.jmessage.v0_2_6.Location(358, 10, "1.1.1", "Location description");
    }

}
