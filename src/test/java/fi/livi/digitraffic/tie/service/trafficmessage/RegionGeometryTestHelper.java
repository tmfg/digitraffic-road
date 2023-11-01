package fi.livi.digitraffic.tie.service.trafficmessage;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeature;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.region.RegionGeometryFeatureCollection;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.model.trafficmessage.RegionGeometry;

public class RegionGeometryTestHelper {

    private static final GeoJsonReader GEOJSON_READER = new GeoJsonReader(GeometryConstants.JTS_GEOMETRY_FACTORY);

    public static String getGeneratedGeoJsonPolygon(final int seed) {
        // Generates x bewteen 27 +/- 4 and y in range 65 +/- 5
        final Random r = new Random(seed);
        final int xMaxDiff = 4;
        final double xDiff = xMaxDiff * r.nextGaussian();
        final int yMaxDiff = 5;
        final double yDiff = yMaxDiff * r.nextGaussian();

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

    public static RegionGeometry createNewRegionGeometry(final int locationCode) {
        return createNewRegionGeometry(locationCode, Instant.parse("2020-01-01T00:00:00Z"), RandomStringUtils.randomAlphanumeric(32));
    }
    public static RegionGeometry createNewRegionGeometry(final int locationCode, final Instant effectiveDate, final String commitId) {
        return createNewRegionGeometry(locationCode, effectiveDate, commitId, AreaType.MUNICIPALITY);
    }

    public static Map<Integer, List<RegionGeometry>> createRegionsInDescOrderMappedByLocationCode(final int...locationCodes) {
        final Map<Integer, List<RegionGeometry>> regionsInDescOrderMappedByLocationCode = new HashMap<>();
        Arrays.stream(locationCodes).forEach(locationCode -> regionsInDescOrderMappedByLocationCode.put(locationCode, createRegionGeometrySingletonCollection(locationCode)));
        return regionsInDescOrderMappedByLocationCode;
    }

    public static List<RegionGeometry> createRegionGeometrySingletonCollection(final int locationCode) {
        return Collections.singletonList(createNewRegionGeometry(locationCode));
    }

    public static RegionGeometryFeatureCollection createRegionGeometryFeatureCollection(final List<RegionGeometryFeature> regionGeometryFeatures) {
        return new RegionGeometryFeatureCollection(Instant.now(), regionGeometryFeatures);
    }

    public static RegionGeometry createNewRegionGeometry(final int locationCode, final Instant effectiveDate, final String commitId, final AreaType type) {
        try {
            final Geometry geometry = GEOJSON_READER.read(getGeneratedGeoJsonPolygon(locationCode));
            return new RegionGeometry(
                "Helsinki", locationCode, type,
                effectiveDate, geometry,
                effectiveDate,
                RandomStringUtils.randomAlphanumeric(32),
                "geometry/regions/" + StringUtils.leftPad("" + locationCode, 5, '0') + "_jokualue.json",
                commitId);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
}