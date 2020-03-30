package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceTrackingProperties;
import fi.livi.digitraffic.tie.helper.PostgisGeometryHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.DataStatusService;

/**
 * This service returns Harja tracking data for public use
 *
 * @see {@link V2MaintenanceTrackingUpdateService}
 * @See <a href="https://github.com/finnishtransportagency/harja">https://github.com/finnishtransportagency/harja</a>
 */
@Service
public class V2MaintenanceTrackingDataService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceTrackingDataService.class);
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;
    private final DataStatusService dataStatusService;

    @Autowired
    public V2MaintenanceTrackingDataService(final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                            final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository,
                                            final DataStatusService dataStatusService) {
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.v2MaintenanceTrackingDataRepository = v2MaintenanceTrackingDataRepository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollection findLatestMaintenanceTrackings(final Instant endTimefrom, final Instant endTimeto,
                                                                               final double xMin, final double yMin,
                                                                               final double xMax, final double yMax,
                                                                               final List<MaintenanceTrackingTask> taskIds) {
        final ZonedDateTime lastUpdated = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA));
        final ZonedDateTime lastChecked = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED));

        final Polygon area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTracking> found = taskIds == null || taskIds.isEmpty() ?
                                                v2MaintenanceTrackingRepository
                                                    .findLatestByAgeAndBoundingBox(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area) :
                                                v2MaintenanceTrackingRepository
                                                    .findLatestByAgeAndBoundingBoxAndTasks(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area, taskIds);
        log.info("method=findMaintenanceRealizations with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
            xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), found.size(), start.getTime());
        final List<MaintenanceTrackingFeature> features = convertToTrackingFeatures(found, true);
        return new MaintenanceTrackingFeatureCollection(lastUpdated, lastChecked, features);
    }

    @Transactional(readOnly = true)
    public MaintenanceTrackingFeatureCollection findMaintenanceTrackings(final Instant endTimefrom, final Instant endTimeto,
                                                                         final double xMin, final double yMin,
                                                                         final double xMax, final double yMax,
                                                                         final List<MaintenanceTrackingTask> taskIds) {
        final ZonedDateTime lastUpdated = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA));
        final ZonedDateTime lastChecked = toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_TRACKING_DATA_CHECKED));

        final Polygon area = PostgisGeometryHelper.createSquarePolygonFromMinMax(xMin, xMax, yMin, yMax);

        final StopWatch start = StopWatch.createStarted();
        final List<MaintenanceTracking> found = taskIds == null || taskIds.isEmpty() ?
                                                v2MaintenanceTrackingRepository
                                                    .findByAgeAndBoundingBox(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area) :
                                                v2MaintenanceTrackingRepository
                                                    .findByAgeAndBoundingBoxAndTasks(toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), area, taskIds);
        log.info("method=findMaintenanceRealizations with params xMin {}, xMax {}, yMin {}, yMax {} fromTime={} toTime={} foundCount={} tookMs={}",
            xMin, xMax, yMin, yMax, toZonedDateTimeAtUtc(endTimefrom), toZonedDateTimeAtUtc(endTimeto), found.size(), start.getTime());
        final List<MaintenanceTrackingFeature> features = convertToTrackingFeatures(found, false);
        return new MaintenanceTrackingFeatureCollection(lastUpdated, lastChecked, features);
    }

    @Transactional(readOnly = true)
    public List<JsonNode> findTrackingDataJsonsByTrackingId(final long trackingId) {
        final ObjectMapper om = new ObjectMapper();
        return v2MaintenanceTrackingDataRepository.findJsonsByTrackingId(trackingId).stream().map(j -> {
            try {
                return om.readTree(j);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    /**
     *
     * @param trackings to convert
     * @param latestPointGeometry if true then only the latest point will be returned as the geometry
     * @return
     */
    private List<MaintenanceTrackingFeature> convertToTrackingFeatures(final List<MaintenanceTracking> trackings, final boolean latestPointGeometry) {
        return trackings.stream().map(r -> {
            final Geometry geometry = convertToGeoJSONGeometry(r, latestPointGeometry);
            final MaintenanceTrackingProperties properties =
                new MaintenanceTrackingProperties(r.getId(),
                    r.getWorkMachine(),
                    toZonedDateTimeAtUtc(r.getSendingTime()),
                    toZonedDateTimeAtUtc(r.getStartTime()),
                    toZonedDateTimeAtUtc(r.getEndTime()),
                    r.getTasks());
            return new MaintenanceTrackingFeature(geometry, properties);
        }).collect(Collectors.toList());
    }

    /**
     *
     * @param tracking
     * @param latestPointGeometry if true then only the latest point will be returned as the geometry.
     * @return
     */
    private Geometry convertToGeoJSONGeometry(final MaintenanceTracking tracking, boolean latestPointGeometry) {
        if (!latestPointGeometry && tracking.getLineString() != null) {
            return new LineString(PostgisGeometryHelper.convertToGeoJSONGeometryCoordinates(tracking.getLineString()));
        } else {
            final Coordinate coordinate = tracking.getLastPoint().getCoordinate();
            return new Point(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        }

    }
}
