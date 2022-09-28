package fi.livi.digitraffic.tie.metadata.geojson.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiLineString;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPoint;
import fi.livi.digitraffic.tie.metadata.geojson.MultiPolygon;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.Polygon;

/**
 * Samples from <a href="https://en.wikipedia.org/wiki/GeoJSON">GeoJSON</a>
 */
public class GeometryJacksonDeserializerTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(GeometryJacksonDeserializerTest.class);

    private final ObjectReader geoJsonGeometryReader = new ObjectMapper().readerFor(Geometry.class);

    public static final String POINT =
            "{\n" +
            "    \"type\": \"Point\", \n" +
            "    \"coordinates\": [30, 10]\n" +
            "}";

    public static final String LINE_STRING =
            "{\n" +
            "    \"type\": \"LineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [30, 10], [10, 30], [40, 40]\n" +
            "    ]\n" +
            "}";

    public static final String MULTI_LINE_STRING =
            "{\n" +
            "    \"type\": \"MultiLineString\", \n" +
            "    \"coordinates\": [\n" +
            "        [[10, 10], [20, 20], [10, 40]], \n" +
            "        [[40, 40], [30, 30], [40, 20], [30, 10]]\n" +
            "    ]\n" +
            "}";

    public static final String POLYGON =
            "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n" +
            "    ]\n" +
            "}";

    public static final String POLYGON_WITH_INTERNAL_POLYGON =
            "{\n" +
            "    \"type\": \"Polygon\", \n" +
            "    \"coordinates\": [\n" +
            "        [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], \n" +
            "        [[20, 30], [35, 35], [30, 20], [20, 30]]\n" +
            "    ]\n" +
            "}";

    public static final String MULTI_POINT =
            "{\n" +
            "    \"type\": \"MultiPoint\", \n" +
            "    \"coordinates\": [\n" +
            "        [10, 40], [40, 30], [20, 20], [30, 10]\n" +
            "    ]\n" +
            "}";

    public static final String MULTI_POLYGON =
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

    public static final String MULTI_POLYGON_WITH_INNER_POLYGON =
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
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void lineString() throws JsonProcessingException {
        final LineString geom = geoJsonGeometryReader.readValue(LINE_STRING);
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void multiLineString() throws JsonProcessingException {
        final MultiLineString geom = geoJsonGeometryReader.readValue(MULTI_LINE_STRING);
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void polygon() throws JsonProcessingException {
        final Polygon geom = geoJsonGeometryReader.readValue(POLYGON);
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void polygonWithInternalPolygon() throws JsonProcessingException {
        final Polygon geom = geoJsonGeometryReader.readValue(POLYGON_WITH_INTERNAL_POLYGON);
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void multiPoint() throws JsonProcessingException {
        final MultiPoint geom = geoJsonGeometryReader.readValue(MULTI_POINT);
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void multiPolygon() throws JsonProcessingException {
        final MultiPolygon geom = geoJsonGeometryReader.readValue(MULTI_POLYGON);
        assertNotNull(geom);
        log.info(geom.toString());
    }

    @Test
    public void multiPolygonWithInnerPolygon() throws JsonProcessingException {
        final MultiPolygon geom = geoJsonGeometryReader.readValue(MULTI_POLYGON_WITH_INNER_POLYGON);
        assertNotNull(geom);
        log.info(geom.toString());
    }
}
