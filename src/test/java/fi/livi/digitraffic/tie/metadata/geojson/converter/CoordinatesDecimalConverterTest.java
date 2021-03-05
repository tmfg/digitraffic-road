package fi.livi.digitraffic.tie.metadata.geojson.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CoordinatesDecimalConverterTest extends AbstractDaemonTestWithoutS3 {
    private static final Logger log = LoggerFactory.getLogger(CoordinatesDecimalConverterTest.class);

    @Autowired
    private ObjectMapper objectMapper;

    private static final String POINT =
        "{\n" +
            "  \"type\" : \"Point\",\n" +
            "  \"coordinates\" : [ 0.100000, 2.900000, 3.000000 ]\n" +
            "}";
    private static final String LINE_STRING =
        "{\n" +
        "  \"type\" : \"LineString\",\n" +
        "  \"coordinates\" : [ [ 1.111111, 1.255556, 1.300000 ], [ 2.100000, 2.200000, 2.300000 ], [ 3.100000, 3.200000, 3.300000 ] ]\n" +
        "}";

    @Test
    public void point() throws JsonProcessingException {
        final Point point = new Point(0.1, 2.9, 2.9999999999);
        final String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(point);
        log.info(json);
        Assert.assertEquals(POINT, json);
    }

    @Test
    public void lineString() throws JsonProcessingException {
        final List<List<Double>> coordinates = new ArrayList<>();
        coordinates.add(asList(1.1111111111, 1.2555555555, 1.3));
        coordinates.add(asList(2.1, 2.2, 2.3));
        coordinates.add(asList(3.1, 3.2, 3.3));
        final LineString lineString = new LineString(coordinates);
        final String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(lineString);
        log.info(json);
        Assert.assertEquals(LINE_STRING, json);
    }

    public static List<Double> asList(Double... a) {
        return Arrays.asList(a);
    }

/*
    LineString
    MultiLineString
    MultiPoint
    MultiPolygon
    Point
    Polygon
  */
}
