package fi.livi.digitraffic.tie.service.datex2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.model.v1.datex2.TrafficAnnouncementType;

public class ImsJsonConverterServiceTest extends AbstractServiceTest {
    private static final Logger log = getLogger(ImsJsonConverterServiceTest.class);

    @Autowired
    private ImsJsonConverterService imsJsonConverterService;

    @Autowired
    protected ObjectMapper objectMapper;

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
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons = imsJsonConverterService.parseFeatureJsonsFromImsJson(FEATURE);
        assertEquals(1, jsons.size());
        final ObjectReader reader = objectMapper.reader();
        final JsonNode original = reader.readTree(FEATURE);
        final JsonNode parsed = reader.readTree(jsons.get(SITUATION_ID1).getLeft());
        assertEquals(original, parsed);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollection() throws JsonProcessingException {
        // Create feature collection with two features (just situationId differs)
        final String feature2 = changeSituationIdInFeature(FEATURE, SITUATION_ID1, SITUATION_ID2);
        final String featureCollection = createFeatureCollectionWithSituations(FEATURE, feature2);

        // parse features from collection and test src == result
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons = imsJsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(2, jsons.size());

        final ObjectReader reader = objectMapper.reader();
        final JsonNode original1 = reader.readTree(FEATURE);
        final JsonNode original2 = reader.readTree(feature2);
        assertNotEquals("Originals should differ with situationId", original1, original2);

        final JsonNode parsed1 = reader.readTree(jsons.get(SITUATION_ID1).getLeft());
        final JsonNode parsed2 = reader.readTree(jsons.get(SITUATION_ID2).getLeft());
        assertEquals(original1, parsed1);
        assertEquals(original2, parsed2);
        assertNotEquals(parsed1, parsed2);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureEmptySituationId() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverterService.parseFeatureJsonsFromImsJson(FEATURE.replace(SITUATION_ID1, ""));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureNoSituationId() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverterService.parseFeatureJsonsFromImsJson(FEATURE.replace("situationId", "situationI"));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_InvalidJson() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverterService.parseFeatureJsonsFromImsJson(FEATURE_COLLECTION);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionEmptySituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace(SITUATION_ID1, ""));
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionNoSituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace("situationId", "situationsId"));
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverterService.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(0, jsons.size());
    }

    private String changeSituationIdInFeature(final String featureToEdit, final String situationIdToReplace, final String replacementSituationId) {
        return StringUtils.replace(featureToEdit, situationIdToReplace, replacementSituationId);
    }

    private String createFeatureCollectionWithSituations(final String... feature) {
        final String features = StringUtils.joinWith(", ", feature);
        return StringUtils.replace(FEATURE_COLLECTION, "FEATURES", features);
    }
}