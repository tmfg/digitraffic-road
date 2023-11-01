package fi.livi.digitraffic.tie.service.maintenance.v1;

import static fi.livi.digitraffic.tie.TestUtils.commitAndEndTransactionAndStartNew;
import static fi.livi.digitraffic.tie.TestUtils.flushCommitEndTransactionAndStartNew;
import static fi.livi.digitraffic.tie.TestUtils.getRandom;
import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingDao.STATE_ROADS_DOMAIN;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.AURAUS_JA_SOHJONPOISTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_JUOTOSTYOT;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.metadata.geojson.Geometry.Type.Point;
import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask.CRACK_FILLING;
import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask.PAVING;
import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask.PLOUGHING_AND_SLUSH_REMOVAL;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.RANGE_X_MAX;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.RANGE_X_MIN;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.RANGE_Y_MAX;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.RANGE_Y_MIN;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createMaintenanceTrackingWithLineString;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createVerticalLineStringWGS84;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createWorkMachines;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createWorkmachine;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getEndTime;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getStartTimeOneHourInPast;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getTaskSetWithIndex;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getTaskWithIndex;
import static java.util.Arrays.asList;
import static org.apache.sshd.common.util.GenericUtils.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.sshd.common.util.GenericUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureCollectionV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingLatestFeatureV1;
import fi.livi.digitraffic.tie.dto.maintenance.v1.MaintenanceTrackingPropertiesV1;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.helper.ThreadUtils;
import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

public class MaintenanceTrackingWebDataServiceV1Test extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingWebDataServiceV1Test.class);
    final static Pair<Double, Double> BOUNDING_BOX_X_RANGE = Pair.of(20.0, 30.0);
    final static Pair<Double, Double> BOUNDING_BOX_Y_RANGE = Pair.of(64.0, 66.0);
    final static Pair<Double, Double> BOUNDING_BOX_CENTER = Pair.of(25.0, 65.0);
    final static Polygon AREA = MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_X_RANGE.getRight(), BOUNDING_BOX_Y_RANGE.getRight());
    private final String DOMAIN_WITH_SOURCE = "domain-with-source";
    private final String DOMAIN_WITHOUT_SOURCE = "domain-without-source";
    private final DecimalFormat f = new DecimalFormat("#0.00");

    @SpyBean
    private MaintenanceTrackingWebDataServiceV1 maintenanceTrackingWebDataServiceV1;

    @Autowired
    private MaintenanceTrackingServiceTestHelperV1 testHelper;

    @Autowired
    private MaintenanceTrackingRepository maintenanceTrackingRepository;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    @BeforeEach
    public void init() {
        testHelper.clearDb();
        testHelper.deleteDomains(DOMAIN_WITH_SOURCE, DOMAIN_WITHOUT_SOURCE);
        entityManager.flush();
        entityManager.clear();
        commitAndEndTransactionAndStartNew();
        maintenanceTrackingWebDataServiceV1.evictRoutesCache();
        maintenanceTrackingWebDataServiceV1.evictRoutesLatestCache();
    }

    @Test
    public void findTrackingsWithCreationTime() throws JsonProcessingException {
        final Instant start = getStartTimeOneHourInPast();
        final Instant created = start.plus(30, ChronoUnit.MINUTES);
        final List<Tyokone> workMachines1 = createWorkMachines(1);
        final List<Tyokone> workMachines2 = createWorkMachines(1);
        // startTime == endTime == start
        final TyokoneenseurannanKirjausRequestSchema seuranta1 =
            createMaintenanceTrackingWithLineString(start, 10, 1, 1, workMachines1, ASFALTOINTI);
        // startTime == endTime == created
        final TyokoneenseurannanKirjausRequestSchema seuranta2 =
            createMaintenanceTrackingWithLineString(created, 10, 2,1, workMachines2, ASFALTOINTI);

        testHelper.saveTrackingDataAsObservations(seuranta1);
        testHelper.saveTrackingDataAsObservations(seuranta2);

        testHelper.handleUnhandledWorkMachineObservations(1000);
        entityManager.flush();
        entityManager.clear();
        entityManager.createNativeQuery("update maintenance_tracking set created = :created where domain=:domain")
            .setParameter("created", created)
            .setParameter("domain", STATE_ROADS_DOMAIN)
            .executeUpdate();

        final List<MaintenanceTracking> all = maintenanceTrackingRepository.findAll(Sort.by("endTime"));
        assertCollectionSize(2, all);

        final MaintenanceTracking first = all.get(0);
        final MaintenanceTracking second = all.get(1);

        // start is exlusive -> nothing to return
        assertCollectionSize(0, findMaintenanceTrackings(
            null, null,
            created, created.plus(1, ChronoUnit.MINUTES), GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures());

        // end is exclusive -> nothing to return
        assertCollectionSize(0, findMaintenanceTrackings(
            null, null,
            created.minus(1, ChronoUnit.MINUTES), created, GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures());

        // Both are created at the same time -> both are returned
        assertCollectionSize(2, findMaintenanceTrackings(
            null, null,
            created.minusSeconds(1), created.plus(1, ChronoUnit.MINUTES), GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures());

        // Created match both and endTime (exlusive) only first
        final List<MaintenanceTrackingFeatureV1> firstFound = findMaintenanceTrackings(
            start, start.plusMillis(1),
            created.minusSeconds(1), created.plus(1, ChronoUnit.MINUTES), GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures();
        assertCollectionSize(1, firstFound);
        assertEquals(first.getId(), firstFound.get(0).getProperties().id);

        // Created match both and endTime only first
        final List<MaintenanceTrackingFeatureV1> secondFound = findMaintenanceTrackings(
            created, created.plusMillis(1),
            created.minusSeconds(1), created.plus(1, ChronoUnit.MINUTES), GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures();
        assertCollectionSize(1, secondFound);
        assertEquals(second.getId(), secondFound.get(0).getProperties().id);
    }

    @Test
    public void findCombinedTrackingsWithMultipleWorkMachines() throws JsonProcessingException {
        final Instant start = getStartTimeOneHourInPast();
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final TyokoneenseurannanKirjausRequestSchema seuranta1 =
            createMaintenanceTrackingWithLineString(start, 10, 1, 1, workMachines, ASFALTOINTI);
        final TyokoneenseurannanKirjausRequestSchema seuranta2 =
            createMaintenanceTrackingWithLineString(start.plus(5, ChronoUnit.MINUTES), 10, 2,1, workMachines, ASFALTOINTI);
        final Instant end = getEndTime(seuranta2);

        testHelper.saveTrackingDataAsObservations(seuranta1);
        testHelper.saveTrackingDataAsObservations(seuranta2);
        testHelper.handleUnhandledWorkMachineObservations(1000);

        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(start, end, PAVING).getFeatures();
        // Trackings should be grouped/machine
        final LinkedHashMap<Long, List<MaintenanceTrackingPropertiesV1>> grouped = groupTrackingsByStartId(features);
        assertEquals(machineCount, grouped.size());

        grouped.forEach((id, properties) -> {
            // Each group (machine) should have 2 combined trackings
            assertEquals(2, properties.size());
            final MaintenanceTrackingPropertiesV1 first = properties.get(0);
            final MaintenanceTrackingPropertiesV1 second = properties.get(1);
            assertEquals(start, first.startTime);
            assertEquals(end, second.endTime);
        });
    }

    @Test
    public void findCombinedMultipleTrackings() {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final Instant startTime = getStartTimeOneHourInPast();

        // Generate 5 messages/machine with 10 coordinates in each
        final Instant endTime = IntStream.range(0, 5).mapToObj(i -> {
            final Instant start = startTime.plus(i* 10L, ChronoUnit.MINUTES);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithLineString(start, 10, i + 1, 1, workMachines, ASFALTOINTI, PAALLYSTEIDEN_JUOTOSTYOT);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return getEndTime(seuranta);
        }).max(Instant::compareTo).orElseThrow();
        final int expectedObservationCount = 5 * machineCount; // 5 messages/machine
        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(expectedObservationCount, handled);

        final List<MaintenanceTrackingLatestFeatureV1> latestFeatures = findLatestMaintenanceTrackings(startTime, endTime).getFeatures();

        assertCollectionSize(machineCount, latestFeatures); // One tracking/machine
        assertAllHasOnlyPointGeometries(latestFeatures);

        // All messages are combined by having previous tracking link
        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(startTime, endTime).getFeatures();
        assertCollectionSize(machineCount*5, features); // 5 tracking/machine

        // Check features properties
        features.forEach(t -> {
            // As observations are simplified, we can't know how many points is left in geometry
            // But start and end are preserved if they are not same points
            assertTrue(t.getGeometry().getCoordinates().size() > 1);
        });
    }

    @Test
    public void findTrackingWithDifferentTasks() {
        final int machineCount = getRandomId(2, 10);
        // Work machines with harja id 1,2,...(machineCount+1)
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final Instant startTime = getStartTimeOneHourInPast();

        // Generate 5 different messages with different tasks for each machine
        // Each machine successive trackings will bombine next tracking first point as previous tracking last point
        IntStream.range(0, 5).forEach(idx -> {
            final Instant start = startTime.plus(idx*10L, ChronoUnit.MINUTES);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithLineString(start, 10, 1, workMachines, SuoritettavatTehtavat.values()[idx]);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // Without task parameter all should be found
        List<MaintenanceTrackingFeatureV1> allFeatures =
            findMaintenanceTrackingsInclusiveEnd(startTime, startTime.plus(10 * 5 + 9, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(machineCount*5, allFeatures);

        // Latest should return latest one per machine
        List<MaintenanceTrackingLatestFeatureV1> latestFeatures =
            findLatestMaintenanceTrackings(startTime, startTime.plus(10 * 5 + 9, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(machineCount, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);

        // Find with tasks
        IntStream.range(0, 5).forEach(idx -> {
            final Instant endTime = startTime.plus(idx*10L+10L, ChronoUnit.MINUTES); // 10 observations end time is 10 min after first
            final List<MaintenanceTrackingFeatureV1> features =
                findMaintenanceTrackingsInclusiveEnd(startTime, endTime, getTaskWithIndex(idx)).getFeatures();
            assertCollectionSize(machineCount, features);
            features.forEach(f -> assertEquals(getTaskSetWithIndex(idx), f.getProperties().tasks));
        });
    }

    @Test
    public void findTrackingWithDifferentJobsForSameMachine() throws JsonProcessingException {
        // Work machines with harja id 1
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneHourInPast();
        // tracking with job 1
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // tracking with job 2
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime.plus(10, ChronoUnit.MINUTES), 10, 2, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // 2 tracking should be made as they have different jobs
        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(startTime, startTime.plus(10+10, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(2, features);

        // 2 tracking should be found as latest as same machine with different job id is handled as different machine
        final List<MaintenanceTrackingLatestFeatureV1> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plus(10+11, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(2, latestFeatures);
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void findSeparateTrackingsWhenTransitionInBetweenTasks() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneHourInPast();
        // tracking with job 1
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime.plus(1, ChronoUnit.MINUTES), 10, 1, workMachines));
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime.plus(2, ChronoUnit.MINUTES), 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        // 2 tracking should be made as they have different jobs
        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(startTime, startTime.plus(10+10+10, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(2, features);

        // Only the latest one should be found
        final List<MaintenanceTrackingLatestFeatureV1> latestFeatures = findLatestMaintenanceTrackings(startTime, startTime.plus(10+10+10, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(1, latestFeatures);
        assertEquals(startTime.plus(2, ChronoUnit.MINUTES), latestFeatures.get(0).getProperties().getTime());
        assertAllHasOnlyPointGeometries(latestFeatures);
    }

    @Test
    public void observationsTimesCrossesBetweenMessagesThenTrackingsAreCombinedToGroups() throws JsonProcessingException {
        // M1 = Message 1, P1=Point 1. Then observation times are:
        // M1(P1) < M2(P1) < M1(P2) < M2(P2) < M1(P3) < M2(P3) ...
        final List<Tyokone> workMachines = createWorkMachines(getRandom(1,5));
        final Instant startTime = getStartTimeOneHourInPast();
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
        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(startTime, startTime.plus(1+9, ChronoUnit.MINUTES)).getFeatures();
        final LinkedHashMap<Long, List<MaintenanceTrackingPropertiesV1>> groupsByStartId = groupTrackingsByStartId(features);
        // There is as many groups of trackings as there is machines
        assertEquals(workMachines.size(), groupsByStartId.size());
    }

    @Test
    public void findTrackingDataJsonsByTrackingId() throws IOException {
        final List<Tyokone> workMachines = createWorkMachines(getRandom(1,5));
        final Instant startTime = getStartTimeOneHourInPast();
        // Create 2 messages that are combined as one tracking
        // 10 observations
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // 10 observations
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithLineString(startTime.plus(10, ChronoUnit.MINUTES), 10, 2, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.handleUnhandledWorkMachineObservations(1000);

        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(startTime, startTime.plus(20, ChronoUnit.MINUTES)).getFeatures();
        assertCollectionSize(workMachines.size() * 2, features);
        // Observation data count is == tracking count
        final List<JsonNode> jsons = maintenanceTrackingWebDataServiceV1.findTrackingDataJsonsByTrackingId(features.get(0).getProperties().id);
        assertCollectionSize(1, jsons);
    }

    @Test
    public void findWithMultipleTasks() throws JsonProcessingException {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final Instant startTime = getStartTimeOneHourInPast();
        final Instant endTime = startTime.plus(9, ChronoUnit.MINUTES);
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
        final List<MaintenanceTrackingFeatureV1> features = findMaintenanceTrackingsInclusiveEnd(startTime, endTime, CRACK_FILLING, PAVING).getFeatures();
        assertCollectionSize(machineCount*2, features);

        // Check that each features tasks are AURAUS_JA_SOHJONPOISTO and PAALLYSTEIDEN_JUOTOSTYOT or ASFALTOINTI
        features.forEach(f -> {
            final Set<MaintenanceTrackingTask> tasks = f.getProperties().tasks;
            if (tasks.size() == 2) {
                assertEquals(new HashSet<>(asList(PLOUGHING_AND_SLUSH_REMOVAL, CRACK_FILLING)), tasks);
            } else {
                assertEquals(new HashSet<>(GenericUtils.asSet(PAVING)), tasks);
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
        final Instant startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_CENTER.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft()-0.5, BOUNDING_BOX_Y_RANGE.getRight() + 0.5);

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            MaintenanceTrackingServiceTestHelperV1.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollectionV1 result = maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            startTime, startTime.plusMillis(1),
            null, null,
            AREA,
            Collections.emptySet(),
            GenericUtils.asSet(STATE_ROADS_DOMAIN));
        assertEquals(1, result.getFeatures().size());
        final MaintenanceTrackingPropertiesV1 props = result.getFeatures().get(0).getProperties();

        assertEquals(startTime, props.startTime);
        assertEquals(startTime, props.endTime);
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
        final Instant startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_X_RANGE.getRight() + 0.1,
                                                                     BOUNDING_BOX_Y_RANGE.getLeft() - 10,
                                                                     BOUNDING_BOX_Y_RANGE.getRight() + 10);
        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            MaintenanceTrackingServiceTestHelperV1.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollectionV1 result = maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            startTime, startTime,
            null, null, AREA,
            Collections.emptySet(),
            GenericUtils.asSet(STATE_ROADS_DOMAIN));
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
        final Instant startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = List.of(
                asList(BOUNDING_BOX_CENTER.getLeft(), BOUNDING_BOX_CENTER.getRight())
        );

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            MaintenanceTrackingServiceTestHelperV1.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollectionV1 result = maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            startTime, startTime.plusMillis(1),
            null, null,
            AREA,
            Collections.emptySet(),
            GenericUtils.asSet(STATE_ROADS_DOMAIN));
        assertEquals(1, result.getFeatures().size());
        final MaintenanceTrackingPropertiesV1 props = result.getFeatures().get(0).getProperties();

        assertEquals(startTime, props.startTime);
        assertEquals(startTime, props.endTime);
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
        final Instant startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = List.of(
                asList(BOUNDING_BOX_X_RANGE.getRight() + 0.1, BOUNDING_BOX_CENTER.getRight())
        );

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            MaintenanceTrackingServiceTestHelperV1.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollectionV1 result = maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            startTime, startTime,
            null, null,
            AREA,
            Collections.emptySet(),
            GenericUtils.asSet(STATE_ROADS_DOMAIN));
        assertEquals(0, result.getFeatures().size());
    }

    @Test
    public void getById() throws JsonProcessingException {
        final Tyokone workMachine = createWorkmachine(1);
        final Instant startTime = getStartTimeOneHourInPast();

        List<List<Double>> fromWGS84 = createVerticalLineStringWGS84(BOUNDING_BOX_X_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getLeft(), BOUNDING_BOX_Y_RANGE.getRight());

        final List<List<Double>> fromETRS89 = CoordinateConverter.convertLineStringCoordinatesFromWGS84ToETRS89(fromWGS84);

        testHelper.saveTrackingDataAsObservations(
            MaintenanceTrackingServiceTestHelperV1.createMaintenanceTracking(startTime, 1, workMachine, fromETRS89, AURAUS_JA_SOHJONPOISTO));

        final int handled = testHelper.handleUnhandledWorkMachineObservations(1000);
        assertEquals(1, handled);

        final MaintenanceTrackingFeatureCollectionV1 result1 = maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            startTime, startTime.plusMillis(1),
            null, null,
            AREA,
            Collections.emptySet(),
            GenericUtils.asSet(STATE_ROADS_DOMAIN));
        final MaintenanceTrackingFeatureV1 feature1 = result1.getFeatures().get(0);

        final MaintenanceTrackingFeatureV1 feature2 =
            maintenanceTrackingWebDataServiceV1.getMaintenanceTrackingById(result1.getFeatures().get(0).getProperties().id);

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
        final long trackingId1 = testHelper.insertTrackingForDomain(DOMAIN_WITH_SOURCE, wm1.getId());
        final long trackingId2 = testHelper.insertTrackingForDomain(DOMAIN_WITHOUT_SOURCE, wm2.getId());
        flushCommitEndTransactionAndStartNew(entityManager);

        // Tracking for domain with source should be found
        assertNotNull(maintenanceTrackingWebDataServiceV1.getMaintenanceTrackingById(trackingId1));
        assertThrows(ObjectNotFoundException.class, () -> {
            // Tracking for domain without source should not be found
            maintenanceTrackingWebDataServiceV1.getMaintenanceTrackingById(trackingId2);
        });
    }

    @Test
    public void latestUpdateTime() throws JsonProcessingException, InterruptedException {

        // Create 2 trackings with 1 s handling time gap -> creation time diff is 1 s
        final List<Tyokone> workMachines1 = createWorkMachines(1);
        final List<Tyokone> workMachines2 = createWorkMachines(1);

        final TyokoneenseurannanKirjausRequestSchema seuranta1 =
            createMaintenanceTrackingWithLineString(getStartTimeOneHourInPast(), 10, 1, 1, workMachines1, ASFALTOINTI);
        final TyokoneenseurannanKirjausRequestSchema seuranta2 =
            createMaintenanceTrackingWithLineString(getStartTimeOneHourInPast(), 10, 1,1, workMachines2, AURAUS_JA_SOHJONPOISTO);

        // created time = transaction start time
        testHelper.saveTrackingDataAsObservations(seuranta1);
        testHelper.handleUnhandledWorkMachineObservations(1000);
        // created time = transaction start time
        // +1s to next creation time
        ThreadUtils.delayMs(1000);
        flushCommitEndTransactionAndStartNew(entityManager);
        testHelper.saveTrackingDataAsObservations(seuranta2);
        testHelper.handleUnhandledWorkMachineObservations(1000);

        flushCommitEndTransactionAndStartNew(entityManager);

        // Get trackings and check second is created after first
        // and last update in data is same as created time of second
        final MaintenanceTrackingFeatureCollectionV1 fc = findMaintenanceTrackings(
            null, null,
            null, null, GenericUtils.asSet(STATE_ROADS_DOMAIN));
        assertCollectionSize(2, fc.getFeatures());
        final Instant lastUpdated = fc.getLastModified();
        final MaintenanceTrackingFeatureV1 first = fc.getFeatures().get(0);
        final MaintenanceTrackingFeatureV1 second = fc.getFeatures().get(1);
        assertTrue(first.getProperties().created.isBefore(second.getProperties().created));
        assertEquals(lastUpdated, second.getProperties().created);
    }

    @Test
    public void latestUpdateTimeWithDomain() throws InterruptedException {
        // Create one tracking for both domains with 2 s diff between creation times
        final MaintenanceTrackingWorkMachine wm1 = testHelper.createAndSaveWorkMachine();
        final MaintenanceTrackingWorkMachine wm2 = testHelper.createAndSaveWorkMachine();
        final String firstDomain = DOMAIN_WITH_SOURCE;
        final String secondDomain = DOMAIN_WITHOUT_SOURCE;
        testHelper.insertDomain(firstDomain, "Foo/Bar");
        testHelper.insertDomain(secondDomain, "Foo/Bar");

        // tracking for firstDomain
        testHelper.insertTrackingForDomain(firstDomain, wm1.getId());
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA_CHECKED, firstDomain);
        ThreadUtils.delayMs(2000); // delay creation with 2 s
        // tracking for secondDomain 2 s later
        flushCommitEndTransactionAndStartNew(entityManager);
        testHelper.insertTrackingForDomain(secondDomain, wm2.getId());
        dataStatusService.updateDataUpdated(DataType.MAINTENANCE_TRACKING_DATA_CHECKED, secondDomain);
        flushCommitEndTransactionAndStartNew(entityManager);

        final List<MaintenanceTracking> all = maintenanceTrackingRepository.findAll(Sort.by("created"));
        assertCollectionSize(2, all);
        final MaintenanceTracking first = all.get(0);
        final MaintenanceTracking second = all.get(1);
        assertEquals(firstDomain, first.getDomain());
        assertEquals(secondDomain, second.getDomain());
        assertNotEquals(first.getCreated(), second.getCreated());

        // find with first domain should have same update time as creation time and almost same the same data checked time
        final MaintenanceTrackingFeatureCollectionV1 fc1 = findMaintenanceTrackings(
            null, null,
            null, null, GenericUtils.asSet(firstDomain));
        assertCollectionSize(1, fc1.getFeatures());
        assertEquals(first.getCreated().toInstant(), fc1.getLastModified());

        // find with second domain should have same update time as creation time and almost same the same data checked time
        final MaintenanceTrackingFeatureCollectionV1 fc2 = findMaintenanceTrackings(
            null, null,
            null, null, asSet(secondDomain));
        assertCollectionSize(1, fc2.getFeatures());
        assertEquals(second.getCreated().toInstant(), fc2.getLastModified());

        // With both domains, the result has the newest creation time (=second domain)
        final MaintenanceTrackingFeatureCollectionV1 fcBoth = findMaintenanceTrackings(
            null, null,
            null, null, asSet(secondDomain, firstDomain));
        assertCollectionSize(2, fcBoth.getFeatures());
        assertEquals(second.getCreated().toInstant(), fcBoth.getLastModified());
    }

    @Test
    public void findRoutesIsCached() {
        final Instant start = Instant.now().minus(3, ChronoUnit.MINUTES);
        final List<Tyokone> workmachines = createWorkMachines(3);
        IntStream.range(0,3).forEach(i -> { // Create 3 routes total
            log.info("Create tracking {}.", i);
            final TyokoneenseurannanKirjausRequestSchema seuranta1 =
                createMaintenanceTrackingWithLineString(start.plusNanos(i * 1000L), 10, 1, 1,
                                                        Collections.singletonList(workmachines.get(i)), ASFALTOINTI);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta1);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            testHelper.handleUnhandledWorkMachineObservations(1000);
            entityManager.flush();
            entityManager.clear();
            // First call should go to service and be cached.
            // Second and third call should come from cache and have same values
            assertCollectionSize(
                1,
                findMaintenanceTrackings(null, null, null, null, GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures()
            );
            assertCollectionSize(
                1,
                findLatestMaintenanceTrackings(null, null).getFeatures()
            );

        });

        // Verify cached methods are called only once
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findMaintenanceTrackingRoutes(
            null, null, null, null, null, Collections.emptySet(), GenericUtils.asSet(STATE_ROADS_DOMAIN)
        );
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findLatestMaintenanceTrackingRoutes(
            null, null, null, Collections.emptySet(), GenericUtils.asSet(STATE_ROADS_DOMAIN)
        );

        maintenanceTrackingWebDataServiceV1.evictRoutesCache();
        maintenanceTrackingWebDataServiceV1.evictRoutesLatestCache();
        assertCollectionSize(
            3,
            findMaintenanceTrackings(null, null, null, null, GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures()
        );
        assertCollectionSize(
            3,
            findLatestMaintenanceTrackings(null, null).getFeatures()
        );
        logCache();
    }

    @Test
    public void findRoutesIsCachedAlsoInParallelCalls() throws JsonProcessingException, InterruptedException {
        final Instant start = Instant.now().minus(3, ChronoUnit.MINUTES);
        final TyokoneenseurannanKirjausRequestSchema seuranta =
            createMaintenanceTrackingWithLineString(start, 10, 1, 1,
                                                    createWorkMachines(1), ASFALTOINTI);
        testHelper.saveTrackingDataAsObservations(seuranta);
        testHelper.handleUnhandledWorkMachineObservations(1000);
        entityManager.flush();
        entityManager.clear();

        final int numberOfThreads = 10;
        final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);

        assertCollectionSize(
            1,
            findMaintenanceTrackings(null, null, null, null, GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures()
        );
        assertCollectionSize(
            1,
            findLatestMaintenanceTrackings(null, null).getFeatures()
        );

        final AtomicInteger a = new AtomicInteger();
        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                final int current = a.getAndIncrement();
                log.info("Thread {} start", current);
                assertCollectionSize(
                    1,
                    findMaintenanceTrackings(null, null, null, null, GenericUtils.asSet(STATE_ROADS_DOMAIN)).getFeatures()
                );
                assertCollectionSize(
                    1,
                    findLatestMaintenanceTrackings(null, null).getFeatures()
                );
                log.info("Thread {} end", current);
                latch.countDown();
            });
        }
        latch.await();
        // Verify cached methods are called only once
        // Only one thread should go in and other should come from cache
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findMaintenanceTrackingRoutes(
            null, null, null, null, null, Collections.emptySet(), GenericUtils.asSet(STATE_ROADS_DOMAIN)
        );
        verify(maintenanceTrackingWebDataServiceV1, times(1)).findLatestMaintenanceTrackingRoutes(
            null, null, null, Collections.emptySet(), GenericUtils.asSet(STATE_ROADS_DOMAIN)
        );
        logCache();
    }

    @Test
    public void convertToNormalizedAreaParameterWholeAreaToNull() {
        // Whole area is converted to null
        assertNull(MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(RANGE_X_MIN, RANGE_X_MAX, RANGE_Y_MIN, RANGE_Y_MAX));
    }

    // X = 19.0 - 32.0
    // Y = 59.0 - 72.0;
    @Test
    public void convertToNormalizedAreaParameterFromLowerLimits() {
        final Polygon polygon = MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(17.1, 30.1, 61.51, 70.1);
        final double xMin = 17.0;
        final double xMax = 31.0;
        final double yMin = 61.5;
        final double yMax = 70.5;
        assertCoords(xMin, xMax, yMin, yMax, polygon);
    }

    @Test
    public void convertToNormalizedAreaParameterFromUpperLimits() {
        final Polygon polygon = MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(17.9, 30.9, 61.99, 70.99);
        final double xMin = 17.0;
        final double xMax = 31.0;
        final double yMin = 61.5;
        final double yMax = 71.0;
        assertCoords(xMin, xMax, yMin, yMax, polygon);
    }

    @Test
    public void convertToNormalizedAreaParameterNoChange() {
        final Polygon polygon = MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(17.0, 31.0, 61.5, 70.0);
        final double xMin = 17.0;
        final double xMax = 31.0;
        final double yMin = 61.5;
        final double yMax = 70.0;
        assertCoords(xMin, xMax, yMin, yMax, polygon);
    }

    private void assertCoords(final double xMin, final double xMax, final double yMin, final double yMax, final Polygon polygon) {
        final Coordinate[] coords = polygon.getCoordinates();
        assertEquals(5, coords.length, "Coordinates length should be 5 but was " + coords.length);
        assertEquals(xMin, coords[0].getX(), "xMin didn't match");
        assertEquals(yMin, coords[0].getY(), "yMin didn't match");
        assertEquals(xMin, coords[1].getX(), "xMin didn't match");
        assertEquals(yMax, coords[1].getY(), "yMax didn't match");
        assertEquals(xMax, coords[2].getX(), "xMax didn't match");
        assertEquals(yMax, coords[2].getY(), "yMax didn't match");
        assertEquals(xMax, coords[3].getX(), "xMax didn't match");
        assertEquals(yMin, coords[3].getY(), "yMin didn't match");
        assertEquals(xMin, coords[4].getX(), "xMin didn't match");
        assertEquals(yMin, coords[4].getY(), "yMin didn't match");
    }

    private void logCache() {
        cacheManager.getCacheNames().forEach(cn -> {
            final CacheStats s = ((CaffeineCache) Objects.requireNonNull(cacheManager.getCache(cn))).getNativeCache().stats();
            log.info("method=cacheStats cacheName={} hitCount={} missCount={} hitRate={} missRate={} evictionCount={} averageLoadPenaltyMs={}",
                    cn, s.hitCount(), s.missCount(), f.format(s.hitRate()), f.format(s.missRate()), s.evictionCount(), (long)s.averageLoadPenalty()/1000000); // ns -> ms
        });

    }

    private MaintenanceTrackingLatestFeatureCollectionV1 findLatestMaintenanceTrackings(final Instant start, final Instant end,
                                                                                        final MaintenanceTrackingTask...tasks) {
        return maintenanceTrackingWebDataServiceV1.findLatestMaintenanceTrackingRoutes(
            start, end,
            MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(RANGE_X_MIN, RANGE_X_MAX, RANGE_Y_MIN, RANGE_Y_MAX),
            asSet(tasks),
            GenericUtils.asSet(STATE_ROADS_DOMAIN));
    }

    private void assertAllHasOnlyPointGeometries(final List<MaintenanceTrackingLatestFeatureV1> features) {
        features.forEach(f -> assertEquals(Point, f.getGeometry().getType()));
    }

    private MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackingsInclusiveEnd(final Instant endFrom, final Instant endTo,
                                                                                        final MaintenanceTrackingTask...tasks) {
        return findMaintenanceTrackings(
            endFrom, endTo.plusMillis(1),
            null, null,
            GenericUtils.asSet(STATE_ROADS_DOMAIN),
            tasks);
    }

    private MaintenanceTrackingFeatureCollectionV1 findMaintenanceTrackings(final Instant endFrom, final Instant endBefore,
                                                                            final Instant changeAfter, final Instant changeBefore,
                                                                            final Set<String> domains,
                                                                            final MaintenanceTrackingTask...tasks) {
        return maintenanceTrackingWebDataServiceV1.findMaintenanceTrackingRoutes(
            endFrom, endBefore, changeAfter, changeBefore,
            MaintenanceTrackingWebDataServiceV1.convertToNormalizedAreaParameter(RANGE_X_MIN, RANGE_X_MAX, RANGE_Y_MIN, RANGE_Y_MAX),
            asSet(tasks),
            domains);
    }

    private LinkedHashMap<Long, List<MaintenanceTrackingPropertiesV1>> groupTrackingsByStartId(final List<MaintenanceTrackingFeatureV1> trackings) {
        Map<Long, MaintenanceTrackingPropertiesV1> idToTrackingMap =
            trackings.stream().collect(Collectors.toMap(f -> f.getProperties().id, Feature::getProperties));
        LinkedHashMap<Long, List<MaintenanceTrackingPropertiesV1>> groupsByStartId = new LinkedHashMap<>();
        trackings.stream()
            .map(MaintenanceTrackingFeatureV1::getProperties)
            .forEach(p -> {
            // This is the first one to handle
            if (p.previousId == null) {
                groupsByStartId.put(p.id, new ArrayList<>(Collections.singleton(p)));
            } else {
                // Find the first in group and add tracking to it's group
                MaintenanceTrackingPropertiesV1 previous = p;
                while (previous.previousId != null) {
                    previous = idToTrackingMap.get(previous.previousId);
                }
                groupsByStartId.get(previous.id).add(p);
            }
        });
        return groupsByStartId;
    }
}
