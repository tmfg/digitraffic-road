package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.Instant;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AreaType;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;

public class RegionGeometryTestHelper {

    private static final GeoJsonReader GEOJSON_READER = new GeoJsonReader();

    public static String getGeneratedGeoJsonPolygon(int seed) {
        // Generates x bewteen 27 +/- 4 and y in range 65 +/- 5
        final Random r = new Random(seed);
        final int xMaxDiff = 4;
        double xDiff = xMaxDiff * r.nextGaussian();
        final int yMaxDiff = 5;
        double yDiff = yMaxDiff * r.nextGaussian();

        return
            "{" +
                "\"type\":\"Polygon\"," +
                "\"coordinates\": [" +
                "   [["+(27.0+xDiff)+", "+(65.0+yDiff)+"], ["+(27.1+xDiff)+", "+(65.0+yDiff)+"], ["+(27.1+xDiff)+", "+(65.1+yDiff)+"], ["+(27.0+xDiff)+", "+(65.1+yDiff)+"], ["+(27.0+xDiff)+", "+(65.0+yDiff)+"]], " +
                "   [["+(27.01+xDiff)+", "+(65.01+yDiff)+"], ["+(27.09+xDiff)+", "+(65.01+yDiff)+"], ["+(27.09+xDiff)+", "+(65.09+yDiff)+"], ["+(27.01+xDiff)+", "+(65.09+yDiff)+"], ["+(27.01+xDiff)+", "+(65.01+yDiff)+"]]" +
                "]" +
            "}";
    }

    public static RegionGeometry createNewRegionGeometry() {
        return createNewRegionGeometry(1, Instant.parse("2020-01-01T00:00:00Z"), RandomStringUtils.randomAlphanumeric(32));
    }

    public static RegionGeometry createNewRegionGeometry(int locationCode) {
        return createNewRegionGeometry(locationCode, Instant.parse("2020-01-01T00:00:00Z"), RandomStringUtils.randomAlphanumeric(32));
    }
    public static RegionGeometry createNewRegionGeometry(int locationCode, final Instant effectiveDate, final String commitId) {
        return createNewRegionGeometry(locationCode, effectiveDate, commitId, AreaType.MUNICIPALITY);
    }

    public static RegionGeometry createNewRegionGeometry(int locationCode, final Instant effectiveDate, final String commitId, final AreaType type) {
        try {
            final Geometry geometry = GEOJSON_READER.read(getGeneratedGeoJsonPolygon(locationCode));
            return new RegionGeometry(
                "Helsinki", locationCode, type,
                effectiveDate, geometry,
                effectiveDate,
                RandomStringUtils.randomAlphanumeric(32),
                "geometry/regions/" + StringUtils.leftPad("" + locationCode, 5, '0') + "_jokualue.json",
                commitId);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}