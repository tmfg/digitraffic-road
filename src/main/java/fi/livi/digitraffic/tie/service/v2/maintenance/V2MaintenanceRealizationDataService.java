package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationPointRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceRealizationRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTaskRepository;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationCoordinateDetails;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationProperties;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTask;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskCategory;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationTaskOperation;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationPoint;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTaskCategory;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTaskOperation;
import fi.livi.digitraffic.tie.service.DataStatusService;

@Service
public class V2MaintenanceRealizationDataService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationDataService.class);
    private final V2MaintenanceRealizationRepository v2RealizationRepository;
    private final V2MaintenanceRealizationDataRepository v2RealizationDataRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final V2MaintenanceTaskRepository v2MaintenanceTaskRepository;
    private final V2MaintenanceRealizationPointRepository v2RealizationPointRepository;
    private final DataStatusService dataStatusService;

    @Autowired
    public V2MaintenanceRealizationDataService(final V2MaintenanceRealizationRepository v2RealizationRepository,
                                               final V2MaintenanceRealizationDataRepository v2RealizationDataRepository,
                                               final ObjectMapper objectMapper,
                                               final V2MaintenanceTaskRepository v2MaintenanceTaskRepository,
                                               final V2MaintenanceRealizationPointRepository v2RealizationPointRepository,
                                               final DataStatusService dataStatusService) {
        this.v2RealizationRepository = v2RealizationRepository;
        this.v2RealizationDataRepository = v2RealizationDataRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        this.v2MaintenanceTaskRepository = v2MaintenanceTaskRepository;
        this.v2RealizationPointRepository = v2RealizationPointRepository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public MaintenanceRealizationFeatureCollection findMaintenanceRealizations(final Instant from, final Instant to,
                                                                               final double xMin, final double yMin,
                                                                               final double xMax, final double yMax) {
        final ZonedDateTime lastUpdated = DateHelper.toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_REALIZATION_DATA));
        final ZonedDateTime lastChecked = DateHelper.toZonedDateTimeAtUtc(dataStatusService.findDataUpdatedTime(DataType.MAINTENANCE_REALIZATION_DATA_CHECKED));
        final List<MaintenanceRealization> found = v2RealizationRepository.findByAgeAndBoundingBox(from, to, xMin, yMin, xMax, yMax);
        final List<MaintenanceRealizationFeature> features = convertToFeatures(found);
        return new MaintenanceRealizationFeatureCollection(lastUpdated, lastChecked, features);
    }

    @Transactional
    public List<MaintenanceRealizationTask> findAllRealizationsTasks() {
        return v2MaintenanceTaskRepository.findAllByOrderById().stream()
            .map(t -> createMaintenanceRealizationTask(t)).collect(Collectors.toList());
    }

    @Transactional
    public List<MaintenanceRealizationTaskOperation> findAllRealizationsTaskOperations() {
        return v2MaintenanceTaskRepository.findAllOperationsOrderById();
    }

    @Transactional
    public List<MaintenanceRealizationTaskCategory> findAllRealizationsTaskCategories() {
        return v2MaintenanceTaskRepository.findAllCategoriesOrderById();
    }

    private List<MaintenanceRealizationFeature> convertToFeatures(final List<MaintenanceRealization> all) {
        return all.stream().map(r -> {

                final Set<Long> taskIds = convertToMaintenanceRealizationTaskIds(r.getTasks());
                final List<List<Double>> coordinates = convertToCoordinates(r.getLineString());
                final List<MaintenanceRealizationCoordinateDetails> coordinateDetails = convertToMaintenanceCoordinateDetails(r.getRealizationPoints());
                final MaintenanceRealizationProperties properties = new MaintenanceRealizationProperties(toZonedDateTimeAtUtc(r.getSendingTime()), taskIds, coordinateDetails);
                return new MaintenanceRealizationFeature(new LineString(coordinates), properties);

        }).collect(Collectors.toList());
    }

    private List<List<Double>> convertToCoordinates(final org.locationtech.jts.geom.LineString lineString) {
        return Arrays.stream(lineString.getCoordinates())
            .map(c -> Arrays.asList(c.getX(), c.getY(), c.getZ()))
            .collect(Collectors.toList());
    }

    private List<MaintenanceRealizationCoordinateDetails> convertToMaintenanceCoordinateDetails(final List<MaintenanceRealizationPoint> points) {
        return points.stream().map(p -> new MaintenanceRealizationCoordinateDetails(toZonedDateTimeAtUtc(p.getTime()))).collect(Collectors.toList());
    }

    private MaintenanceRealizationTask createMaintenanceRealizationTask(MaintenanceTask t) {
        final MaintenanceTaskCategory c = t.getCategory();
        final MaintenanceTaskOperation o = t.getOperation();
        return new MaintenanceRealizationTask(t.getId(), t.getFi(), t.getSv(), t.getEn(),
            new MaintenanceRealizationTaskOperation(o.getId(), o.getFi(), o.getSv(), o.getEn()),
            new MaintenanceRealizationTaskCategory(c.getId(), c.getFi(), c.getSv(), c.getEn()));
    }

    private Set<Long> convertToMaintenanceRealizationTaskIds(final Set<MaintenanceTask> tasks) {
        return tasks.stream()
            .map(MaintenanceTask::getId)
            .collect(Collectors.toSet());
    }
}
