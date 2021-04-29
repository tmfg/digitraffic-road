package fi.livi.digitraffic.tie.metadata.geojson.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutLocalStack;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPoint;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPolygon;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.Polygon;

/**
 * Samples from https://en.wikipedia.org/wiki/GeoJSON
 */
public class GeometryJacksonDeserializerTest extends AbstractDaemonTestWithoutLocalStack {
    private static final Logger log = LoggerFactory.getLogger(GeometryJacksonDeserializerTest.class);

    @Autowired
    private ObjectMapper objectMapper;

    private ObjectReader geoJsonGeometryReader;

    @BeforeEach
    public void initReader() {
        geoJsonGeometryReader = objectMapper.readerFor(Geometry.class);
    }

    private static final String POINT =
            "{\n" +
            "    \"type\": \"Point\", \n" +
            "    \"coordinates\": [30, 10]\n" +
            "}";

    private static final String LINE_STRING =
            "{\n" +
            "    \"type\": \"LineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [30, 10], [10, 30], [40, 40]\n" +
            "    ]\n" +
            "}";

    private static final String MULTI_LINE_STRING =
            "{\n" +
            "    \"type\": \"MultiLineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [[10, 10], [20, 20], [10, 40]], \n" +
            "        [[40, 40], [30, 30], [40, 20], [30, 10]]\n" +
            "    ]\n" +
            "}";

    private static final String POLYGON =
            "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n" +
            "    ]\n" +
            "}";

    private static String POLYGON_WITH_INTERNAL_POLYGON =
            "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], \n" +
            "        [[20, 30], [35, 35], [30, 20], [20, 30]]\n" +
            "    ]\n" +
            "}";

    private static final String MULTI_POINT =
            "{\n" +
            "    \"type\": \"MultiPoint\", \n" +
            "    \"coordinates\": [\n" +
            "        [10, 40], [40, 30], [20, 20], [30, 10]\n" +
            "    ]\n" +
            "}";

    private static final String MULTI_POLYGON =
            "{\n" +
            "    \"type\": \"MultiPolygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [\n" +
            "            [[30, 20], [45, 40], [10, 40], [30, 20]]\n" +
            "        ], \n" +
            "        [\n" +
            "            [[15, 5], [40, 10], [10, 20], [5, 10], [15, 5]]\n" +
            "        ]\n" +
            "    ]\n" +
            "}";

    private static final String MULTI_POLYGON_WITH_INNER_POLYGON =
            "{\n" +
            "    \"type\": \"MultiPolygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [\n" +
            "            [[40, 40], [20, 45], [45, 30], [40, 40]]\n" +
            "        ], \n" +
            "        [\n" +
            "            [[20, 35], [10, 30], [10, 10], [30, 5], [45, 20], [20, 35]], \n" +
            "            [[30, 20], [20, 15], [20, 25], [30, 20]]\n" +
            "        ]\n" +
            "    ]\n" +
            "}";

    @Test
    public void point() throws JsonProcessingException {
        final Point geom = geoJsonGeometryReader.readValue(POINT);
        log.info(geom.toString());
    }

    @Test
    public void lineString() throws JsonProcessingException {
        final LineString geom = geoJsonGeometryReader.readValue(LINE_STRING);
        log.info(geom.toString());
    }

    @Test
    public void multiLineString() throws JsonProcessingException {
        final MultiLineString geom = geoJsonGeometryReader.readValue(MULTI_LINE_STRING);
        log.info(geom.toString());
    }

    @Test
    public void polygon() throws JsonProcessingException {
        final Polygon geom = geoJsonGeometryReader.readValue(POLYGON);
        log.info(geom.toString());
    }

    @Test
    public void polygonWithInternalPolygon() throws JsonProcessingException {
        final Polygon geom = geoJsonGeometryReader.readValue(POLYGON_WITH_INTERNAL_POLYGON);
        log.info(geom.toString());
    }

    @Test
    public void multiPoint() throws JsonProcessingException {
        final MultiPoint geom = geoJsonGeometryReader.readValue(MULTI_POINT);
        log.info(geom.toString());
    }

    @Test
    public void multiPolygon() throws JsonProcessingException {
        final MultiPolygon geom = geoJsonGeometryReader.readValue(MULTI_POLYGON);
        log.info(geom.toString());
    }

    @Test
    public void multiPolygonWithInnerPolygon() throws JsonProcessingException {
        final MultiPolygon geom = geoJsonGeometryReader.readValue(MULTI_POLYGON_WITH_INNER_POLYGON);
        log.info(geom.toString());
    }
}
