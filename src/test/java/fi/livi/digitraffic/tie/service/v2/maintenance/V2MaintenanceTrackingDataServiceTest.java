package fi.livi.digitraffic.tie.service.v2.maintenance;

import static fi.livi.digitraffic.tie.TestUtils.commitAndEndTransactionAndStartNew;
import static fi.livi.digitraffic.tie.TestUtils.getRandom;
import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.AURAUS_JA_SOHJONPOISTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_JUOTOSTYOT;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Point;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.BRUSHING;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.CRACK_FILLING;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.PAVING;
import static fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask.PLOUGHING_AND_SLUSH_REMOVAL;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_X_MAX;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_X_MIN;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_Y_MAX;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.RANGE_Y_MIN;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithLineString;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createVerticalLineStringWGS84;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createWorkMachines;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createWorkmachine;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getEndTime;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getStartTimeOneHourInPast;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getTaskSetWithIndex;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getTaskSetWithTasks;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getTaskWithIndex;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeature;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeature;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollection;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingProperties;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper;

public class V2MaintenanceTrackingDataServiceTest extends AbstractServiceTest {

    final static Pair<Double, Double> BOUNDING_BOX_X_RANGE = Pair.of(20.0, 30.0);
    final static Pair<Double, Double> BOUNDING_BOX_Y_RANGE = Pair.of(64.0, 66.0);
    final static Pair<Double, Double> BOUNDING_BOX_CENTER = Pair.of(25.0, 65.0);

    private final String DOMAIN_WITH_SOURCE = "domain-with-source";
    private final String DOMAIN_WITHOUT_SOURCE = "domain-without-source";

    @Autowired
    private V2MaintenanceTrackingDataService v2MaintenanceTrackingDataService;

    @Autowired
    private V3MaintenanceTrackingServiceTestHelper testHelper;

    @AfterEach
    @BeforeEach
    public void init() {
        testHelper.clearDb();
        testHelper.deleteDomains(DOMAIN_WITH_SOURCE, DOMAIN_WITHOUT_SOURCE);
        commitAndEndTransactionAndStartNew();
    }

    @Test
    public void findCombinedTrackingsWithMultipleWorkMachines() throws JsonProcessingException {
        final ZonedDateTime start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final TyokoneenseurannanKirjausRequestSchema seuranta1 =
            createMaintenanceTrackingWithPoints(start, 10, 1, 1, workMachines, ASFALTOINTI);
        final TyokoneenseurannanKirjausRequestSchema seuranta2 =
            createMaintenanceTrackingWithPoints(start.plusMinutes(10), 10, 2,1, workMachines, ASFALTOINTI);
        final ZonedDateTime end = getEndTime(seuranta2);

        testHelper.saveTrackingDataAsObservations(seuranta1);
        testHelper.saveTrackingDataAsObservations(seuranta2);
        testHelper.handleUnhandledWorkMachineObservations(1000);

        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(start, end, PAVING).getFeatures();
        assertCollectionSize(machineCount, features);

        final Set<Long> workMachineUniqueIds = new HashSet<>();
        features.forEach(t -> {
            final MaintenanceTrackingProperties properties = t.getProperties();
            // As observations are simplified, we can't know how many points is left in geometry
            assertTrue(t.getGeometry().getCoordinates().size() > 1);
            assertEquals(getTaskSetWithTasks(PAVING), t.getProperties().tasks);
            assertEquals(start.toInstant(), properties.startTime);
            assertEquals(end.toInstant(), properties.endTime);
            // {machineCount} unique machines
            assertFalse(workMachineUniqueIds.contains(properties.workMachineId));
            workMachineUniqueIds.add(properties.workMachineId);
        });
    }

    @Test
    public void findCombinedMultipleTrackings() {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        // Generate 5 messages with 10 observations/machine in each
        final ZonedDateTime endTime = IntStream.range(0, 5).mapToObj(i -> {
            final ZonedDateTime start = startTime.plusMinutes(i* 10L);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(start, 10, i + 1, 1, workMachines, ASFALTOINTI, PAALLYSTEIDEN_JUOTOSTYOT);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return getEndTime(seuranta);
        }).max(ChronoZonedDateTime::compareTo).orElseThrow();
        final int expectedObservationCount = 5 * machineCount * 10; // 5 messages, 10 observations/machine in each
        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(expectedObservationCount, handled);

        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, endTime).getFeatures();
        assertCollectionSize(machineCount, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);

        // All messages are combined as one/machine
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, endTime).getFeatures();
        assertCollectionSize(machineCount, features);

        // Check features properties
        features.forEach(t -> {
            // 10 observations for each
            final MaintenanceTrackingProperties properties = t.getProperties();
            // As observations are simplified, we can't know how many points is left in geometry
            assertTrue(t.getGeometry().getCoordinates().size() > 1);
            assertEquals(getTaskSetWithTasks(PAVING, CRACK_FILLING), properties.tasks);
            assertEquals(startTime.toInstant(), properties.startTime);
            assertEquals(endTime.toInstant(), properties.endTime);
        });
    }

    @Test
    public void findTrackingWithDifferentTasks() {
        final int machineCount = getRandomId(2, 10);
        // Work machines with harja id 1,2,...(machineCount+1)
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        // Generate 5 different messages with different tasks for each machine
        // Each machine successive trackings will bombine next tracking first point as previous tracking last point
        IntStream.range(0, 5).forEach(idx -> {
            final ZonedDateTime start = startTime.plusMinutes(idx*10L);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(start, 10, 1, workMachines, SuoritettavatTehtavat.values()[idx]);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // Without task parameter all should be found
        List<MaintenanceTrackingFeature> allFeatures =
            findMaintenanceTrackings(startTime, startTime.plusMinutes(10 * 5 + 9)).getFeatures();
        assertCollectionSize(machineCount*5, allFeatures);

        // Latest should return latest one per machine
        List<MaintenanceTrackingLatestFeature> latestFeatures =
            findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(10 * 5 + 9)).getFeatures();
        assertCollectionSize(machineCount, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);

        // Find with tasks
        IntStream.range(0, 5).forEach(idx -> {
            final ZonedDateTime endTime = startTime.plusMinutes(idx*10L+10L); // 10 observations end time is 10 min after first
            final List<MaintenanceTrackingFeature> features =
                findMaintenanceTrackings(startTime, endTime, getTaskWithIndex(idx)).getFeatures();
            assertCollectionSize(machineCount, features);
            features.forEach(f -> assertEquals(getTaskSetWithIndex(idx), f.getProperties().tasks));
        });
    }

    @Test
    public void findTrackingWithDifferentJobsForSameMachine() throws JsonProcessingException {
        // Work machines with harja id 1
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();
        // tracking with job 1
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // tracking with job 2
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // 2 tracking should be made as they have different jobs
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(10+10)).getFeatures();
        assertCollectionSize(2, features);

        // 2 tracking should be found as latest as same machine with different job id is handled as different machine
        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(10+10)).getFeatures();
        assertCollectionSize(2, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void findSeparateTrackingsWhenTransitionInBetweenTasks() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();
        // tracking with job 1
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 1, workMachines));
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10+10), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // 2 tracking should be made as they have different jobs
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(10+10+10)).getFeatures();
        assertCollectionSize(2, features);

        // Only the latest one should be found
        final List<MaintenanceTrackingLatestFeature> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plusMinutes(10+10+10)).getFeatures();
        assertCollectionSize(1, latestFeatures);
        assertEquals(startTime.plusMinutes(10+10+9).toInstant(), latestFeatures.get(0).getProperties().getTime());
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void observationsTimesCrossesBetweenMessagesThenTrackingsAreCombined() throws JsonProcessingException {
        // M1 = Message 1, P1=Point 1. Then observation times are:
        // M1(P1) < M2(P1) < M1(P2) < M2(P2) < M1(P3) < M2(P3) ...
        final List<Tyokone> workMachines = createWorkMachines(getRandom(1,5));
        final ZonedDateTime startTime = getStartTimeOneHourInPast();
        // tracking with startime T1 and end time T1+9
        // 10 observations / machine
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // tracking with startime T1+30s and end time T1+9m+30s
        // 10 observations / machine, each time between first observations points times
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusSeconds(30), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // 2 tracking should be made as first one's end time is after second one's start time.
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(1+9)).getFeatures();
        assertCollectionSize(workMachines.size(), features);
    }

    @Test
    public void findTrackingDataJsonsByTrackingId() throws IOException {
        final List<Tyokone> workMachines = createWorkMachines(getRandom(1,5));
        final ZonedDateTime startTime = getStartTimeOneHourInPast();
        // Create 2 messages that are combined as one tracking
        // 10 observations
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // 10 observations
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);
        final int expectedObservationCountPerMachine = 10 + 10;
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, startTime.plusMinutes(20)).getFeatures();
        assertCollectionSize(workMachines.size(), features);

        final List<JsonNode> jsons = v2MaintenanceTrackingDataService.findTrackingDataJsonsByTrackingId(features.get(0).getProperties().id);
        assertCollectionSize(expectedObservationCountPerMachine, jsons);
    }

    @Test
    public void findWithMultipleTasks() throws JsonProcessingException {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();
        final ZonedDateTime endTime = startTime.plusMinutes(9);
        // Generate 3 observations for each machine
        testHelper.saveTrackingDataAsObservations(createMaintenanceTrackingWithLineString(startTime, 10, 1, workMachines,
            AURAUS_JA_SOHJONPOISTO, PAALLYSTEIDEN_JUOTOSTYOT)); // PLOUGHING_AND_SLUSH_REMOVAL, CRACK_FILLING
        testHelper.saveTrackingDataAsObservations(createMaintenanceTrackingWithLineString(startTime, 10, 2, workMachines,
            ASFALTOINTI)); // PAVING
        testHelper.saveTrackingDataAsObservations(createMaintenanceTrackingWithLineString(startTime, 10, 3, workMachines,
            AURAUS_JA_SOHJONPOISTO, PAALLYSTEIDEN_PAIKKAUS)); // PLOUGHING_AND_SLUSH_REMOVAL, PATCHING
        final int expectedHavaintoCount = machineCount * 3;
        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(expectedHavaintoCount, handled);

        // First two should be returned
        final List<MaintenanceTrackingFeature> features = findMaintenanceTrackings(startTime, endTime, CRACK_FILLING, PAVING).getFeatures();
        assertCollectionSize(machineCount*2, features);

        // Check that each features tasks are AURAUS_JA_SOHJONPOISTO and PAALLYSTEIDEN_JUOTOSTYOT or ASFALTOINTI
        features.forEach(f -> {
            final Set<MaintenanceTrackingTask> tasks = f.getProperties().tasks;
            if (tasks.size() == 2) {
                assertEquals(new HashSet<>(asList(PLOUGHING_AND_SLUSH_REMOVAL, CRACK_FILLING)), tasks);
            } else {
                assertEquals(new HashSet<>(singletonList(PAVING)), tasks);
            }
        });
    }

    /**
     *      | - linestring
     * –––––|––––-
     * |    |    | - bounding box
     * |    |    |
     * –––––|-----
     *      |
     */
    @Test
    public void findMaintenanceTrackingWithLinestringCrossingWithBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_CENTER.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft()-0.5, BOUNDING_BOX_Y_RANGE.getRight() + 0.5);

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            V3MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList(),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
        assertEquals(1, result.getFeatures().size());
        final MaintenanceTrackingProperties props = result.getFeatures().get(0).getProperties();

        assertEquals(startTime.toInstant(), props.startTime);
        assertEquals(startTime.toInstant(), props.endTime);
    }


    /**
     *                          | - linestring
     *                –––––––––-|
     * bounding box - |        ||
     *                |        ||
     *                –––––––––-|
     *                          |
     */
    @Test
    public void findMaintenanceTrackingWithLinestringNotCrossingWithBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_X_RANGE.getRight() + 0.1,
                                                                     BOUNDING_BOX_Y_RANGE.getLeft() - 10,
                                                                     BOUNDING_BOX_Y_RANGE.getRight() + 10);
        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            V3MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList(),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
        assertEquals(0, result.getFeatures().size());
    }

    /**
     * ––––––––––-
     * |  point  | - bounding box
     * |    `    |
     * ––––––-----
     */
    @Test
    public void findMaintenanceTrackingWithPointInsideBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = singletonList(
            asList(BOUNDING_BOX_CENTER.getLeft(), BOUNDING_BOX_CENTER.getRight())
        );

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            V3MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList(),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
        assertEquals(1, result.getFeatures().size());
        final MaintenanceTrackingProperties props = result.getFeatures().get(0).getProperties();

        assertEquals(startTime.toInstant(), props.startTime);
        assertEquals(startTime.toInstant(), props.endTime);
    }

    /**
     * ––––––––––-
     * |         | - bounding box
     * |         |` - point
     * ––––––-----
     */
    @Test
    public void findMaintenanceTrackingWithPointOutsideBoundingBox() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = singletonList(
            asList(BOUNDING_BOX_X_RANGE.getRight() + 0.1, BOUNDING_BOX_CENTER.getRight())
        );

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            V3MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList(),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
        assertEquals(0, result.getFeatures().size());
    }

    @Test
    public void getById() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final ZonedDateTime startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getRight());

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            V3MaintenanceTrackingServiceTestHelper.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollection result1 = v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            startTime.toInstant(), startTime.toInstant(),
            BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight(),
            Collections.emptyList(),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
        final MaintenanceTrackingFeature feature1 = result1.getFeatures().get(0);

        final MaintenanceTrackingFeature feature2 =
            v2MaintenanceTrackingDataService.getMaintenanceTrackingById(result1.getFeatures().get(0).getProperties().id);

        assertEquals(feature1.getGeometry(), feature2.getGeometry());
        assertEquals(feature1.getProperties().id, feature2.getProperties().id);
    }

    @Test
    public void getByIdWithAndWitoutDomainSource() {
        // Create trackings for domains with and witout soure
        final MaintenanceTrackingWorkMachine wm1 = testHelper.createAndSaveWorkMachine();
        final MaintenanceTrackingWorkMachine wm2 = testHelper.createAndSaveWorkMachine();
        testHelper.insertDomain(DOMAIN_WITH_SOURCE, "Foo/Bar");
        testHelper.insertDomain(DOMAIN_WITHOUT_SOURCE, null);
        commitAndEndTransactionAndStartNew();
        final long trackingId1 = insertTrackingForDomain(DOMAIN_WITH_SOURCE, wm1.getId());
        final long trackingId2 = insertTrackingForDomain(DOMAIN_WITHOUT_SOURCE, wm2.getId());
        entityManager.flush();
        commitAndEndTransactionAndStartNew();

        // Tracking for domain with source should be found
        assertNotNull(v2MaintenanceTrackingDataService.getMaintenanceTrackingById(trackingId1));
        assertThrows(ObjectNotFoundException.class, () -> {
            // Tracking for domain without source should not be found
            v2MaintenanceTrackingDataService.getMaintenanceTrackingById(trackingId2);
        });
    }

    private MaintenanceTrackingFeatureCollection findMaintenanceTrackings(final ZonedDateTime start, final ZonedDateTime end,
                                                                              final MaintenanceTrackingTask...tasks) {
        return v2MaintenanceTrackingDataService.findMaintenanceTrackings(
            start.toInstant(), end.toInstant(),
            RANGE_X_MIN, RANGE_Y_MIN, RANGE_X_MAX, RANGE_Y_MAX,
            asList(tasks),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
    }

    private MaintenanceTrackingLatestFeatureCollection findLatestMaintenanceTrackings(final ZonedDateTime start, final ZonedDateTime end,
                                                                                      final MaintenanceTrackingTask...tasks) {
        return v2MaintenanceTrackingDataService.findLatestMaintenanceTrackings(
            start.toInstant(), end.toInstant(),
            RANGE_X_MIN, RANGE_Y_MIN, RANGE_X_MAX, RANGE_Y_MAX,
            asList(tasks),
            Collections.singletonList(V2MaintenanceTrackingRepository.STATE_ROADS_DOMAIN));
    }

    private void assertAllHasOnlyPointGeometries(final List<MaintenanceTrackingLatestFeature> features) {
        features.forEach(f -> assertEquals(Point, f.getGeometry().getType()));
    }

    private long insertTrackingForDomain(final String domain, final long workMachineId) {
        entityManager.flush();
        entityManager.createNativeQuery(
                "INSERT INTO maintenance_tracking(id, domain, last_point, work_machine_id, sending_system, sending_time, start_time, end_time, finished)\n" +
                         "VALUES (nextval('SEQ_MAINTENANCE_TRACKING'), '" + domain + "', ST_PointFromText('POINT(-71.064544 42.28787 0)', 4326), " +
                                  workMachineId + ", 'dummy', now(), now(), now(), true)" )
            .executeUpdate();
        final long id = ((BigInteger) entityManager.createNativeQuery(
            "select id " +
                "from road.public.maintenance_tracking " +
                "where domain = '" + domain + "' " +
                "order by id desc " +
                "limit 1").getSingleResult()).longValue();
        entityManager.createNativeQuery(
                "INSERT INTO road.public.maintenance_tracking_task(maintenance_tracking_id, task)\n" +
                    "VALUES (" + id + ", '" + BRUSHING.name() + "')")
            .executeUpdate();
        return id;
    }
}
