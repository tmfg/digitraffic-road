package fi.livi.digitraffic.tie.service.v3.maintenance;

import static fi.livi.digitraffic.tie.TestUtils.getRandomId;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.model.v3.maintenance.V3MaintenanceTrackingObservationData.Status.HANDLED;
import static fi.livi.digitraffic.tie.model.v3.maintenance.V3MaintenanceTrackingObservationData.Status.UNHANDLED;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createMaintenanceTrackingWithPoints;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.createWorkMachines;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getEndTime;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getStartTimeOneDayInPast;
import static fi.livi.digitraffic.tie.service.v3.maintenance.V3MaintenanceTrackingServiceTestHelper.getTaskSetWithIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.v3.V3MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import fi.livi.digitraffic.tie.metadata.geojson.converter.CoordinateConverter;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.model.v3.maintenance.V3MaintenanceTrackingObservationData;

public class V3MaintenanceTrackingUpdateServiceTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(V3MaintenanceTrackingUpdateServiceTest.class);

    @Autowired
    private V3MaintenanceTrackingObservationDataRepository v3MaintenanceTrackingObservationDataRepository;

    @Autowired
    private V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;

    @Autowired
    private V3MaintenanceTrackingUpdateService v3MaintenanceTrackingUpdateService;

    @Autowired
    private V3MaintenanceTrackingServiceTestHelper testHelper;

    @Value("${workmachine.tracking.distinct.observation.gap.minutes}")
    private int maxGapInMinutes;

    @BeforeEach
    public void init() {
        testHelper.clearDb();
    }

    @Test
    public void readUnhandedObservationDataWithMultipleWorkMachinesMatches() throws IOException {
        final ZonedDateTime now = getStartTimeOneDayInPast();
        final int machineCount = getRandomId(2, 10);
        final int observationCount = 10;
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        // Create maintenance tracking message for <machineCount> machines and <observationCount> observations for each machine
        // and save observations to db
        final TyokoneenseurannanKirjausRequestSchema seuranta =
            createMaintenanceTrackingWithPoints(now, 10, 1, workMachines, ASFALTOINTI);
        testHelper.saveTrackingDataAsObservations(seuranta);

        // Check that data can be fetched from db
        final List<V3MaintenanceTrackingObservationData> unhandled =
            v3MaintenanceTrackingObservationDataRepository.findUnhandled(100, 0).collect(Collectors.toList());
        assertEquals(observationCount * machineCount, unhandled.size());

        // Check that unhanded are in ascending order by observation time
        final AtomicReference<Instant> prevObservationTime = new AtomicReference<> (Instant.MIN);
        unhandled.forEach(o -> {
            assertEquals(UNHANDLED, o.getStatus());
            final Instant observationTime = o.getObservationTime();
            log.debug("observationTime={}", observationTime);
            assertTrue(observationTime.equals(prevObservationTime.get()) || observationTime.isAfter(prevObservationTime.get()));
            prevObservationTime.set(observationTime);
        });

        // Check that all machines has right count of observations
        IntStream.range(0, machineCount)
            .forEach(i -> assertEquals(10, unhandled.stream()
                .filter(observation -> observation.getHarjaWorkmachineId().equals(workMachines.get(i).getId().longValue())).count()));

        // Check that saved observations has valid observation json
        seuranta.getHavainnot().forEach(havainto -> {
            try {
                final Havainto h = havainto.getHavainto();
                final String formatedJson = testHelper.getFormatedObservationJson(h);
                final V3MaintenanceTrackingObservationData observation =
                    unhandled.stream()
                        .filter(o -> o.getHarjaWorkmachineId().equals(h.getTyokone().getId().longValue()) &&
                                     o.getObservationTime().equals(h.getHavaintoaika().toInstant()))
                        .findFirst().orElseThrow();
                assertEquals(formatedJson, observation.getJson());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void handleTrackingWithMultipleWorkMachines() throws JsonProcessingException {
        // Create maintenance tracking message for <machineCount> machines and <observationCount> observations for each machine
        // and save observations to db
        final ZonedDateTime now = getStartTimeOneDayInPast();
        final int machineCount = getRandomId(2, 10);
        final int observationCount = 10;
        final TyokoneenseurannanKirjausRequestSchema seuranta =
            createMaintenanceTrackingWithPoints(now, observationCount, 1, machineCount, ASFALTOINTI);
        testHelper.saveTrackingDataAsObservations(seuranta);

        final List<V3MaintenanceTrackingObservationData> unhandled =
            v3MaintenanceTrackingObservationDataRepository.findUnhandled(100,0).collect(Collectors.toList());
        // Check observation count and status
        assertEquals(machineCount * observationCount, unhandled.size());
        unhandled.forEach(o -> assertEquals(UNHANDLED, o.getStatus()));

        // Handle data and check status
        final int handled = v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(100);
        assertEquals(machineCount * observationCount, handled);

        // Check all data has been marked as handled
        final List<V3MaintenanceTrackingObservationData> datasAfter = v3MaintenanceTrackingObservationDataRepository.findAll();
        assertCollectionSize(machineCount * observationCount, datasAfter);
        datasAfter.forEach(o -> assertEquals(HANDLED, o.getStatus()));

        // Check data for each machine is ok
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        final Map<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);
        assertTrackingGroupsSize(machineCount, trackings);
        assertEquals(machineCount, groups.size());
        groups.values().forEach(g -> {
            final MaintenanceTracking start = g.get(0);
            final MaintenanceTracking end = g.get(g.size()-1);
            // 10 observations for each
            assertEquals(10, g.size());
            assertEquals(1, start.getTasks().size());
            assertTrue(start.getTasks().contains(MaintenanceTrackingTask.PAVING));
            assertEquals(now, start.getStartTime());
            assertEquals(now.plusMinutes(9), end.getEndTime());
        });

        // Check all generated work machines exists
        checkAllWorkMachinesExists(groups, machineCount);
    }

    @Test
    public void combineMultipleTrackings() {
        final int machineCount = getRandomId(2, 10);
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        final int observationCountPerTracking = getRandomId(5, 10);
        final int trackingMessagesCount = getRandomId(5, 10);

        // Create <trackingMessagesCount> tracking messages for each machine. Each message contains <observationCountPerTracking> observations for machine
        final ZonedDateTime endTime =
            // trackingMessagesCount / machine
            IntStream.range(0,trackingMessagesCount).mapToObj(i -> {
                final ZonedDateTime start = startTime.plusMinutes((long) i * observationCountPerTracking);
                final TyokoneenseurannanKirjausRequestSchema seuranta =
                    createMaintenanceTrackingWithPoints(start, observationCountPerTracking, i+1, 1, workMachines, ASFALTOINTI);
                try {
                    testHelper.saveTrackingDataAsObservations(seuranta);
                } catch (final JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return getEndTime(seuranta);
            }).max(ChronoZonedDateTime::compareTo).orElseThrow();

        int handled;
        int total = 0;
        do {
            handled = v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData((machineCount*observationCountPerTracking*trackingMessagesCount)/10);
            total += handled;
        } while (handled > 0);
        assertEquals(machineCount * trackingMessagesCount * observationCountPerTracking, total);

        // All observations should be combined as one tracking / machine
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(machineCount, trackings);
        final Map<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);

        groups.values().forEach(g -> {
            // 10 observations for each
            final MaintenanceTracking start = g.get(0);
            final MaintenanceTracking end = g.get(g.size()-1);
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
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        final int trackingsCountPerMachine = 5;
        final int observationCountPerTracking = 10;

        // Generate <trackingsCountPerMachine> trackings for each machine with <observationCountPerTracking> observations in each tracking message for each machine
        // Task changes for each tracking
        IntStream.range(0, trackingsCountPerMachine).forEach(idx -> {
            // Each tracking starts minute after previous ending time as observations are generated for every minute starting from start time
            final ZonedDateTime start = startTime.plusMinutes((long) idx *observationCountPerTracking);
            final TyokoneenseurannanKirjausRequestSchema seuranta =
                createMaintenanceTrackingWithPoints(start, observationCountPerTracking, 1, workMachines, SuoritettavatTehtavat.values()[idx]);
            try {
                testHelper.saveTrackingDataAsObservations(seuranta);
                log.debug("Tracking start={} end={}",
                          seuranta.getHavainnot().get(0).getHavainto().getHavaintoaika(),
                          seuranta.getHavainnot().get(seuranta.getHavainnot().size()-1).getHavainto().getHavaintoaika());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final int handled = v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);
        assertEquals(machineCount * trackingsCountPerMachine * observationCountPerTracking, handled);

        // Get trackings for all workmachines
        final Map<Long, List<MaintenanceTracking>> trackingsByHarjaId =
            findAllMaintenanceTrackings()
                .stream()
                .collect(Collectors.groupingBy(mt -> mt.getWorkMachine().getHarjaId()));

        // Check that every workmachine has all task generated by range(0, trackingsCountPerMachine) index
        LongStream.range(1, machineCount + 1).forEach(harjaId -> {
            final List<MaintenanceTracking> machineTrackings = trackingsByHarjaId.get(harjaId);
            IntStream.range(0, trackingsCountPerMachine*observationCountPerTracking).forEach(idx -> {
                final MaintenanceTracking t = machineTrackings.get(idx);
                assertEquals(getTaskSetWithIndex(idx/observationCountPerTracking), t.getTasks());
            });
        });
    }

    @Test
    public void splitTrackingWhenJobChanges() throws JsonProcessingException {
        // Work machines with harja id 1
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // next trackings start minute just after previous ends
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        final int handled = v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);
        assertEquals(20, handled);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        assertEquals(1L, getTrackingGroupStart(trackings, 0).getWorkMachine().getHarjaUrakkaId().longValue());
        assertEquals(2L, getTrackingGroupStart(trackings, 1).getWorkMachine().getHarjaUrakkaId().longValue());
    }

    @Test
    public void insideTimeLimitCombinesTrackingsAsOne() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        final int observationCountPerTracking = 10;
        final int jobId = 1;
        // Last point will be startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, observationCountPerTracking, jobId, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be just 5 min from previous tracking last point -> should combine as same tracking
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes((observationCountPerTracking-1) + maxGapInMinutes), observationCountPerTracking, jobId, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // Assert every source havainto generates new tracking
        assertEquals(observationCountPerTracking * 2, trackings.size());
    }

    @Test
    public void timeLimitBreaksTrackingInGroups() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        // Last point will be startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be over 5 min (6 min) from previous tracking last point
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10+maxGapInMinutes), 10, 2, 1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        assertFirstAndSecondGroupDontOverlap(trackings);
    }

    @Test
    public void timeLimitAndTaskChangeBreaksTrackingInGroups() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // Second tracking over time gap and task change
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10 + maxGapInMinutes), 10, 2, 1, workMachines, SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS));
        v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        assertFirstAndSecondGroupDontOverlap(trackings);
    }

    @Test
    public void overSpeedBreaksTrackingInTwoParts() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        // Last point will be startTime + 9 min
        final TyokoneenseurannanKirjausRequestSchema kirjaus =
            createMaintenanceTrackingWithPoints(startTime, 10, 1, workMachines, ASFALTOINTI);
        // Set coordinates from 6..10 so far that speed between 5th and 6th point exceeds 120 km/h when there is one minute between points.
        IntStream.range(5,10).forEach(i -> {
            final KoordinaattisijaintiSchema koordinaatit = kirjaus.getHavainnot().get(i).getHavainto().getSijainti().getKoordinaatit();
            // Set forward  so far that it exceeds speed 120 km/h when there is one minute between points.
            koordinaatit.setX(koordinaatit.getX() + 2500);
        });
        testHelper.saveTrackingDataAsObservations(kirjaus);
        v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();

        assertEquals(2, countTrackingGroups(trackings));
        trackings.sort(Comparator.comparing(MaintenanceTracking::getStartTime));

        assertFirstAndSecondGroupDontOverlap(trackings);
    }

    @Test
    public void taskChangeBreaksTrackingAndLastPointOfFirstTrackingIsSameAsFirstPointOfNextTracking() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be after previous last point -> 10 min after start
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2, 1, workMachines, SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS));
        v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(2, trackings);
        assertFirstAndSecondGroupOverlaps(trackings);
    }


    @Test
    public void taskChangeToTransitionBreaksTrackingAndLastPointOfFirstTrackingIsSameAsFirstPointOfTransitionTracking() throws JsonProcessingException {
        final List<Tyokone> workMachines = createWorkMachines(1);
        final ZonedDateTime startTime = getStartTimeOneDayInPast();
        // Last point will be in time startTime + 9 min
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(startTime, 10, 1,1, workMachines, SuoritettavatTehtavat.ASFALTOINTI));
        // First point will be after previous last point -> 10 min after start
        final TyokoneenseurannanKirjausRequestSchema transition = createMaintenanceTrackingWithPoints(startTime.plusMinutes(10), 10, 2,1, workMachines);
        testHelper.saveTrackingDataAsObservations(transition);
        v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);

        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);

        final MaintenanceTracking endOfTracking = trackings.get(trackings.size()-1);
        final Havainto transitionHavainto = transition.getHavainnot().get(0).getHavainto();
        final KoordinaattisijaintiSchema koordinaatit = transitionHavainto.getSijainti().getKoordinaatit();
        final Point transitionFirstPoint = CoordinateConverter.convertFromETRS89ToWGS84(new Point(koordinaatit.getX(), koordinaatit.getY()));

        assertEquals(endOfTracking.getEndTime(), transitionHavainto.getHavaintoaika());
        assertEquals(endOfTracking.getLastPoint().getX(), transitionFirstPoint.getLongitude(), 0.01);
        assertEquals(endOfTracking.getLastPoint().getY(), transitionFirstPoint.getLatitude(), 0.01);
    }

    @Test
    public void longJumpInLineStringData() throws IOException {
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/distancegap/long-jump-twice-1.json");

        log.info("Handled count={}", v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000));

        // Jump twice -> should split to three
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(3, trackings);
    }

    @Test
    public void lineStringsShouldBeHandledAsOneIfNoDistanceGap() throws IOException {

        // 3 and 4 point long linestrings
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/linestring-first.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/linestring-second.json");

        log.info("Handled count={}", v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(100));

        // Two linestring trackings without long jump between last and first point should be joined
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // First tracking has end poind added from second tracking first poing
        final MaintenanceTracking first = trackings.get(0);
        final MaintenanceTracking second = trackings.get(1);
        assertEquals(3, first.getLineString().getNumPoints()); // Simplification takes points from 3 -> 2, but next tracking start point is appended so sum is 2+1
        assertEquals(4, second.getLineString().getNumPoints()); // Simplification drops from 5 -> 4 points
        assertEquals(first.getLastPoint(), second.getLineString().getStartPoint());
    }

    @Test
    public void singlePointLineStringsShouldBeHandledAsLineStringTrackings() throws IOException {

        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/point-linestring-1.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/point-linestring-2.json");
        testHelper.saveTrackingFromResourceToDbAsObservations("classpath:harja/service/linestring/point-linestring-3.json");

        log.info("Handled count={}", v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(100));

        // 3 LineStrings with single point in each should be combined as one tracking
        final List<MaintenanceTracking> trackings = findAllMaintenanceTrackings();
        assertTrackingGroupsSize(1, trackings);
        // single points are duplicated (not the starting one) as two same point linestring -> 1 + 2 + 2 = 5 points
        assertEquals(2, trackings.get(0).getLineString().getNumPoints());
        assertEquals(2, trackings.get(1).getLineString().getNumPoints());
        assertNull(trackings.get(2).getLineString());
        assertEquals(trackings.get(0).getId(), trackings.get(1).getPreviousTrackingId());
        assertEquals(trackings.get(1).getId(), trackings.get(2).getPreviousTrackingId());
    }

    @Test
    public void deleteDataOlderThanDays() throws IOException {
        final ZonedDateTime start10Days = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusDays(10);
        final ZonedDateTime start9Days = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusDays(9);
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(start10Days, 10, 1, 1, SuoritettavatTehtavat.ASFALTOINTI));
        testHelper.saveTrackingDataAsObservations(
            createMaintenanceTrackingWithPoints(start9Days, 10, 1, 1, SuoritettavatTehtavat.ASFALTOINTI));
        assertCollectionSize(0, findAllMaintenanceTrackings());
        final int count = v3MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingObservationData(1000);
        // Assert all handled
        assertEquals( 20, count);
        assertCollectionSize(20, findAllMaintenanceTrackings());
        assertCollectionSize( 20, v3MaintenanceTrackingObservationDataRepository.findAll());
        final long deleded1 = v3MaintenanceTrackingUpdateService.deleteDataOlderThanDays(9, 5);
        assertEquals( 5, deleded1);
        final long deleded2 = v3MaintenanceTrackingUpdateService.deleteDataOlderThanDays(9, 5);
        assertEquals( 5, deleded2);
        final long deleded3 = v3MaintenanceTrackingUpdateService.deleteDataOlderThanDays(9, 5);
        assertEquals( 0, deleded3);
        assertCollectionSize( 10, v3MaintenanceTrackingObservationDataRepository.findAll());
        // Handled data is not deleted
        assertCollectionSize(20, findAllMaintenanceTrackings());
    }

    private void assertTrackingGroupsSize(final int size, final List<MaintenanceTracking> trackings) {
        assertEquals(size, countTrackingGroups(trackings));
    }

    private int countTrackingGroups(final List<MaintenanceTracking> trackings) {
        final Map<Long, Long> idToPreviousIdMapping =
            trackings.stream().collect(Collectors.toMap(MaintenanceTracking::getId, mt -> mt.getPreviousTrackingId() != null ? mt.getPreviousTrackingId() : mt.getId()));
        final Set<Long> startIds = idToPreviousIdMapping.keySet().stream().map(k -> getTrackingStartId(idToPreviousIdMapping, k)).collect(Collectors.toSet());
        return startIds.size();
    }

    private Long getTrackingStartId(final Map<Long, Long> idToPreviousIdMapping, long id) {
        final Long found = idToPreviousIdMapping.get(id);
        if (found.equals(id)) {
            return id;
        }
        return getTrackingStartId(idToPreviousIdMapping, found);
    }


    /**
     * Gets start of continuous tracking group with index. Continuous group of trackings means tracking chain
     * with reference to previous tracking.
     * @param trackings to find group from
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

    /**
     * Gets end of continuous tracking group with index. Continuous group of trackings means tracking chain
     * with reference to previous tracking.
     * @param trackings to find group from
     * @param groupIndex index of group (chain of trackings) 0, 1,... etc
     * @return Latest (end) tracking of the tracking group
     */
    private MaintenanceTracking getTrackingGroupEnd(final List<MaintenanceTracking> trackings, final int groupIndex) {
        int currentGroupIndex = 0;
        for (int i = 0; i < trackings.size(); i++) {
            final MaintenanceTracking t = trackings.get(i);
            if (t.getPreviousTrackingId() == null && i > 0) {
                currentGroupIndex++;
            }
            if (currentGroupIndex > groupIndex) {
                // The end of previous group is previous tracking
                return trackings.get(i-1);
            }
            // If we are in the right group and last tracking, return it
            if (currentGroupIndex == groupIndex && i == trackings.size()-1) {
                return trackings.get(i);
            }
        }
        throw new IllegalArgumentException("End not found");
    }


    private List<MaintenanceTracking> findAllMaintenanceTrackings() {
        return v2MaintenanceTrackingRepository.findAll(Sort.by("startTime"));
    }

    private LinkedHashMap<Long, List<MaintenanceTracking>> groupTrackingsByStartId(final List<MaintenanceTracking> trackings) {
        final Map<Long, MaintenanceTracking> idToTrackingMap =
            trackings.stream().collect(Collectors.toMap(MaintenanceTracking::getId, Function.identity()));
        LinkedHashMap<Long, List<MaintenanceTracking>> groupsByStartId = new LinkedHashMap<>();
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
                .map(list -> list.get(0).getWorkMachine())
                .sorted(Comparator.comparing(MaintenanceTrackingWorkMachine::getHarjaId))
                .collect(Collectors.toList());
        // Check all work machines exists with harjaIds generated sequential from 1 onwards
        IntStream.range(0, machineCount).forEach(i -> assertEquals(i+1, wms.get(i).getHarjaId().intValue()));
    }

    private void assertFirstAndSecondGroupDontOverlap(final List<MaintenanceTracking> trackings) {
        final LinkedHashMap<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);
        final Iterator<List<MaintenanceTracking>> iter = groups.values().iterator();
        final List<MaintenanceTracking> firstGroup = iter.next();
        final MaintenanceTracking firstGroupEnd = firstGroup.get(firstGroup.size()-1);
        final MaintenanceTracking secondGroupStart = iter.next().get(0);
        Assertions.assertNotEquals(firstGroupEnd.getEndTime(), secondGroupStart.getStartTime());
        Assertions.assertNotEquals(firstGroupEnd.getLastPoint(), secondGroupStart.getLineString() != null ? secondGroupStart.getLineString().getStartPoint() : secondGroupStart.getLastPoint());
    }

    private void assertFirstAndSecondGroupOverlaps(final List<MaintenanceTracking> trackings) {
        final LinkedHashMap<Long, List<MaintenanceTracking>> groups = groupTrackingsByStartId(trackings);
        final Iterator<List<MaintenanceTracking>> iter = groups.values().iterator();
        final List<MaintenanceTracking> firstGroup = iter.next();
        final MaintenanceTracking firstGroupEnd = firstGroup.get(firstGroup.size()-1);
        final MaintenanceTracking secondGroupStart = iter.next().get(0);
        assertEquals(firstGroupEnd.getEndTime(), secondGroupStart.getStartTime());
        assertEquals(firstGroupEnd.getLastPoint(), secondGroupStart.getLineString() != null ? secondGroupStart.getLineString().getStartPoint() : secondGroupStart.getLastPoint());
    }

}
