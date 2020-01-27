package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

import fi.livi.digitraffic.tie.dao.v2.V2RealizationDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationPointRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationRepository;
import fi.livi.digitraffic.tie.dao.v2.V2RealizationTaskRepository;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeature;
import fi.livi.digitraffic.tie.dto.v2.maintenance.MaintenanceRealizationFeatureCollection;
import fi.livi.digitraffic.tie.external.harja.ReittitoteumanKirjausRequestSchema;
import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealization;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationPoint;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

@Service
public class V2MaintenanceRealizationDataService {

    private static final Logger log = LoggerFactory.getLogger(V2MaintenanceRealizationDataService.class);
    private final V2RealizationRepository v2RealizationRepository;
    private final V2RealizationDataRepository v2RealizationDataRepository;
    private final ObjectWriter jsonWriter;
    private final ObjectReader jsonReader;
    private final V2RealizationTaskRepository v2RealizationTaskRepository;
    private final V2RealizationPointRepository v2RealizationPointRepository;

    private Map<Long, MaintenanceTask> tasksMap;

    @Autowired
    public V2MaintenanceRealizationDataService(final V2RealizationRepository v2RealizationRepository,
                                               final V2RealizationDataRepository v2RealizationDataRepository,
                                               final ObjectMapper objectMapper,
                                               final V2RealizationTaskRepository v2RealizationTaskRepository,
                                               final V2RealizationPointRepository v2RealizationPointRepository) {
        this.v2RealizationRepository = v2RealizationRepository;
        this.v2RealizationDataRepository = v2RealizationDataRepository;
        this.jsonWriter = objectMapper.writerFor(ReittitoteumanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(ReittitoteumanKirjausRequestSchema.class);
        this.v2RealizationTaskRepository = v2RealizationTaskRepository;
        this.v2RealizationPointRepository = v2RealizationPointRepository;
    }

    @Transactional
    public MaintenanceRealizationFeatureCollection findMaintenanceRealizations(final int historyHours) {
        MaintenanceRealizationFeatureCollection fc = new MaintenanceRealizationFeatureCollection(ZonedDateTime.now(), ZonedDateTime.now());
        List<MaintenanceRealization> all = v2RealizationRepository.findAll();
        fc.addAll(convertToFeatures(all));
        return fc;
    }

    private List<MaintenanceRealizationFeature> convertToFeatures(List<MaintenanceRealization> all) {
        final List<MaintenanceRealizationFeature> features =
            all.stream().map(r -> {
                final List<List<Double>> coordinates =
                    Arrays.stream(r.getLineString().getCoordinates())
                        .map(c -> Arrays.asList(c.getX(), c.getY(), c.getZ())).collect(Collectors.toList());
                final Set<MaintenanceTask> tasks = r.getTasks();
                final ZonedDateTime sendingTime = r.getSendingTime();
                final List<MaintenanceRealizationPoint> points = r.getRealizationPoints();

            return new MaintenanceRealizationFeature(new LineString(coordinates));
        }).collect(Collectors.toList());
        return features;
    }

}
