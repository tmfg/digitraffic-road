package fi.livi.digitraffic.tie.service.maintenance.v1;

import static fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask.BRUSHING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingObservationDataRepository;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingRepository;
import fi.livi.digitraffic.tie.dao.maintenance.MaintenanceTrackingWorkMachineRepository;
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
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.helper.GeometryConstants;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingObservationData;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingTask;
import fi.livi.digitraffic.tie.model.maintenance.MaintenanceTrackingWorkMachine;
import fi.livi.digitraffic.tie.service.maintenance.MaintenanceTrackingUpdateServiceV1;
import jakarta.persistence.EntityManager;

@Service
public class MaintenanceTrackingServiceTestHelperV1 {
    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingServiceTestHelperV1.class);

    private final MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1;
    private final MaintenanceTrackingRepository maintenanceTrackingRepository;
    private final MaintenanceTrackingObservationDataRepository maintenanceTrackingObservationDataRepository;
    private final MaintenanceTrackingWorkMachineRepository maintenanceTrackingWorkMachineRepository;
    private final ObjectReader jsonReaderForKirjaus;
    private final ObjectWriter jsonWriterForHavainto;
    private final EntityManager entityManager;
    private final ResourceLoader resourceLoader;
    private static double maxLineStringGapInKilometers;

    public final static Pair<Double, Double> RANGE_X = Pair.of(19.0, 32.0);
    public final static Pair<Double, Double> RANGE_Y = Pair.of(59.0, 72.0);

    public static final double RANGE_X_MIN = 19.0;
    public static final double RANGE_X_MAX = 32.0;
    public static final double RANGE_Y_MIN = 59.0;
    public static final double RANGE_Y_MAX = 72.0;

    public static final int RANGE_X_MIN_ETRS = 41086; // 41085.768094711006
    //public static final int RANGE_X_MAX_ETRS = 672275; // 672275.051072051
    public static final int RANGE_Y_MIN_ETRS = 6567583; // 6567582.7414794015
    //public static final int RANGE_Y_MAX_ETRS = 7996086; // 7996086.925158125

    private final ObjectReader jsonReaderForTrackingsArray;

    @Autowired
    public MaintenanceTrackingServiceTestHelperV1(final ObjectMapper objectMapper,
                                                  final MaintenanceTrackingUpdateServiceV1 maintenanceTrackingUpdateServiceV1,
                                                  final MaintenanceTrackingRepository maintenanceTrackingRepository,
                                                  final MaintenanceTrackingObservationDataRepository maintenanceTrackingObservationDataRepository,
                                                  final MaintenanceTrackingWorkMachineRepository maintenanceTrackingWorkMachineRepository,
                                                  final EntityManager entityManager,
                                                  final ResourceLoader resourceLoader,
                                                  @Value("${workmachine.tracking.distinct.linestring.observationgap.km}")
                                                  final double maxLineStringGapInKilometers) {

        this.maintenanceTrackingUpdateServiceV1 = maintenanceTrackingUpdateServiceV1;
        this.maintenanceTrackingRepository = maintenanceTrackingRepository;
        this.jsonReaderForKirjaus = objectMapper.readerFor(TyokoneenseurannanKirjausRequestSchema.class);
        this.jsonWriterForHavainto = objectMapper.writerFor(Havainto.class);
        this.jsonReaderForTrackingsArray = objectMapper.readerForArrayOf(TyokoneenseurannanKirjausRequestSchema.class);
        this.maintenanceTrackingObservationDataRepository = maintenanceTrackingObservationDataRepository;
        this.maintenanceTrackingWorkMachineRepository = maintenanceTrackingWorkMachineRepository;
        this.entityManager = entityManager;
        this.resourceLoader = resourceLoader;
        MaintenanceTrackingServiceTestHelperV1.maxLineStringGapInKilometers = maxLineStringGapInKilometers;
    }

    public void clearDb() {
        maintenanceTrackingRepository.deleteAllInBatch();
        maintenanceTrackingObservationDataRepository.deleteAllInBatch();
        maintenanceTrackingWorkMachineRepository.deleteAllInBatch();
    }

    public static List<List<Double>> createVerticalLineStringWGS84(final double x, final double minY, final double maxY) {
        final Point start = PostgisGeometryUtils.createPointWithZ(new Coordinate(x, minY));
        final Point end = PostgisGeometryUtils.createPointWithZ(new Coordinate(x, maxY));
        final double dist = PostgisGeometryUtils.distanceBetweenWGS84PointsInKm(start, end);
        final int minCountOfPoints = (int)Math.ceil((dist/maxLineStringGapInKilometers));
        double increment = (maxY-minY)/minCountOfPoints;

        return IntStream.range(1, minCountOfPoints+1)
            .mapToObj(i -> asList(x, minY + (i*increment)))
            .collect(Collectors.toList());
    }

    /**
     * Generates work machines with running harja ids [1, 2,...count]
     * @param count Count of generated work machines
     * @return Created workmachines
     */
    public static List<Tyokone> createWorkMachines(final int count) {
        log.info("Create {} workmachines", count);
        return IntStream.range(1, count+1).mapToObj(MaintenanceTrackingServiceTestHelperV1::createWorkmachine).collect(toList());
    }

    /**
     * Creates WorkMachineTracking with a LineString observation
     * @param observationTime Time of observation (start and end is same)
     * @param jobId Harja job id
     * @param workMachine Machines
     * @param coordinatesEtrs Coordinates in Etrs format x, y
     * @param tasks Performed tasks
     * @return Created tracking
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTracking(final Instant observationTime,
                                                                                   final int jobId,
                                                                                   final Tyokone workMachine,
                                                                                   final List<List<Double>> coordinatesEtrs,
                                                                                   final SuoritettavatTehtavat...tasks) {

        final List<List<Object>> coordinatesEtrsAsObjects
            = coordinatesEtrs.stream()
                .map(outer -> outer.stream().map(innner -> (Object)innner).collect(Collectors.toList()))
                .collect(Collectors.toList());

        final GeometriaSijaintiSchema sijainti = coordinatesEtrsAsObjects.size() == 1 ?
            new GeometriaSijaintiSchema().withKoordinaatit(new KoordinaattisijaintiSchema()
                .withX((double)coordinatesEtrsAsObjects.get(0).get(0))
                .withY((double)coordinatesEtrsAsObjects.get(0).get(1))) :
            new GeometriaSijaintiSchema().withViivageometria(new ViivageometriasijaintiSchema().withCoordinates(coordinatesEtrsAsObjects));
        final List<Havainnot> havainnot = singletonList(
            new Havainnot().withHavainto(
                new Havainto()
                    .withHavaintoaika(observationTime.atZone(ZoneOffset.UTC))
                    .withTyokone(workMachine)
                    .withUrakkaid(jobId)
                    .withSijainti(sijainti)
                    .withSuoritettavatTehtavat(Arrays.stream(tasks).collect(toList()))
                    .withSuunta(90.0)));

        final OtsikkoSchema otsikko = createOtsikko(observationTime);

        return new TyokoneenseurannanKirjausRequestSchema(otsikko, havainnot);
    }

    /**
     * Creates WorkMachineTracking with a LineString observation
     * @param observationTime Time of observation (start and end is same)
     * @param pointsPerObservation how many points (LineString length) to generate for every machine.
     * @param jobId Harja job id
     * @param machineCount How many machines to create
     * @param tasks Performed tasks
     * @return Created tracking
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithLineString(final Instant observationTime,
                                                                                                 final int pointsPerObservation,
                                                                                                 final int jobId,
                                                                                                 final int machineCount,
                                                                                                 final SuoritettavatTehtavat...tasks) {
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        return createMaintenanceTracking(observationTime, pointsPerObservation, jobId, 1, workMachines, true, tasks);
    }

    /**
     * Creates WorkMachineTracking with a LineString observation
     * @param observationTime Time of observation (start and end is same)
     * @param pointsPerObservation how many points (LineString lenght) to generate for every machine.
     * @param jobId Harja job id
     * @param workMachines Machines
     * @param tasks Performed tasks
     * @return Created tracking
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithLineString(final Instant observationTime,
                                                                                                 final int pointsPerObservation,
                                                                                                 final int jobId,
                                                                                                 final List<Tyokone> workMachines,
                                                                                                 final SuoritettavatTehtavat...tasks) {
        return createMaintenanceTracking(observationTime, pointsPerObservation, 1, jobId, workMachines, true, tasks);
    }

    /**
     * Creates WorkMachineTracking with a LineString observation
     * @param observationTime Time of observation (start and end is same)
     * @param observationCount How many observations (LineString lenght) to generate for every machine.
     * @param ordinal n th generation of observation (Aftert 1st the 2nd will continue lineary after 1st coordinates)
     * @param jobId Harja job id
     * @param workMachines Machines
     * @param tasks Performed tasks
     * @return Created tracking
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithLineString(final Instant observationTime,
                                                                                                 final int observationCount,
                                                                                                 final int ordinal,
                                                                                                 final int jobId,
                                                                                                 final List<Tyokone> workMachines,
                                                                                                 final SuoritettavatTehtavat...tasks) {
        return createMaintenanceTracking(observationTime, observationCount, ordinal, jobId, workMachines, true, tasks);
    }

    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithPoints(final Instant observationTime,
                                                                                             final int observationCount,
                                                                                             final int jobId,
                                                                                             final int machineCount,
                                                                                             final SuoritettavatTehtavat...tasks) {
        final List<Tyokone> workMachines = createWorkMachines(machineCount);
        return createMaintenanceTracking(observationTime, observationCount, 1, jobId, workMachines, false, tasks);
    }
    /**
     * Creates WorkMachineTracking with multiple a Point observations
     * @param observationTime Time of first observation. Every observation time after that is increased with one minute.
     * @param observationCount how many observations (Point observations) to generate for every machine.
     * @param jobId Harja job id
     * @param workMachines Machines
     * @param tasks Performed tasks
     * @return Created tracking
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithPoints(final Instant observationTime,
                                                                                             final int observationCount,
                                                                                             final int jobId,
                                                                                             final List<Tyokone> workMachines,
                                                                                             final SuoritettavatTehtavat...tasks) {
        return createMaintenanceTracking(observationTime, observationCount, 1, jobId, workMachines, false, tasks);
    }

    /**
     * Creates WorkMachineTracking with multiple a Point observations
     * @param observationTime Time of first observation. Every observation time after that is increased with one minute.
     * @param pointsCount how many observations (Point observations) to generate for every machine.
     * @param ordinal n th generation of observation (Aftert 1st the 2nd will continue lineary after 1st coordinates)
     * @param jobId Harja job id
     * @param workMachines Machines
     * @param tasks Performed tasks
     * @return Created tracking
     */
    public static TyokoneenseurannanKirjausRequestSchema createMaintenanceTrackingWithPoints(final Instant observationTime,
                                                                                             final int pointsCount,
                                                                                             final int ordinal,
                                                                                             final int jobId,
                                                                                             final List<Tyokone> workMachines,
                                                                                             final SuoritettavatTehtavat...tasks) {
        return createMaintenanceTracking(observationTime, pointsCount, ordinal, jobId, workMachines, false, tasks);
    }

    /**
     *
     * @param observationTime Time of observation
     * @param pointsCount how many points to generate/workmachine. Every observation time is increased with one minute.
     * @param workMachines jobId and work machine pairs to generate data for
     * @return Created tracking
     */
    private static TyokoneenseurannanKirjausRequestSchema createMaintenanceTracking(final Instant observationTime,
                                                                                    final int pointsCount,
                                                                                    final int observationOrdinal,
                                                                                    final int jobId,
                                                                                    final List<Tyokone> workMachines,
                                                                                    final boolean lineString,
                                                                                    final SuoritettavatTehtavat...tasks) {
        final OtsikkoSchema otsikko = createOtsikko(observationTime);
        final List<Havainnot> havainnot =
            workMachines.stream().map(workMachine ->
                createHavainnot(observationTime, workMachine, jobId, pointsCount, observationOrdinal, Arrays.stream(tasks).collect(toList()), lineString))
                .flatMap(Collection::stream)
                .collect(toList());

        return new TyokoneenseurannanKirjausRequestSchema(otsikko, havainnot);
    }

    /**
     * If lineString is true this returns one observation with lineString of size {observationCount}.
     * Else returns {observationCount} point observations
     */
    private static List<Havainnot> createHavainnot(final Instant observationTime, final Tyokone workMachine, final int jobId,
                                                   final int pointsCount, final int ordinal, final List<SuoritettavatTehtavat> tasks, final boolean lineString) {

        // This sets speed < 50 km/h and distance between points < 0,5 km
        final double coordinateFactor = 100;
        final int additionToCoordinates = (ordinal-1) * pointsCount;
        if (lineString) {

            // LineString observation with {pointsCount} points
            List<List<Object>> coordinates = IntStream.range(0, pointsCount)
                .mapToObj(i -> Arrays.<Object>asList(RANGE_X_MIN_ETRS + (i + additionToCoordinates) * coordinateFactor,
                                                RANGE_Y_MIN_ETRS + (i + additionToCoordinates) * coordinateFactor)).collect(toList());

            final GeometriaSijaintiSchema sijainti =
                new GeometriaSijaintiSchema().withViivageometria(new ViivageometriasijaintiSchema().withCoordinates(coordinates));
            return singletonList(
                new Havainnot().withHavainto(
                    new Havainto()
                        .withHavaintoaika(observationTime.atZone(ZoneOffset.UTC))
                        .withTyokone(workMachine)
                        .withUrakkaid(jobId)
                        .withSijainti(sijainti)
                        .withSuoritettavatTehtavat(tasks)
                        .withSuunta(90.0)));
        } else {
            // {observationCount} amount of point observations
            return IntStream.range(0, pointsCount).mapToObj(i ->
                new Havainnot().withHavainto(
                    new Havainto()
                        .withHavaintoaika(observationTime.atZone(ZoneOffset.UTC).plusMinutes(i))
                        .withTyokone(workMachine)
                        .withUrakkaid(jobId)
                        .withSijainti(
                            new GeometriaSijaintiSchema()
                                .withKoordinaatit(new KoordinaattisijaintiSchema(RANGE_X_MIN_ETRS + (i + additionToCoordinates) * coordinateFactor,
                                                                                RANGE_Y_MIN_ETRS + (i + additionToCoordinates) * coordinateFactor, 0.0)))
                        .withSuoritettavatTehtavat(tasks)
                        .withSuunta(90.0))).collect(toList());
        }
    }

    public static Tyokone createWorkmachine(final int id) {
        return new Tyokone(id, "Tyokone_" + id, "Scania");
    }

    private static OtsikkoSchema createOtsikko(final Instant startTime) {
        return new OtsikkoSchema(
            new Lahettaja("Tievoima",
            new OrganisaatioSchema("Tie huolto Oy", "8561566-0")),
            new TunnisteSchema(123),
            startTime.atZone(ZoneOffset.UTC));
    }

    public String getFormatedTrackingJson(final String trackingJsonPath) throws IOException {
        final String json = readResourceContent(trackingJsonPath);
        final JsonNode root = new ObjectMapper().readTree(json);
        return root.toPrettyString();
    }

    public int handleUnhandledWorkMachineObservations(final int maxToHandle) {
        final int handled = maintenanceTrackingUpdateServiceV1.handleUnhandledMaintenanceTrackingObservationData(maxToHandle);
        log.info("Handled {} unhandled MaintenanceTrackingObservationDatas", handled);
        return handled;
    }

    private String readResourceContent(final String resourcePattern) throws IOException {
        return FileUtils.readFileToString(resourceLoader.getResource(resourcePattern).getFile(), UTF_8);
    }

    String UPSERT_MAINTENANCE_TRACKING_OBSERVATION_DATA_SQL = """
            INSERT INTO MAINTENANCE_TRACKING_OBSERVATION_DATA(
                    id,
                    observation_time,
                    sending_time,
                    json,
                    harja_workmachine_id,
                    harja_contract_id,
                    sending_system,
                    status,
                    hash,
                    s3_uri
            )
            VALUES(
                    NEXTVAL('SEQ_MAINTENANCE_TRACKING_OBSERVATION_DATA'),
                    '${observationTime}',
                    '${sendingTime}',
                    '${json}',
                    ${harjaWorkmachineId},
                    ${harjaContractId},
                    '${sendingSystem}',
                    '${status}',
                    '${hash}',
                    '${s3Uri}'
            )""";


    public void saveTrackingDataAsObservations(final TyokoneenseurannanKirjausRequestSchema seuranta) throws JsonProcessingException {
        final String sendingSystem = seuranta.getOtsikko().getLahettaja().getJarjestelma();
        final Instant sendingTime = seuranta.getOtsikko().getLahetysaika().toInstant();

        for(final Havainnot havainnot : seuranta.getHavainnot()) {
            final Havainto h = havainnot.getHavainto();
            final String json = jsonWriterForHavainto.writeValueAsString(h);
            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("observationTime", h.getHavaintoaika().toInstant().toString());
            valuesMap.put("sendingTime", sendingTime.toString());
            valuesMap.put("json", json);
            valuesMap.put("harjaWorkmachineId", h.getTyokone().getId());
            valuesMap.put("harjaContractId", h.getUrakkaid());
            valuesMap.put("sendingSystem", sendingSystem);
            valuesMap.put("status", MaintenanceTrackingObservationData.Status.UNHANDLED.name());
            valuesMap.put("hash", json.hashCode());
            valuesMap.put("s3Uri", "plaa");

            final StringSubstitutor sub = new StringSubstitutor(valuesMap);
            String sql = sub.replace(UPSERT_MAINTENANCE_TRACKING_OBSERVATION_DATA_SQL);

            entityManager.createNativeQuery(sql).executeUpdate();
        }
        entityManager.flush();
    }

    public String getFormatedObservationJson(final Havainto seuranta) throws JsonProcessingException {
        return jsonWriterForHavainto.writeValueAsString(seuranta);
    }

    public static Set<MaintenanceTrackingTask> getTaskSetWithIndex(final int enumIndex) {
        return new HashSet<>(singletonList(getTaskWithIndex(enumIndex)));
    }

    public static Set<MaintenanceTrackingTask> getTaskSetWithTasks(final MaintenanceTrackingTask...tasks) {
        return new HashSet<>(asList(tasks));
    }

    public static MaintenanceTrackingTask getTaskWithIndex(final int enumIndex) {
        return MaintenanceTrackingTask.getByharjaEnumName(SuoritettavatTehtavat.values()[enumIndex].name());
    }

    public void saveTrackingFromResourceToDbAsObservations(final String path) throws IOException {
        final String json = getFormatedTrackingJson(path);
        final TyokoneenseurannanKirjausRequestSchema tracking = jsonReaderForKirjaus.readValue(json);
        saveTrackingDataAsObservations(tracking);
    }

    public void saveTrackingFromResourceToDbAsObservationsFromMultipleMessages(final String path) throws IOException {
        final String json = readResourceContent(path);
        final TyokoneenseurannanKirjausRequestSchema[] trackings = jsonReaderForTrackingsArray.readValue(json);
        for(TyokoneenseurannanKirjausRequestSchema tracking : trackings) {
            saveTrackingDataAsObservations(tracking);
        }
    }

    public static Instant getEndTime(final TyokoneenseurannanKirjausRequestSchema seuranta) {
        final List<Havainnot> havainnot = seuranta.getHavainnot();
        return havainnot.get(havainnot.size()-1).getHavainto().getHavaintoaika().toInstant();
    }

    public static Instant getStartTimeOneHourInPast() {
        return getTimeHoursInPast(1);
    }

    public static Instant getTimeHoursInPast(final int hours) {
        return DateHelper.getNowWithoutMillis().minus(hours, ChronoUnit.HOURS);
    }

    public static Instant getStartTimeOneDayInPast() {
        return DateHelper.getNowWithoutMillis().minus(1, ChronoUnit.DAYS);
    }

    public MaintenanceTrackingWorkMachine createAndSaveWorkMachine() {
        final MaintenanceTrackingWorkMachine wm =
            new MaintenanceTrackingWorkMachine(TestUtils.getRandomId(1, 100000), TestUtils.getRandomId(1, 100000), "TEST");
        maintenanceTrackingWorkMachineRepository.save(wm);
        return wm;
    }

    public void insertDomain(final String domain, final String source) {
        entityManager.createNativeQuery(
                "insert into maintenance_tracking_domain(name, source)\n" +
                "VALUES (:domain, :source)" +
                "on conflict (name) do nothing ")
            .setParameter("domain", domain)
            .setParameter("source", source)
            .executeUpdate();
        entityManager.flush();
    }

    public void deleteDomains(final String...domains) {
        entityManager.createNativeQuery(
                "delete from maintenance_tracking_domain where name in (:domains)")
            .setParameter("domains", Arrays.asList(domains))
            .executeUpdate();
        entityManager.flush();
    }

    public long insertTrackingForDomain(final String domain, final long workMachineId) {
        entityManager.flush();
        entityManager.createNativeQuery(
                "INSERT INTO maintenance_tracking(id, domain, last_point, geometry, work_machine_id, sending_system, sending_time, start_time, end_time, finished)\n" +
                "VALUES (nextval('SEQ_MAINTENANCE_TRACKING'), '" + domain +
                "', ST_PointFromText('POINT(20.0 64.0 0)', " + GeometryConstants.SRID + "), " +
                "ST_GeometryFromText('LINESTRING(20.10 64.10 0, 20.0 64.0 0)', " + GeometryConstants.SRID + "), " +
                workMachineId + ", 'dummy', now(), now(), now(), true)" )
            .executeUpdate();
        final Long id = (Long)entityManager.createNativeQuery(
                "select id " +
                "from road.public.maintenance_tracking " +
                "where domain = '" + domain + "' " +
                "order by id desc " +
                "limit 1").getSingleResult();
        entityManager.createNativeQuery(
                "INSERT INTO road.public.maintenance_tracking_task(maintenance_tracking_id, task)\n" +
                    "VALUES (" + id + ", '" + BRUSHING.name() + "')")
            .executeUpdate();
        return id;
    }

}