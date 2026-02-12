package fi.livi.digitraffic.tie.service.trafficmessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.SituationType;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.TrafficAnnouncementType;

public class ImsJsonConverterTest extends AbstractServiceTest {

    @Autowired
    private ImsJsonConverter imsJsonConverter;

    @Autowired
    protected ObjectMapper objectMapper;

    private static final String SITUATION_ID1 = "GUID00000001";
    private static final String SITUATION_ID2 = "GUID00000002";
    private static final String FEATURE = """
                    {
                      "type" : "Feature",
                      "geometry" : {
                        "type" : "Point",
                        "coordinates" : [ 23.77474, 61.50221 ]
                      },
                      "properties" : {
                        "situationId" : "GUID00000001",
                        "version" : 1,
                        "releaseTime" : "2019-12-13T14:43:18.388+02:00",
                        "locationToDisplay" : {
                          "e" : 331529.0,
                          "n" : 6805963.0
                        },
                        "announcements" : [ ],
                        "contact" : {
                          "phone" : "12341234",
                          "fax" : "43214321",
                          "email" : "paivystys@liikenne.fi"
                        }
                      }
                    }""";

    private static final String FEATURE_COLLECTION = """
                    {
                      "type": "FeatureCollection",
                      "features": [
                        FEATURES\s
                      ]
                    }""";

    @Test
    public void parseFeatureJsonsFromImsJson_Feature() throws JsonProcessingException {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons = imsJsonConverter.parseFeatureJsonsFromImsJson(FEATURE);
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
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons = imsJsonConverter.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(2, jsons.size());

        final ObjectReader reader = objectMapper.reader();
        final JsonNode original1 = reader.readTree(FEATURE);
        final JsonNode original2 = reader.readTree(feature2);
        assertNotEquals(original1, original2, "Originals should differ with situationId");

        final JsonNode parsed1 = reader.readTree(jsons.get(SITUATION_ID1).getLeft());
        final JsonNode parsed2 = reader.readTree(jsons.get(SITUATION_ID2).getLeft());
        assertEquals(original1, parsed1);
        assertEquals(original2, parsed2);
        assertNotEquals(parsed1, parsed2);
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureEmptySituationId() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverter.parseFeatureJsonsFromImsJson(FEATURE.replace(SITUATION_ID1, ""));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureNoSituationId() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverter.parseFeatureJsonsFromImsJson(FEATURE.replace("situationId", "situationI"));
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_InvalidJson() {
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverter.parseFeatureJsonsFromImsJson(FEATURE_COLLECTION);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionEmptySituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace(SITUATION_ID1, ""));
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverter.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(0, jsons.size());
    }

    @Test
    public void parseFeatureJsonsFromImsJson_FeatureCollectionNoSituationId() {
        final String featureCollection = FEATURE_COLLECTION.replace("FEATURES", FEATURE.replace("situationId", "situationsId"));
        final Map<String, Triple<String, SituationType, TrafficAnnouncementType>> jsons =
            imsJsonConverter.parseFeatureJsonsFromImsJson(featureCollection);
        assertEquals(0, jsons.size());
    }

    private String changeSituationIdInFeature(final String featureToEdit, final String situationIdToReplace, final String replacementSituationId) {
        return Strings.CS.replace(featureToEdit, situationIdToReplace, replacementSituationId);
    }

    private String createFeatureCollectionWithSituations(final String... feature) {
        final String features = StringUtils.joinWith(", ", (Object[]) feature);
        return Strings.CS.replace(FEATURE_COLLECTION, "FEATURES", features);
    }
}
