package fi.livi.digitraffic.tie.service.v2.maintenance;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingDataRepository;
import fi.livi.digitraffic.tie.dao.v2.V2MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.external.harja.Havainnot;
import fi.livi.digitraffic.tie.external.harja.Havainto;
import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import fi.livi.digitraffic.tie.external.harja.Tyokone;
import fi.livi.digitraffic.tie.external.harja.TyokoneenseurannanKirjausRequestSchema;
import fi.livi.digitraffic.tie.external.harja.entities.GeometriaSijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.KoordinaattisijaintiSchema;
import fi.livi.digitraffic.tie.external.harja.entities.Lahettaja;
import fi.livi.digitraffic.tie.external.harja.entities.OrganisaatioSchema;
import fi.livi.digitraffic.tie.external.harja.entities.OtsikkoSchema;
import fi.livi.digitraffic.tie.external.harja.entities.TunnisteSchema;
import fi.livi.digitraffic.tie.external.harja.entities.ViivageometriasijaintiSchema;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTracking;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTrackingTask;

@Service
public class V2MaintenanceTrackingServiceTestHelper {

    private final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService;
    private final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository;
    private final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository;
    private final ObjectReader jsonReader;
    private final ObjectWriter jsonWriter;
    private final EntityManager entityManager;
    private final ResourceLoader resourceLoader;

    public static final String COMPANY = "Tie huolto Oy";
    public static final String COMPANY_ID = "8561566-0";

    public static final double RANGE_X_MIN = 19.0;
    public static final double RANGE_X_MAX = 32.0;
    public static final double RANGE_Y_MIN = 59.0;
    public static final double RANGE_Y_MAX = 72.0;

    public static final int RANGE_X_MIN_ETRS = 41086; // 41085.768094711006
    public static final int RANGE_X_MAX_ETRS = 672275; // 672275.051072051
    public static final int RANGE_Y_MIN_ETRS = 6567583; // 6567582.7414794015
    public static final int RANGE_Y_MAX_ETRS = 7996086; // 7996086.925158125

    public final static Instant SINGLE_REALISATIONS_3_TASKS_END_TIME = ZonedDateTime.parse("2020-01-13T10:48:47Z").toInstant();
    public final static Instant TrackingS_8_TASKS_2_END_TIME = ZonedDateTime.parse("2020-01-13T12:06:55Z").toInstant();

    /*  SINGLE_REALISATIONS_3_TASKS should have following points for Tracking with task 12911L, 1368L
           WGS84                 ETRS-TM35FIN
        1. x=25.87174 y=64.26403 P: 7126921 m I: 445338 m - Pukkila
        2. x=25.95947 y=64.17967 P: 7117449 m I: 449434 m - Piippola
        3. x=26.33006 y=64.10373 P: 7108745 m I: 467354 m - Pyhäntä

        Bounding box min x=26.3 y=64.1 max x=27.0 y=65.0 should contain Pyhäntä point
        Bounding box min x=26.34 y=64.1 max x=27.0 y=65.0 should not contain any points */
    public final static Pair<Double, Double> RANGE_X_AROUND_TASK = Pair.of(26.3, 27.0);
    public final static Pair<Double, Double> RANGE_Y_AROUND_TASK = Pair.of(64.1, 65.0);
    public final static Set<Long> TASK_IDS_INSIDE_BOX = new HashSet<>(Arrays.asList(12911L, 1368L));
    public final static Set<Integer> TASK_IDS_INSIDE_BOX_INTS = new HashSet<>(Arrays.asList(12911, 1368));
    public final static Pair<Double, Double> RANGE_X_OUTSIDE_TASK = Pair.of(26.34, 27.0);
    public final static Pair<Double, Double> RANGE_Y_OUTSIDE_TASK = Pair.of(64.1, 65.0);

    @Autowired
    public V2MaintenanceTrackingServiceTestHelper(final ObjectMapper objectMapper,
                                                  final V2MaintenanceTrackingUpdateService v2MaintenanceTrackingUpdateService,
                                                  final V2MaintenanceTrackingRepository v2MaintenanceTrackingRepository,
                                                  final V2MaintenanceTrackingDataRepository v2MaintenanceTrackingDataRepository,
                                                  final EntityManager entityManager,
                                                  final ResourceLoader resourceLoader) {

        this.v2MaintenanceTrackingUpdateService = v2MaintenanceTrackingUpdateService;
        this.v2MaintenanceTrackingRepository = v2MaintenanceTrackingRepository;
        this.jsonWriter = objectMapper.writerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.jsonReader = objectMapper.readerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.v2MaintenanceTrackingDataRepository = v2MaintenanceTrackingDataRepository;
        this.entityManager = entityManager;
        this.resourceLoader = resourceLoader;
    }

    public void clearDb() {
        v2MaintenanceTrackingRepository.deleteAllInBatch();
        v2MaintenanceTrackingDataRepository.deleteAllInBatch();
    }

    public void checkCoordinateCount(final MaintenanceTracking tracking, final int count) {
        Assert.assertEquals(count, tracking.getLineString().getCoordinates().length);
    }

    public void checkContainsOnlyTasksWithIds(final MaintenanceTracking tracking, final MaintenanceTrackingTask... tasks) {
        final Set<MaintenanceTrackingTask> actualTasks = tracking.getTasks();
        final HashSet<MaintenanceTrackingTask> expectedTasks = new HashSet<MaintenanceTrackingTask>(Arrays.asList(tasks));
        Assert.assertEquals(expectedTasks, actualTasks);
    }

    private void saveTrackingDataAsPlainText(final String trackingJson) {
        final MaintenanceTrackingData Tracking = new MaintenanceTrackingData(trackingJson);
        v2MaintenanceTrackingDataRepository.save(Tracking);
    }

    /**
     * Generates work machines with running harja ids [1, 2,...count]
     * @param count Count of generated work machines
     * @return
     */
    public static List<Tyokone> createWorkMachines(final int count) {
        return IntStream.range(1, count+1).mapToObj(i -> createTyokone(i)).collect(toList());
    }

    /**
     * Creates WorkMachineTracking with a LineString observation
     * @param observationTime Time of observation (start and end is same)
     * @param observationCount how many observations (LineString lenght) to generate for every machine.
     * @param jobId Harja job id
     * @param workMachines Machines
     * @param tasks Performed tasks
     * @return
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithLineString(final ZonedDateTime observationTime,
                                                                                                 final int observationCount,
                                                                                                 final int jobId,
                                                                                                 final List<Tyokone> workMachines,
                                                                                                 final SuoritettavatTehtavat...tasks) {
        return createMaintenanceTracking(observationTime, observationCount, jobId, workMachines, true, tasks);
    }

    /**
     * Creates WorkMachineTracking with multiple a Point observations
     * @param observationTime Time of first observation. Every observation time after that is increased with one minute.
     * @param observationCount how many observations (Point observations) to generate for every machine.
     * @param jobId Harja job id
     * @param workMachines Machines
     * @param tasks Performed tasks
     * @return
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithPoints(final ZonedDateTime observationTime,
                                                                                             final int observationCount,
                                                                                             final int jobId,
                                                                                             final List<Tyokone> workMachines,
                                                                                             final SuoritettavatTehtavat...tasks) {
        return createMaintenanceTracking(observationTime, observationCount, jobId, workMachines, false, tasks);
    }

    /**
     *
     * @param observationTime Time of observation
     * @param observationCount how many observations to generate/workmachine. Every observation time is increased with one minute.
     * @param workMachines jobId and work machine pairs to generate data for
     * @return
     */
    private static TyokoneenseurannanKirjausRequestSchema createMaintenanceTracking(final ZonedDateTime observationTime,
                                                                                    final int observationCount,
                                                                                    final int jobId,
                                                                                    final List<Tyokone> workMachines,
                                                                                    final boolean lineString,
                                                                                    final SuoritettavatTehtavat...tasks) {
        final OtsikkoSchema otsikko = createOtsikko(observationTime);
        final List<Havainnot> havainnot =
            workMachines.stream().map(workMachine ->
                createHavainnot(observationTime, workMachine, jobId, observationCount, Arrays.stream(tasks).collect(toList()), lineString))
                .flatMap(Collection::stream)
                .collect(toList());

        return new TyokoneenseurannanKirjausRequestSchema(otsikko, havainnot);
    }

    private final static double ETRS_SCALE = 50000.0;
    /**
     * If lineString is true this returns one observation with lineString of size {observationCount}.
     * Else returns {observationCount} point observations
     */
    private static List<Havainnot> createHavainnot(final ZonedDateTime observationTime, final Tyokone workMachine, final int jobId,
                                                   final int observationCount, final List<SuoritettavatTehtavat> tasks, final boolean lineString) {

        // This sets speed < 50 km/h between points
        final double coordinateFactor = 500;//(RANGE_X_MAX_ETRS - RANGE_X_MIN_ETRS) / observationCount;

        if (lineString) {
            // LineString observation with {observationCount} points
            List<List<Object>> coordinates = IntStream.range(0, observationCount)
                .mapToObj(i -> Arrays.<Object>asList(RANGE_X_MIN_ETRS + i*coordinateFactor, RANGE_Y_MIN_ETRS + i*coordinateFactor)).collect(toList());

            final GeometriaSijaintiSchema sijainti =
                new GeometriaSijaintiSchema().withViivageometria(new ViivageometriasijaintiSchema().withCoordinates(coordinates));
            return Collections.singletonList(
                new Havainnot().withHavainto(
                    new Havainto()
                        .withHavaintoaika(observationTime)
                        .withTyokone(workMachine)
                        .withUrakkaid(jobId)
                        .withSijainti(sijainti)
                        .withSuoritettavatTehtavat(tasks)
                        .withSuunta(90.0)));
        } else {
            // {observationCount} amount of point observations
            return IntStream.range(0, observationCount).mapToObj(i ->
                new Havainnot().withHavainto(
                    new Havainto()
                        .withHavaintoaika(observationTime.plusMinutes(i))
                        .withTyokone(workMachine)
                        .withUrakkaid(jobId)
                        .withSijainti(
                            new GeometriaSijaintiSchema()
                                .withKoordinaatit(new KoordinaattisijaintiSchema(RANGE_X_MIN_ETRS + i * coordinateFactor, RANGE_Y_MIN_ETRS + i * coordinateFactor, 0.0)))
                        .withSuoritettavatTehtavat(tasks)
                        .withSuunta(90.0))).collect(toList());
        }
    }

    private static Tyokone createTyokone(final int id) {
        return new Tyokone(id, "Tyokone_" + id, "Scania");
    }

    private static OtsikkoSchema createOtsikko(final ZonedDateTime startTime) {
        return new OtsikkoSchema(
            new Lahettaja("Tievoima",
            new OrganisaatioSchema("Tie huolto Oy", "8561566-0")),
            new TunnisteSchema(123),
            startTime);
    }

    public String getFormatedTrackingJson(final String TrackingJsonPath) throws IOException {
        return jsonWriter.writeValueAsString(jsonReader.readValue(readResourceContent(TrackingJsonPath)));
    }

    public void checkValidJson(final String json) {
        // Test reading as object and then back to json
        try {
            jsonWriter.writeValueAsString(jsonReader.readValue(json));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public void saveTrackingAsJson(final String trackingJSon) throws JsonProcessingException {
        final TyokoneenseurannanKirjausRequestSchema tracking = jsonReader.readValue(trackingJSon);
        v2MaintenanceTrackingUpdateService.saveMaintenanceTrackingData(tracking);
    }

    public int handleUnhandledWorkMachineTrackings() {
        return v2MaintenanceTrackingUpdateService.handleUnhandledMaintenanceTrackingData(100);
    }

    public void flushAndClearSession() {
        entityManager.flush();
        entityManager.clear();
    }

    private String readResourceContent(final String resourcePattern) throws IOException {
        return FileUtils.readFileToString(resourceLoader.getResource(resourcePattern).getFile(), UTF_8);
    }

    public void saveTrackingData(final TyokoneenseurannanKirjausRequestSchema seuranta) throws JsonProcessingException {
        saveTrackingAsJson(jsonWriter.writeValueAsString(seuranta));
    }

    public String getFormatedTrackingJson(final TyokoneenseurannanKirjausRequestSchema seuranta) throws JsonProcessingException {
        return jsonWriter.writeValueAsString(seuranta);
    }

    public static Set<MaintenanceTrackingTask> getTaskSetWithIndex(final int enumIndex) {
        return new HashSet<>(asList(getTaskWithIndex(enumIndex)));
    }

    public static Set<MaintenanceTrackingTask> getTaskSetWithTasks(final MaintenanceTrackingTask...tasks) {
        return new HashSet<>(asList(tasks));
    }

    public static MaintenanceTrackingTask getTaskWithIndex(final int enumIndex) {
        return MaintenanceTrackingTask.values()[enumIndex];
    }
}
