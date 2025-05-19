package fi.livi.digitraffic.tie.service.maintenance;

import static fi.livi.digitraffic.test.util.AssertUtil.assertCollectionSize;
import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.ASFALTOINTI;
import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingObservationData.Status.HANDLED;
import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingObservationData.Status.UNHANDLED;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.createWorkMachines;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getEndTime;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getStartTimeOneDayInPast;
import static fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1.getTaskSetWithIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingObservationData;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.maintenance.v1.MaintenanceTrackingServiceTestHelperV1;

public class MaintenanceTrackingUpdateServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingUpdateServiceTest.class);

    @Autowired
    private MaintenanceTrackingObservationDataRepository maintenanceTrackingObservationDataRepository;

    @Autowired
    private MaintenanceTrackingRepository maintenanceTrackingRepository;

    @Autowired
    private MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1;

    @Autowired
    private MaintenanceTrackingServiceTestHelperV1 testHelper;

    @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
    private int maxGapInMinutes;

    @BeforeEach
    public void init() {
        testHelper.clearDb();
    }

    @Test
    public void isAllTaskTypesMapped() {
        // Check mapping from DT -> HARJA
        for (final MaintenanceTrackingTask value : MaintenanceTrackingTask.values()) {
            if (!value.equals(MaintenanceTrackingTask.UNKNOWN)) {
                assertNotNull(SuoritettavatTehtavatSchema.valueOf(value.getHarjaEnumName()),
                        StringUtil.format("{} with harjaEnumName {} not mapped to correct {}",
                                MaintenanceTrackingTask.class.getSimpleName(), value.getHarjaEnumName(),
                                SuoritettavatTehtavatSchema.class.getSimpleName()));
            }
        }

        // Check mapping from HARJA -> DT
        for (final SuoritettavatTehtavatSchema value : SuoritettavatTehtavatSchema.values()) {
            assertNotNull(MaintenanceTrackingTask.getByharjaEnumName(value.name()),
                    StringUtil.format("{}.getByharjaEnumName( {} ) returned null",
                            MaintenanceTrackingTask.class.getSimpleName(), value.name()));
            assertNotEquals(MaintenanceTrackingTask.UNKNOWN, MaintenanceTrackingTask.getByharjaEnumName(value.name()),
                    StringUtil.format("Harja type {}.{} not mapped in {} not mapped",
                            SuoritettavatTehtavatSchema.class.getSimpleName(), value.name(),
                            MaintenanceTrackingTask.class.getSimpleName()));
        }

        // Check mapping from MaintenanceTrackingTask -> DB maintenance_task_enum
        final List<?> taskEnumsInDb =
                entityManager.createNativeQuery("SELECT unnest(enum_range(NULL::maintenance_task_enum))", String.class).getResultList();

        final List<String> missingValuesInDb =
                Arrays.stream(MaintenanceTrackingTask.values())
                        .map(Enum::name)
                        .filter(name -> !taskEnumsInDb.contains(name))
                        .toList();
                new ArrayList<String>();

        assertTrue(missingValuesInDb.isEmpty(),
                StringUtil.format("Missing {} MaintenanceTrackingTask values in db. Create them with SQL:\n{}\n",
                        missingValuesInDb.size(), createEnumsSql(missingValuesInDb)));
    }

    private String createEnumsSql(final List<String> missingValuesInDb) {
        return missingValuesInDb.stream()
                .map(enumName -> StringUtil.format("ALTER TYPE maintenance_task_enum ADD VALUE '{}';", enumName))
                .collect(Collectors.joining("\n"));
    }

    @Test
    public void readUnhandedObservationDataWithMultipleWorkMachinesMatches() throws IOException {
        final Instant now = getStartTimeOneDayInPast();
        final int machineCount = getRandomId(2, 10);
        final int observationCount = 10;
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        // Create maintenance tracking message for <machineCount> machines and <observationCount> observations for each machine
        // and save observations to db
        final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(now, 10, 1, workMachines, ASFALTOINTI);
        testHelper.saveTrackingDataAsObservations(seuranta);

        // Check that data can be fetched from db
        final List<MaintenanceTrackingObservationData> unhandled =
                maintenanceTrackingObservationDataRepository.findUnhandled(100, 0).toList();
        assertEquals(observationCount * machineCount, unhandled.size());

        // Check that unhanded are in ascending order by observation time
        final AtomicReference<Instant> prevObservationTime = new AtomicReference<>(Instant.MIN);
        unhandled.forEach(o -> {
            assertEquals(UNHANDLED, o.getStatus());
            final Instant observationTime = o.getObservationTime();
            log.debug("observationTime={}", observationTime);
            assertTrue(observationTime.equals(prevObservationTime.get()) ||
                    observationTime.isAfter(prevObservationTime.get()));
            prevObservationTime.set(observationTime);
        });

        // Check that all machines has right count of observations
        IntStream.range(0, machineCount)
                .forEach(i -> assertEquals(10, unhandled.stream()
                        .filter(observation -> observation.getHarjaWorkmachineId()
                                .equals(workMachines.get(i).getId().longValue())).count()));

        // Check that saved observations has valid observation json
        seuranta.getHavainnot().forEach(havainto -> {
            try {
                final Havainto h = havainto.getHavainto();
                final String formatedJson = testHelper.getFormatedObservationJson(h);
                final MaintenanceTrackingObservationData observation =
                    unhandled.stream()
                        .filter(o -> o.getHarjaWorkmachineId().equals(h.getTyokone().getId().longValue()) &&
                                     o.getObservationTime().equals(h.getHavaintoaika()))
                        .findFirst().orElseThrow();
                assertEquals(formatedJson, observation.getJson());
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void handleTrackingWithMultipleWorkMachines() throws JsonProcessingException {
        // Create maintenance tracking message for <machineCount> machines and <observationCount> observations for each machine
        // and save observations to db
        final Instant now = getStartTimeOneDayInPast();
        final int machineCount = getRandomId(2, 10);
        final int observationCount = 10;
        final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(now, observationCount, 1, machineCount, ASFALTOINTI);
        testHelper.saveTrackingDataAsObservations(seuranta);

        final List<MaintenanceTrackingObservationData> unhandled =
                maintenanceTrackingObservationDataRepository.findUnhandled(100, 0).toList();
        // Check observation count and status
        assertEquals(machineCount * observationCount, unhandled.size());
        unhandled.forEach(o -> assertEquals(UNHANDLED, o.getStatus()));

        // Handle data and check status
        final int handled = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(100);
        assertEquals(machineCount * observationCount, handled);

        // Check all data has been marked as handled
        final List<MaintenanceTrackingObservationData> datasAfter =
                maintenanceTrackingObservationDataRepository.findAll();
        assertCollectionSize(machineCount * observationCount, datasAfter);
        datasAfter.forEach(o -> assertEquals(HANDLED, o.getStatus()));

        // Check data for each machine is ok
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        final Map<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);
        assertTrackingGroupsSize(machineCount, trackings);
        assertEquals(machineCount, groups.size());
        groups.values().forEach(g -> {
            final MaintenanceTracking start = g.getFirst();
            final MaintenanceTracking end = g.getLast();
            // 10 observations for each
            assertEquals(10, g.size());
            assertEquals(1, start.getTasks().size());
            assertTrue(start.getTasks().contains(MaintenanceTrackingTask.PAVING));
            assertEquals(now, start.getStartTime());
            assertEquals(now.plus(9, ChronoUnit.MINUTES), end.getEndTime());
        });

        // Check all generated work machines exists
        checkAllWorkMachinesExists(groups, machineCount);
    }

    @Test
    public void combineMultipleTrackings() {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final Instant startTime = getStartTimeOneDayInPast();
        final int observationCountPerTracking = getRandomId(5, 10);
        final int trackingMessagesCount = getRandomId(5, 10);

        // Create <trackingMessagesCount> tracking messages for each machine. Each message contains <observationCountPerTracking> observations for machine
        final Instant endTime =
                // trackingMessagesCount / machine
                IntStream.range(0, trackingMessagesCount).mapToObj(i -> {
                    final Instant start = startTime.plus((long) i * observationCountPerTracking, ChronoUnit.MINUTES);
                    final TyokoneenseurannanKirjausRequestSchema seuranta =
                            createMaintenanceTrackingWithPoints(start, observationCountPerTracking, i + 1, 1,
                                    workMachines, ASFALTOINTI);
                    try {
                        testHelper.saveTrackingDataAsObservations(seuranta);
                    } catch (final JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    return getEndTime(seuranta);
                }).max(Instant::compareTo).orElseThrow();

        int handled;
        int total = 0;
        do {
            handled = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(
                    (machineCount * observationCountPerTracking * trackingMessagesCount) / 10);
            total += handled;
        } while (handled > 0);
        assertEquals(machineCount * trackingMessagesCount * observationCountPerTracking, total);

        // All observations should be combined as one tracking / machine
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(machineCount, trackings);
        final Map<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);

        groups.values().forEach(g -> {
            // 10 observations for each
            final MaintenanceTracking start = g.getFirst();
            final MaintenanceTracking end = g.getLast();
            assertEquals(trackingMessagesCount * observationCountPerTracking, g.size());
            assertEquals(1, start.getTasks().size());
            assertTrue(start.getTasks().contains(MaintenanceTrackingTask.PAVING));
            assertEquals(startTime, start.getStartTime());
            assertEquals(endTime, end.getEndTime());
        });

        checkAllWorkMachinesExists(groups, machineCount);
    }

    @Test
    public void splitTrackingWhenTaskChanges() {
        final int machineCount = getRandomId(2, 10);
        // Work machines with harja id 1,2,...(machineCount+1)
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final Instant startTime = getStartTimeOneDayInPast();
        final int trackingsCountPerMachine = 5;
        final int observationCountPerTracking = 10;

        // Generate <trackingsCountPerMachine> trackings for each machine with <observationCountPerTracking> observations in each tracking message for each machine
        // Task changes for each tracking
        IntStream.range(0, trackingsCountPerMachine).forEach(idx -> {
            // Each tracking starts minute after previous ending time as observations are generated for every minute starting from start time
            final Instant start = startTime.plus((long) idx * observationCountPerTracking, ChronoUnit.MINUTES);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                    createMaintenanceTrackingWithPoints(start, observationCountPerTracking, 1, workMachines,
                            SuoritettavatTehtavatSchema.values()[idx]);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta);
                log.debug("Tracking start={} end={}",
                        seuranta.getHavainnot().getFirst().getHavainto().getHavaintoaika(),
                        seuranta.getHavainnot().getLast().getHavainto()
                                .getHavaintoaika());
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final int handled = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);
        assertEquals(machineCount * trackingsCountPerMachine * observationCountPerTracking, handled);

        // Get trackings for all workmachines
        final Map<Long, List<MaintenanceTracking>> trackingsByHarjaId =
                findAllMaintenanceTrackings()
                        .stream()
                        .collect(Collectors.groupingBy(mt -> mt.getWorkMachine().getHarjaId()));

        // Check that every workmachine has all task generated by range(0, trackingsCountPerMachine) index
        LongStream.range(1, machineCount + 1).forEach(harjaId -> {
            final List<MaintenanceTracking> machineTrackings = trackingsByHarjaId.get(harjaId);
            IntStream.range(0, trackingsCountPerMachine * observationCountPerTracking).forEach(idx -> {
                final MaintenanceTracking t = machineTrackings.get(idx);
                assertEquals(getTaskSetWithIndex(idx / observationCountPerTracking), t.getTasks());
            });
        });
    }

    @Test
    public void splitTrackingWhenJobChanges() throws JsonProcessingException {
        // Work machines with harja id 1
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        // next trackings start minute just after previous ends
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime.plus(10, ChronoUnit.MINUTES), 10, 2, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        final int handled = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);
        assertEquals(20, handled);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        assertEquals(1L, getTrackingGroupStart(trackings, 0).getWorkMachine().getHarjaUrakkaId().longValue());
        assertEquals(2L, getTrackingGroupStart(trackings, 1).getWorkMachine().getHarjaUrakkaId().longValue());
    }

    @Test
    public void insideTimeLimitCombinesTrackingsAsOne() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        final int observationCountPerTracking = 10;
        final int jobId = 1;
        // Last point will be startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime, observationCountPerTracking, 1, jobId, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        // First point will be just 5 min from previous tracking last point -> should combine as same tracking
        // ordinal 1 -> 2 makes next tracking points continue from the end of the previous one
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(
                        startTime.plus((observationCountPerTracking - 1) + maxGapInMinutes, ChronoUnit.MINUTES),
                        observationCountPerTracking, 2, jobId, workMachines, SuoritettavatTehtavatSchema.ASFALTOINTI));
        maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // Assert every source havainto generates new tracking
        assertEquals(observationCountPerTracking * 2, trackings.size());
    }

    @Test
    public void timeLimitBreaksTrackingInGroups() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        // Last point will be startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime, 10, 1, 1, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        // First point will be over 5 min (6 min) from previous tracking last point
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime.plus(10 + maxGapInMinutes, ChronoUnit.MINUTES), 10, 2, 1,
                        workMachines, SuoritettavatTehtavatSchema.ASFALTOINTI));
        maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        assertFirstAndSecondGroupDontOverlap(trackings);
    }

    @Test
    public void timeLimitAndTaskChangeBreaksTrackingInGroups() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime, 10, 1, 1, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        // Second tracking over time gap and task change
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime.plus(10 + maxGapInMinutes, ChronoUnit.MINUTES), 10, 2, 1,
                        workMachines, SuoritettavatTehtavatSchema.PAALLYSTEIDEN_PAIKKAUS));
        maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        assertFirstAndSecondGroupDontOverlap(trackings);
    }

    @Test
    public void overSpeedBreaksTrackingInTwoParts() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        // Last point will be startTime + 9 min
        final TyokoneenseurannanKirjausRequestSchema kirjaus =
                createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, ASFALTOINTI);
        // Set coordinates from 6..10 so far that speed between 5th and 6th point exceeds 120 km/h when there is one minute between points.
        IntStream.range(5, 10).forEach(i -> {
            final KoordinaattisijaintiSchema koordinaatit =
                    kirjaus.getHavainnot().get(i).getHavainto().getSijainti().getKoordinaatit();
            // Set forward  so far that it exceeds speed 120 km/h when there is one minute between points.
            koordinaatit.setX(koordinaatit.getX() + 2500);
        });
        testHelper.saveTrackingDataAsObservations(kirjaus);
        maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();

        assertEquals(2, countTrackingGroups(trackings));
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        assertFirstAndSecondGroupDontOverlap(trackings);
    }

    @Test
    public void taskChangeBreaksTrackingAndLastPointOfFirstTrackingIsSameAsFirstPointOfNextTracking()
            throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime, 10, 1, 1, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        // First point will be after previous last point -> 10 min after start
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime.plus(10, ChronoUnit.MINUTES), 10, 2, 1, workMachines,
                        SuoritettavatTehtavatSchema.PAALLYSTEIDEN_PAIKKAUS));
        maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        assertFirstAndSecondGroupOverlaps(trackings);
    }

    @Test
    public void taskChangeToTransitionBreaksTrackingAndLastPointOfFirstTrackingIsSameAsFirstPointOfTransitionTracking()
            throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final Instant startTime = getStartTimeOneDayInPast();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(startTime, 10, 1, 1, workMachines,
                        SuoritettavatTehtavatSchema.ASFALTOINTI));
        // First point will be after previous last point -> 10 min after start
        final TyokoneenseurannanKirjausRequestSchema transition =
                createMaintenanceTrackingWithPoints(startTime.plus(10, ChronoUnit.MINUTES), 10, 2, 1, workMachines);
        testHelper.saveTrackingDataAsObservations(transition);
        maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);

        final MaintenanceTracking endOfTracking = trackings.getLast();
        final Havainto transitionHavainto = transition.getHavainnot().getFirst().getHavainto();
        final KoordinaattisijaintiSchema koordinaatit = transitionHavainto.getSijainti().getKoordinaatit();
        final Point transitionFirstPoint =
                CoordinateConverter.convertFromETRS89ToWGS84(new Point(koordinaatit.getX(), koordinaatit.getY()));

        assertEquals(endOfTracking.getEndTime(), transitionHavainto.getHavaintoaika());
        assertEquals(endOfTracking.getLastPoint().getX(), transitionFirstPoint.getLongitude(), 0.01);
        assertEquals(endOfTracking.getLastPoint().getY(), transitionFirstPoint.getLatitude(), 0.01);
    }

    @Test
    public void longJumpInLineStringData() throws IOException {
        testHelper.saveTrackingFromResourceToDbAsObservations(
                "classpath:harja/service/distancegap/long-jump-twice-1.json");

        log.info("Handled count={}",
                maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000));

        // Jump twice -> should split to three
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(3, trackings);
    }

    @Test
    public void lineStringsShouldBeHandledAsOneIfNoDistanceGap() throws IOException {

        // 3 and 4 point long linestrings
        testHelper.saveTrackingFromResourceToDbAsObservations(
                "classpath:harja/service/linestring/linestring-first.json");
        testHelper.saveTrackingFromResourceToDbAsObservations(
                "classpath:harja/service/linestring/linestring-second.json");

        log.info("Handled count={}",
                maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(100));

        // Two linestring trackings without long jump between last and first point should be joined
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // First tracking has end poind added from second tracking first poing
        final MaintenanceTracking first = trackings.getFirst();
        final MaintenanceTracking second = trackings.get(1);
        assertEquals(3, first.getGeometry()
                .getNumPoints()); // Simplification takes points from 3 -> 2, but next tracking start point is appended so sum is 2+1
        assertEquals(4, second.getGeometry().getNumPoints()); // Simplification drops from 5 -> 4 points
        assertEquals(first.getLastPoint(), PostgisGeometryUtils.getStartPoint(second.getGeometry()));
    }

    @Test
    @Rollback(value = false)
    public void singlePointLineStringsShouldBeHandledAsLineStringTrackings() throws IOException {

        testHelper.saveTrackingFromResourceToDbAsObservations(
                "classpath:harja/service/linestring/point-linestring-1.json");
        testHelper.saveTrackingFromResourceToDbAsObservations(
                "classpath:harja/service/linestring/point-linestring-2.json");
        testHelper.saveTrackingFromResourceToDbAsObservations(
                "classpath:harja/service/linestring/point-linestring-3.json");

        log.info("Handled count={}",
                maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(100));

        // 3 LineStrings with single point in each should be combined as one tracking group
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // Consecutive single point trackings are connected by adding next tracking first point as end point for previous tracking resulting:
        // tracking[0]: [t1p1,t2p1] (t1 = tracking 1., p1 = point 1.)
        // tracking[1]: [t2p1,t3p1],
        // tracking[2]: [t3p1],
        assertEquals(2, trackings.getFirst().getGeometry().getNumPoints());
        assertEquals(2, trackings.get(1).getGeometry().getNumPoints());
        assertEquals(1, trackings.get(2).getGeometry().getNumPoints());
        // previous tracking end point should be next tracking start point
        assertEquals(trackings.get(1).getGeometry().getCoordinates()[0],
                trackings.getFirst().getGeometry().getCoordinates()[1]);
        assertEquals(trackings.get(2).getGeometry().getCoordinates()[0],
                trackings.get(1).getGeometry().getCoordinates()[1]);
        assertEquals(trackings.getFirst().getId(), trackings.get(1).getPreviousTrackingId());
        assertEquals(trackings.get(1).getId(), trackings.get(2).getPreviousTrackingId());
    }

    @Test
    @Rollback(value = false)
    public void singleCoordinateTrackingsShouldBeHandledAsLineStringTrackings() throws IOException {

        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/point/point-1.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/point/point-2.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/point/point-3.json");

        log.info("Handled count={}",
                maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(100));

        // 3 single coordinate trackings should be combined as one tracking group
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // Consecutive single point trackings are connected by adding next tracking first point as end point for previous tracking resulting:
        // tracking[0]: [t1p1,t2p1] (t1 = tracking 1., p1 = point 1.)
        // tracking[1]: [t2p1,t3p1],
        // tracking[2]: [t3p1],
        assertEquals(2, trackings.getFirst().getGeometry().getNumPoints());
        assertEquals(2, trackings.get(1).getGeometry().getNumPoints());
        assertEquals(1, trackings.get(2).getGeometry().getNumPoints());
        // previous tracking end point should be next tracking start point
        assertEquals(trackings.get(1).getGeometry().getCoordinates()[0],
                trackings.getFirst().getGeometry().getCoordinates()[1]);
        assertEquals(trackings.get(2).getGeometry().getCoordinates()[0],
                trackings.get(1).getGeometry().getCoordinates()[1]);
        assertEquals(trackings.getFirst().getId(), trackings.get(1).getPreviousTrackingId());
        assertEquals(trackings.get(1).getId(), trackings.get(2).getPreviousTrackingId());
    }

    @Test
    public void deleteDataOlderThanDays() throws IOException {
        final Instant start10Days = TimeUtil.nowWithoutMillis().minus(10, ChronoUnit.DAYS);
        final Instant start9Days = TimeUtil.nowWithoutMillis().minus(9, ChronoUnit.DAYS);
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(start10Days, 10, 1, 1, SuoritettavatTehtavatSchema.ASFALTOINTI));
        testHelper.saveTrackingDataAsObservations(
                createMaintenanceTrackingWithPoints(start9Days, 10, 1, 1, SuoritettavatTehtavatSchema.ASFALTOINTI));
        assertCollectionSize(0, findAllMaintenanceTrackings());
        final int count = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(1000);
        // Assert all handled
        assertEquals(20, count);
        assertCollectionSize(20, findAllMaintenanceTrackings());
        assertCollectionSize(20, maintenanceTrackingObservationDataRepository.findAll());
        final long deleded1 = maintenanceTrackingUpdateServiceV1.deleteDataOlderThanDays(9, 5);
        assertEquals(5, deleded1);
        final long deleded2 = maintenanceTrackingUpdateServiceV1.deleteDataOlderThanDays(9, 5);
        assertEquals(5, deleded2);
        final long deleded3 = maintenanceTrackingUpdateServiceV1.deleteDataOlderThanDays(9, 5);
        assertEquals(0, deleded3);
        assertCollectionSize(10, maintenanceTrackingObservationDataRepository.findAll());
        // Handled data is not deleted
        assertCollectionSize(20, findAllMaintenanceTrackings());
    }

    private void assertTrackingGroupsSize(final int size, final List<MaintenanceTracking> trackings) {
        assertEquals(size, countTrackingGroups(trackings));
    }

    private int countTrackingGroups(final List<MaintenanceTracking> trackings) {
        final Map<Long, Long> idToPreviousIdMapping =
                trackings.stream().collect(Collectors.toMap(MaintenanceTracking::getId,
                        mt -> mt.getPreviousTrackingId() != null ? mt.getPreviousTrackingId() : mt.getId()));
        final Set<Long> startIds =
                idToPreviousIdMapping.keySet().stream().map(k -> getTrackingStartId(idToPreviousIdMapping, k))
                        .collect(Collectors.toSet());
        return startIds.size();
    }

    private Long getTrackingStartId(final Map<Long, Long> idToPreviousIdMapping, final long id) {
        final Long found = idToPreviousIdMapping.get(id);
        if (found.equals(id)) {
            return id;
        }
        return getTrackingStartId(idToPreviousIdMapping, found);
    }

    /**
     * Gets start of continuous tracking group with index. Continuous group of trackings means tracking chain
     * with reference to previous tracking.
     *
     * @param trackings  to find group from
     * @param groupIndex index of group (chain of trackings) 0, 1,... etc
     * @return First tracking of the tracking group
     */
    private MaintenanceTracking getTrackingGroupStart(final List<MaintenanceTracking> trackings, final int groupIndex) {
        int currentGroupIndex = 0;
        for (int i = 0; i < trackings.size(); i++) {
            final MaintenanceTracking t = trackings.get(i);
            if (t.getPreviousTrackingId() == null && i > 0) {
                currentGroupIndex++;
            }
            if (currentGroupIndex == groupIndex) {
                // Return first in the currentGroupIndex
                return trackings.get(i);
            }
        }
        throw new IllegalArgumentException("End not found");
    }

    private List<MaintenanceTracking> findAllMaintenanceTrackings() {
        return maintenanceTrackingRepository.findAll(Sort.by("startTime"));
    }

    private LinkedHashMap<Long, List<MaintenanceTracking>> groupTrackingsByStartId(
            final List<MaintenanceTracking> trackings) {
        final Map<Long, MaintenanceTracking> idToTrackingMap =
                trackings.stream().collect(Collectors.toMap(MaintenanceTracking::getId, Function.identity()));
        final LinkedHashMap<Long, List<MaintenanceTracking>> groupsByStartId = new LinkedHashMap<>();
        trackings.forEach(t -> {
            // This is the first one to handle
            if (t.getPreviousTrackingId() == null) {
                groupsByStartId.put(t.getId(), new ArrayList<>(Collections.singleton(t)));
            } else {
                // Find the first in group and add tracking to it's group
                MaintenanceTracking previous = t;
                while (previous.getPreviousTrackingId() != null) {
                    previous = idToTrackingMap.get(previous.getPreviousTrackingId());
                }
                groupsByStartId.get(previous.getId()).add(t);
            }
        });
        return groupsByStartId;
    }

    private void checkAllWorkMachinesExists(final Map<Long, List<MaintenanceTracking>> groups, final int machineCount) {
        final List<MaintenanceTrackingWorkMachine> wms =
                groups.values().stream()
                        .map(list -> list.getFirst().getWorkMachine())
                        .sorted(Comparator.comparing(MaintenanceTrackingWorkMachine::getHarjaId))
                        .toList();
        // Check all work machines exists with harjaIds generated sequential from 1 onwards
        IntStream.range(0, machineCount).forEach(i -> assertEquals(i + 1, wms.get(i).getHarjaId().intValue()));
    }

    private void assertFirstAndSecondGroupDontOverlap(final List<MaintenanceTracking> trackings) {
        final LinkedHashMap<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);
        final Iterator<List<MaintenanceTracking>> iter = groups.values().iterator();
        final List<MaintenanceTracking> firstGroup = iter.next();
        final MaintenanceTracking firstGroupEnd = firstGroup.getLast();
        final MaintenanceTracking secondGroupStart = iter.next().getFirst();
        assertNotEquals(firstGroupEnd.getEndTime(), secondGroupStart.getStartTime());
        assertNotEquals(firstGroupEnd.getLastPoint(),
                secondGroupStart.getGeometry() != null ?
                PostgisGeometryUtils.getStartPoint(secondGroupStart.getGeometry()) : secondGroupStart.getLastPoint());
    }

    private void assertFirstAndSecondGroupOverlaps(final List<MaintenanceTracking> trackings) {
        final LinkedHashMap<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);
        final Iterator<List<MaintenanceTracking>> iter = groups.values().iterator();
        final List<MaintenanceTracking> firstGroup = iter.next();
        final MaintenanceTracking firstGroupEnd = firstGroup.getLast();
        final MaintenanceTracking secondGroupStart = iter.next().getFirst();
        assertEquals(firstGroupEnd.getEndTime(), secondGroupStart.getStartTime());
        assertEquals(firstGroupEnd.getLastPoint(),
                secondGroupStart.getGeometry() != null ?
                PostgisGeometryUtils.getStartPoint(secondGroupStart.getGeometry()) : secondGroupStart.getLastPoint());
    }

}
