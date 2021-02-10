package fi.livi.digitraffic.tie.service.v2.datex2;

import java.time.Instant;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import fi.livi.digitraffic.tie.dto.v3.trafficannouncement.geojson.AreaType;
import fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson.RegionGeometry;

public class RegionGeometryTestHelper {

    private static final GeoJsonReader GEOJSON_READER = new GeoJsonReader();

    private static final String geoJsonPolygon =
        "{\n" +
            "   \"type\": \"Polygon\",\n" +
            "   \"coordinates\": [\n" +
            "       [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n" +
            "       [100.0, 1.0], [100.0, 0.0] ]\n" +
            "   ]\n" +
            "}";

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
            final Geometry geometry = GEOJSON_READER.read(geoJsonPolygon);
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