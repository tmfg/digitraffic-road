package fi.livi.digitraffic.tie.data.controller.v2;

import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.MAINTENANCE_TRACKINGS_PATH;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_X;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_Y;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithLineString;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createWorkMachines;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getStartTimeOneHourInPast;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getTaskSetWithTasks;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper;

public class MaintenanceTrackingsControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingsControllerTest.class);

    final static String DOMAIN = "state-roads";
    final static String SOURCE = "Harja/Väylävirasto";

    @Autowired
    private V3MaintenanceTrackingServiceTestHelper testHelper;

    @Autowired
    private V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;

    private ResultActions getTrackingsJson(final Instant from, final Instant to, final Set<MaintenanceTrackingTask> tasks, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final String tasksParams = tasks.stream().map(t -> "&taskId=" + t.toString()).collect(Collectors.joining());
        final String url = API_V2_BASE_PATH + API_DATA_PART_PATH + MAINTENANCE_TRACKINGS_PATH +
            String.format(Locale.US, "?from=%s&to=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f%s", from.toString(), to.toString(), xMin, yMin, xMax, yMax, tasksParams);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("Response:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    private ResultActions getLatestTrackingsJson(final Instant from, final Set<MaintenanceTrackingTask> tasks, final double xMin, final double yMin, final double xMax, final double yMax) throws Exception {
        final String tasksParams = tasks.stream().map(t -> "&taskId=" + t.toString()).collect(Collectors.joining());
        final String url = API_V2_BASE_PATH + API_DATA_PART_PATH + MAINTENANCE_TRACKINGS_PATH + "/latest" +
                           String.format(Locale.US, "?from=%s&xMin=%f&yMin=%f&xMax=%f&yMax=%f%s",
                                         from.toString(), xMin, yMin, xMax, yMax, tasksParams);
        log.info("Get URL: {}", url);
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(url);
        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("Response:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    @BeforeEach
    public void initData() {
        testHelper.clearDb();
        TestUtils.entityManagerFlushAndClear(entityManager);
    }

    @Test
    public void assertNoWorkMachineIdInResult() throws Exception {
        final ZonedDateTime start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final int observationCount = 10;
        final List<Tyokone> workMachines = createWorkMachines(machineCount);

        testHelper.saveTrackingDataAsObservations( // end time will be same as start
            createMaintenanceTrackingWithLineString(start, observationCount, 1, workMachines, ASFALTOINTI, PAALLYSTEIDEN_PAIKKAUS));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // First tracking
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(start.toInstant(), start.toInstant(), new HashSet<>(),
                             RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()),
                             machineCount)
            .andExpect(jsonPath("features[*].properties.workMachineId").doesNotExist())
            .andExpect(jsonPath("features[*].properties.source", hasItems(equalTo(SOURCE))))
            .andExpect(jsonPath("features[*].properties.domain", hasItems(equalTo(DOMAIN))))
            .andExpect(jsonPath("features[*].properties.startTime").exists())
            .andExpect(jsonPath("features[*].properties.startTime", hasItems(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER)))
            .andExpect(jsonPath("features[*].properties.endTime").exists())
            .andExpect(jsonPath("features[*].properties.endTime", hasItems(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER)))
            .andExpect(jsonPath("features[*].properties.created").exists())
            .andExpect(jsonPath("features[*].properties.created", hasItems(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER)));
    }

    @Test
    public void findMaintenanceTrackingsWithinTime() throws Exception {
        final ZonedDateTime start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final List<Tyokone> firstHalfMachines = workMachines.subList(0, machineCount / 2);
        final List<Tyokone> secondHalfMachines = workMachines.subList(machineCount / 2, machineCount);

        testHelper.saveTrackingDataAsObservations( // end time will be start+9 min
            createMaintenanceTrackingWithPoints(start, 10, 1, firstHalfMachines, ASFALTOINTI, PAALLYSTEIDEN_PAIKKAUS));

        testHelper.saveTrackingDataAsObservations( // end time will be start+10+9 min
            createMaintenanceTrackingWithPoints(start.plusMinutes(10), 10, 1, secondHalfMachines, ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // First trackings, 10/machine
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                start.toInstant(), start.plusMinutes(9).toInstant(), new HashSet<>(),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), firstHalfMachines.size() * 10);

        // Second tracking, 10/machine
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                start.plusMinutes(10).toInstant(), start.plusMinutes(10+9).toInstant(), new HashSet<>(),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), secondHalfMachines.size() * 10);

        // Both
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                start.toInstant(), start.plusMinutes(10+9).toInstant(), new HashSet<>(),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), machineCount * 10);
    }

    @Test
    public void findMaintenanceTrackingsWithTasks() throws Exception {
        final ZonedDateTime start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);

        IntStream.range(0, machineCount).forEach(i -> {
            final Tyokone machine = workMachines.get(i);
            try {
                // end and start will be the same
                final TyokoneenseurannanKirjausRequestSchema havainnot =
                    createMaintenanceTrackingWithLineString(start, 10, 1, Collections.singletonList(machine),
                                                        SuoritettavatTehtavat.values()[i], SuoritettavatTehtavat.values()[i + 1]);
                testHelper.saveTrackingDataAsObservations(havainnot);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // find with first task should only find the first tracking for machine 1.
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                start.toInstant(), start.toInstant(), getTaskSetWithTasks(getTaskByharjaEnumName(SuoritettavatTehtavat.values()[0].name())),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), 1);

        // Search with second task should return trackings for machine 1. and 2.
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                start.toInstant(), start.toInstant(), getTaskSetWithTasks(getTaskByharjaEnumName(SuoritettavatTehtavat.values()[1].name())),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), 2);
    }

    private static MaintenanceTrackingTask getTaskByharjaEnumName(final String harjaTaskEnumName) {
        return MaintenanceTrackingTask.getByharjaEnumName(harjaTaskEnumName);
    }

    @Test
    public void findLatestMaintenanceTrackings() throws Exception {
        final ZonedDateTime start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);

        // Generate trackings for 50 minutes changing tasks every 10 minutes
        IntStream.range(0, 5).forEach(i -> {
            try {
                // end time will be the same as start
                final TyokoneenseurannanKirjausRequestSchema seuranta =
                    createMaintenanceTrackingWithLineString(
                        start.plusMinutes(i * 10L), 10, 1, workMachines,
                        SuoritettavatTehtavat.values()[i], SuoritettavatTehtavat.values()[i + 1]);
                testHelper.saveTrackingDataAsObservations(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineObservations(1000);
        final ZonedDateTime min = v2MaintenanceTrackingRepository.findAll().stream().map(MaintenanceTracking::getEndTime).min(ChronoZonedDateTime::compareTo).orElseThrow();
        final ZonedDateTime max = v2MaintenanceTrackingRepository.findAll().stream().map(MaintenanceTracking::getEndTime).max(ChronoZonedDateTime::compareTo).orElseThrow();

        log.info("min {} max {} from: {}", min, max, start.toInstant());

        log.info("Machine count {}", machineCount);
        // When getting latest trackings we should get only latest trackings per machine -> result of machineCount
        final ResultActions latestResult =
            expectOkFeatureCollectionWithSize(
                getLatestTrackingsJson(start.toInstant(), new HashSet<>(),
                                       RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), machineCount)
            .andExpect(jsonPath("features[*].properties.source", hasItems(equalTo(SOURCE))))
            .andExpect(jsonPath("features[*].properties.domain", hasItems(equalTo(DOMAIN))))
            .andExpect(jsonPath("features[*].properties.time").exists())
            .andExpect(jsonPath("features[*].properties.time", hasItems(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER)))
            .andExpect(jsonPath("features[*].properties.created").exists())
            .andExpect(jsonPath("features[*].properties.created", hasItems(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_MATCHER)));

        IntStream.range(0, machineCount).forEach(i -> {
            try {
                latestResult.andExpect(jsonPath("features[" + i + "].geometry.type", equalTo("Point")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // When getting all trackings we should all 5 trackings per machine -> result of machineCount*5
        final ResultActions trackingResult = getTrackingsJson(
            start.toInstant(), start.plusMinutes(4 * 10 + 9).toInstant(), new HashSet<>(),
            RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight())
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", hasSize(machineCount * 5)));
         IntStream.range(0, machineCount * 5).forEach(i -> {
             try {
                 trackingResult.andExpect(jsonPath("features[" + i + "].geometry.type", equalTo("LineString")));
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         });
    }

    @Test
    public void findLatestMaintenanceTrackingsWithTask() throws Exception {
        final ZonedDateTime start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);

        // Generate trackings for 50 minutes changing tasks every 10 minutes
        IntStream.range(0, 5).forEach(i -> {
            try {
                log.info("" + SuoritettavatTehtavat.values()[i].name());
                testHelper.saveTrackingDataAsObservations( // end time will be start+9 min
                    createMaintenanceTrackingWithPoints(
                        start.plusMinutes(i * 10L), 10, 1, workMachines,
                        SuoritettavatTehtavat.values()[i]));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineObservations(1000);
        final ZonedDateTime min = v2MaintenanceTrackingRepository.findAll().stream().map(MaintenanceTracking::getEndTime).min(ChronoZonedDateTime::compareTo).orElseThrow();
        final ZonedDateTime max = v2MaintenanceTrackingRepository.findAll().stream().map(MaintenanceTracking::getEndTime).min(ChronoZonedDateTime::compareTo).orElseThrow();

        log.info("min {} max {} from: {}", min, max, start.toInstant());
        log.info("Machine count {}", machineCount);

        // When getting latest trackings we should get only latest trackings per machine -> result of machineCount
        expectOkFeatureCollectionWithSize(
            getLatestTrackingsJson(
                start.toInstant(), new HashSet<>(Collections.singleton(MaintenanceTrackingTask.getByharjaEnumName(SuoritettavatTehtavat.values()[4].name()))),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), machineCount);
    }

    @Test
    public void findWithBoundingBox() throws Exception {
        final ZonedDateTime now = getStartTimeOneHourInPast();

        final List<Tyokone> workMachines = createWorkMachines(1);
        final TyokoneenseurannanKirjausRequestSchema k =
            createMaintenanceTrackingWithLineString(now, 10, 1, workMachines, ASFALTOINTI);
        testHelper.saveTrackingDataAsObservations(k);
        testHelper.handleUnhandledWorkMachineObservations(1000);

        final Double maxX = k.getHavainnot().stream()
            .map(h -> h.getHavainto().getSijainti().getViivageometria().getCoordinates().stream()
                    .map(c -> (Double) c.get(0)) // X
                    .max(Double::compareTo)
                    .orElseThrow())
            .max(Double::compareTo)
            .orElseThrow();
        final Double maxY = k.getHavainnot().stream()
            .map(h -> h.getHavainto().getSijainti().getViivageometria().getCoordinates().stream()
                .map(c -> (Double) c.get(1))// y
                .max(Double::compareTo)
                .orElseThrow())
            .max(Double::compareTo)
            .orElseThrow();

        Point pointWGS84 = CoordinateConverter.convertFromETRS89ToWGS84(new Point(maxX, maxY));

        log.info(pointWGS84.toString());
        // The only tracking should be found when it's inside the bounding box
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                now.toInstant(), now.toInstant(), new HashSet<>(),
                RANGE_X.getLeft(), RANGE_Y.getLeft(), RANGE_X.getRight(), RANGE_Y.getRight()), 1);
        // The only tracking should not be found when it's not inside the bounding box
        expectOkFeatureCollectionWithSize(
            getTrackingsJson(
                now.toInstant(), now.toInstant(), new HashSet<>(),
                pointWGS84.getLongitude()+0.1, pointWGS84.getLatitude()+0.1, RANGE_X.getRight(), RANGE_Y.getRight()), 0);
    }
}