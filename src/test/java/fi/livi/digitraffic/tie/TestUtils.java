package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso.JULKINEN;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.UnexpectedRollbackException;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.Julkisuus;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KeruunTILA;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.roadstation.CollectionStatus;
import fi.livi.digitraffic.tie.model.roadstation.RoadAddress;
import fi.livi.digitraffic.tie.model.roadstation.RoadStation;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationState;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.model.tms.CalculatorDeviceType;
import fi.livi.digitraffic.tie.model.tms.TmsStation;
import fi.livi.digitraffic.tie.model.tms.TmsStationType;
import fi.livi.digitraffic.tie.model.weather.WeatherStation;
import fi.livi.digitraffic.tie.model.weather.WeatherStationType;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.model.weathercam.CameraType;
import jakarta.persistence.EntityManager;

public class TestUtils {

    public static final ResourceLoader resourceLoader = new DefaultResourceLoader();

    public static final int MIN_LOTJU_ID = 500;
    public static final int MAX_LOTJU_ID = 99999;
    public static final String PRESET_PRESENTATION_NAME = "PresentationName";

    private final static AtomicReference<Set<String>> reservedPresetIds = new AtomicReference<>();

    private final static HashSet<Integer> usedLotjuIds = new HashSet<>();

    static {
        reservedPresetIds.set(new HashSet<>());
    }

    public static Path getPath(final String filename) {
        return new File(Objects.requireNonNull(TestUtils.class.getResource(filename)).getFile()).toPath();
    }

    public static  List<Resource> loadResources(final String pattern) throws IOException {
        return Arrays.asList(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern));
    }

    public static Resource loadResource(final String pattern) {
        return resourceLoader.getResource(pattern);
    }

    public static String readResourceContent(final String resourcePattern) throws IOException {
        final Resource resource = loadResource(resourcePattern);
        return FileUtils.readFileToString(resource.getFile(), StandardCharsets.UTF_8);
    }

    public static CameraPreset generateDummyPreset() {
        return generateDummyPreset(generateDummyRoadStation(RoadStationType.CAMERA_STATION));
    }

    public static CameraPreset generateDummyPreset(final RoadStation rs) {

        final CameraPreset cp = new CameraPreset();
        cp.setRoadStation(rs);

        final String cameraId = CameraHelper.convertNaturalIdToCameraId(rs.getNaturalId());
        final String presetId = generateUniquePresetId(cameraId);


        cp.setCameraId(cameraId);
        cp.setPresetId(presetId);
        cp.setPresetName1("presentationName_" + cp.getPresetId());
        cp.setPublic(true);
        cp.setLotjuId(getRandomLotjuId());
        cp.setInCollection(true);
        cp.setCameraType(CameraType.VAPIX);
        cp.setCompression(50);
        cp.setDefaultDirection(true);
        cp.setResolution("1920x1080");
        cp.setDirection("1");
        cp.setCameraLotjuId(cp.getLotjuId());
        cp.setPictureLastModified(ZonedDateTime.now());

        return cp;
    }

    public static RoadStation generateDummyRoadStation(final RoadStationType roadStationType) {
        return generateDummyRoadStation(roadStationType, null);
    }

    public static RoadStation generateDummyRoadStation(final RoadStationType roadStationType, final String nameSuffix) {

        final RoadStation rs = RoadStation.createRoadStation(roadStationType);
        rs.setNaturalId(getRandomLotjuId());
        rs.setLotjuId(rs.getNaturalId());
        rs.setName(roadStationType.name());
        rs.updatePublicity(true);
        rs.setCollectionStatus(CollectionStatus.GATHERING);
        rs.setState(RoadStationState.OK);
        rs.setCollectionInterval(60);
        if (RoadStationType.WEATHER_STATION != roadStationType) {
            rs.setPurpose("Maisema");
        }
        rs.setLatitude(BigDecimal.valueOf(6687086));
        rs.setLongitude(BigDecimal.valueOf(351822));
        rs.setAltitude(BigDecimal.ZERO);
        rs.setMunicipality("Vihti");
        rs.setMunicipalityCode("927");
        rs.setProvince("Uusimaa");
        rs.setProvinceCode("1");
        rs.setCountry("Finland");
        rs.setLiviId(rs.getNaturalId().toString());
        final String name = roadStationType + getNameSuffix(nameSuffix);
        rs.setName(name);
        rs.setNameFi(name + "_fi");
        rs.setNameEn(name + "_en");
        rs.setNameSv(name + "_sv");
        rs.setStartDate(ZonedDateTime.now().minusDays(7));
        rs.setRepairMaintenanceDate(ZonedDateTime.now().minusDays(6));
        rs.setAnnualMaintenanceDate(ZonedDateTime.now().minusDays(10));

        final RoadAddress ra = generateDummyRoadAddres();
        rs.setRoadAddress(ra);

        return rs;
    }


    public static RoadAddress generateDummyRoadAddres() {
        final RoadAddress ra = new RoadAddress();
        ra.setCarriagewayCode(1);
        ra.setSideCode(1);
        ra.setContractArea("Uusimaa");
        ra.setContractAreaCode(1);
        ra.setDistanceFromRoadSectionStart(1000);
        ra.setRoadMaintenanceClass("1");
        ra.setRoadNumber(1);
        ra.setRoadSection(1);
        return ra;
    }

    public static Integer getRandomId(final int min, final int max) {
        assertTrue(max > min);
        final Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static int getRandom(final int minInclusive, final int maxExclusive) {
        final Random random = new Random();
        return random.ints(minInclusive, maxExclusive).findFirst().orElseThrow();
    }

    public static List<EsiasentoVO> createEsiasentos(final long kameraId, final int count) {
        final List<EsiasentoVO> eas = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> {
            final EsiasentoVO ea = new EsiasentoVO();
            ea.setId(getRandomLotjuId());
            ea.setKameraId(kameraId);
            ea.setKeruussa(true);
            ea.setJulkisuus(Julkisuus.JULKINEN);
            ea.setSuunta(StringUtils.leftPad(i+"", 2, '0'));
            ea.setNimiEsitys(PRESET_PRESENTATION_NAME + ea.getId());
            eas.add(ea);
        });
        return eas;
    }

    public static KameraVO createKamera(final Instant publicFrom) {
        final KameraVO k = new KameraVO();
        k.setVanhaId((int)getRandomLotjuId());
        k.setId(Long.valueOf(k.getVanhaId()));
        k.setNimi("Kamera-asema");
        k.setJulkisuus(createKameraJulkisuus(publicFrom, JULKINEN));
        k.setKeruunTila(KeruunTILA.KERUUSSA);

        k.setTieosoite(createKameraAsemanTieOsoite(k.getId()));

        return k;
    }

    public static TiesaaAsemaVO createTiesaaAsema(final long lotjuId) {
        final TiesaaAsemaVO tsa = new TiesaaAsemaVO();
        tsa.setId(lotjuId);
        tsa.setVanhaId(tsa.getId().intValue());
        tsa.setJulkinen(true);
        tsa.setNimi("Tiesääasema_" + lotjuId);
        tsa.setNimiFi(tsa.getNimi());
        tsa.setNimiEn(tsa.getNimi());
        tsa.setNimiSe(tsa.getNimi());
        tsa.setKeruunTila(fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.KeruunTILA.KERUUSSA);
        tsa.setJulkinen(true);
        tsa.setTieosoite(createTiesaaAsemanTieOsoite(lotjuId));
        return tsa;
    }

    public static LamAsemaVO createLamAsema(final long lotjuId) {
        final LamAsemaVO lam = new LamAsemaVO();
        lam.setId(lotjuId);
        lam.setVanhaId(lam.getId().intValue());
        lam.setJulkinen(true);
        lam.setNimi("Tiesääasema_" + lotjuId);
        lam.setNimiFi(lam.getNimi());
        lam.setNimiEn(lam.getNimi());
        lam.setNimiSe(lam.getNimi());
        lam.setKeruunTila(fi.livi.digitraffic.tie.external.lotju.metadata.lam.KeruunTILA.KERUUSSA);
        lam.setJulkinen(true);
        lam.setTieosoite(createLamAsemanTieOsoite(lotjuId));
        return lam;
    }

    private static fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TieosoiteVO createTiesaaAsemanTieOsoite(final long tieosoiteLotjuId) {
        final fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TieosoiteVO to =
            new fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TieosoiteVO();
        to.setId(tieosoiteLotjuId);
        to.setUrakkaAlue("Alue_" + tieosoiteLotjuId);
        to.setLuonut("Testi");
        to.setTienumero(getRandomId(0, 1000));
        to.setTieosa(to.getTienumero());
        to.setEtaisyysTieosanAlusta(to.getTienumero());
        return to;
    }

    private static fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TieosoiteVO createKameraAsemanTieOsoite(final long tieosoiteLotjuId) {
        final fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TieosoiteVO to =
            new fi.livi.digitraffic.tie.external.lotju.metadata.kamera.TieosoiteVO();
        to.setId(tieosoiteLotjuId);
        to.setUrakkaAlue("Alue_" + tieosoiteLotjuId);
        to.setLuonut("Testi");
        to.setTienumero(getRandomId(0, 1000));
        to.setTieosa(to.getTienumero());
        to.setEtaisyysTieosanAlusta(to.getTienumero());
        return to;
    }

    private static fi.livi.digitraffic.tie.external.lotju.metadata.lam.TieosoiteVO createLamAsemanTieOsoite(final long tieosoiteLotjuId) {
        final fi.livi.digitraffic.tie.external.lotju.metadata.lam.TieosoiteVO to =
            new fi.livi.digitraffic.tie.external.lotju.metadata.lam.TieosoiteVO();
        to.setId(tieosoiteLotjuId);
        to.setUrakkaAlue("Alue_" + tieosoiteLotjuId);
        to.setLuonut("Testi");
        to.setTienumero(getRandomId(0, 1000));
        to.setTieosa(to.getTienumero());
        to.setEtaisyysTieosanAlusta(to.getTienumero());
        return to;
    }

    public static Instant getInstant(final int secondsToAdd) {
        return Instant.now().plusSeconds(secondsToAdd).truncatedTo(ChronoUnit.SECONDS);
    }

    public static JulkisuusVO createKameraJulkisuus(final Instant from, final JulkisuusTaso julkisuusTaso) {
        final JulkisuusVO julkisuus = new JulkisuusVO();
        julkisuus.setJulkisuusTaso(julkisuusTaso);
        julkisuus.setAlkaen(DateHelper.toXMLGregorianCalendarAtUtc(from));
        return julkisuus;
    }

    public static long getRandomLotjuId() {
        while(true) {
            final Integer random = getRandomId(MIN_LOTJU_ID, MAX_LOTJU_ID);
            if (!usedLotjuIds.contains(random)) {
                usedLotjuIds.add(random);
                return random;
            }
        }
    }

    private static String generateUniquePresetId(final String cameraId) {
        final AtomicReference<String> presetId = new AtomicReference<>();
        while (presetId.get() == null || reservedPresetIds.get().stream().anyMatch(reserved -> reserved.equals(presetId.get()))) {
            presetId.set(CameraHelper.convertCameraIdToPresetId(cameraId, String.valueOf(RandomUtils.nextLong(0, 100))));
        }
        reservedPresetIds.get().add(presetId.get());
        return presetId.get();
    }

    public static List<TmsStation> generateDummyTmsStations(final int count) {
        return IntStream.range(0, count).mapToObj(i -> generateDummyTmsStation("" + i)).collect(Collectors.toList());
    }

    public static List<WeatherStation> generateDummyWeatherStations(final int count) {
        return IntStream.range(0, count).mapToObj(i -> generateDummyWeatherStation("" + i)).collect(Collectors.toList());
    }

    public static WeatherStation generateDummyWeatherStation() {
        return generateDummyWeatherStation(null);
    }

    public static WeatherStation generateDummyWeatherStation(final String nameSuffix) {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.WEATHER_STATION, nameSuffix);
        final WeatherStation ws = new WeatherStation();
        ws.setRoadStation(rs);
        ws.setLotjuId(rs.getLotjuId());
        ws.setWeatherStationType(WeatherStationType.E_18);
        ws.setMaster(true);
        return ws;
    }

    public static TmsStation generateDummyTmsStation() {
        return generateDummyTmsStation(null);
    }

    public static TmsStation generateDummyTmsStation(final String nameSuffix) {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.TMS_STATION, nameSuffix);

        final TmsStation ts = new TmsStation();
        ts.setRoadStation(rs);
        ts.setLotjuId(rs.getLotjuId());
        ts.setNaturalId(rs.getLotjuId());
        ts.setCalculatorDeviceType(CalculatorDeviceType.DSL_5);
        ts.setName("TMS_" + getNameSuffix(nameSuffix));
        ts.setDirection1Municipality("Vihti");
        ts.setDirection1MunicipalityCode(927);
        ts.setDirection2Municipality("Helsinki");
        ts.setDirection2MunicipalityCode(91);
        ts.setTmsStationType(TmsStationType.DSL_6);

        return ts;
    }

    private static String getNameSuffix(final String nameSuffix) {
        return nameSuffix != null ? "_" + nameSuffix : "";
    }

    public static LamAnturiVakioVO createLamAnturiVakio(final Long stationlotjuId, final String name) {
        final LamAnturiVakioVO vakio1 = new LamAnturiVakioVO();
        vakio1.setNimi(name);
        vakio1.setAsemaId(stationlotjuId);
        vakio1.setId(TestUtils.getRandomLotjuId());
        return vakio1;
    }

    public static LamAnturiVakioArvoVO createLamAnturiVakioArvo(final long anturiVakioLotjuId, final int voimassaAlku, final int voimassaLoppu, final int arvo) {
        final LamAnturiVakioArvoVO vapaaNopeus = new LamAnturiVakioArvoVO();
        vapaaNopeus.setAnturiVakioId(anturiVakioLotjuId);
        vapaaNopeus.setVoimassaAlku(voimassaAlku);
        vapaaNopeus.setVoimassaLoppu(voimassaLoppu);
        vapaaNopeus.setId(TestUtils.getRandomLotjuId());
        vapaaNopeus.setArvo(arvo);
        return vapaaNopeus;
    }

    public static List<CameraPreset> generateDummyCameraStation(final int presetCount) {
        final RoadStation rs = generateDummyRoadStation(RoadStationType.CAMERA_STATION);
        return IntStream
            .range(0,presetCount)
            .mapToObj(i -> generateDummyPreset(rs))
            .sorted(Comparator.comparing(CameraPreset::getPresetId))
            .collect(Collectors.toList());
    }

    public static List<List<CameraPreset>> generateDummyCameraStations(final int stationCount, final int presetCount) {
        return IntStream.range(0,stationCount).mapToObj(i -> generateDummyCameraStation(presetCount)).collect(Collectors.toList());
    }

    public static void truncateRoadStationsData(final EntityManager entityManager) {
        truncateCameraData(entityManager);
        truncateTmsData(entityManager);
        truncateWeatherData(entityManager);
    }

    public static void truncateCameraData(final EntityManager entityManager) {
        entityManager.createNativeQuery("ALTER TABLE camera_preset DISABLE TRIGGER camera_preset_prevent_delete_t").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM camera_preset_history WHERE created > '1970-01-01T00:00Z'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM camera_preset WHERE created > '1970-01-01T00:00Z'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station where road_station_type = 'CAMERA_STATION'").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE camera_preset ENABLE TRIGGER camera_preset_prevent_delete_t").executeUpdate();
        entityManager.flush();
    }

    public static void truncateTmsData(final EntityManager entityManager) {
        entityManager.createNativeQuery("ALTER TABLE tms_station DISABLE TRIGGER tms_station_prevent_delete_t").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM sensor_value WHERE road_station_id IN (SELECT id FROM road_station WHERE road_station_type = 'TMS_STATION')").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station_sensors WHERE road_station_id IN (SELECT id FROM road_station WHERE road_station_type = 'TMS_STATION')").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station_sensor WHERE lotju_id > 252 AND road_station_type = 'TMS_STATION'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tms_sensor_constant_value WHERE created > '1970-01-01T00:00Z'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tms_sensor_constant WHERE created > '1970-01-01T00:00Z'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM tms_station WHERE created > '1970-01-01T00:00Z'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station where road_station_type = 'TMS_STATION'").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE tms_station ENABLE TRIGGER tms_station_prevent_delete_t").executeUpdate();
        entityManager.flush();
    }

    public static void truncateWeatherData(final EntityManager entityManager) {
        entityManager.createNativeQuery("ALTER TABLE weather_station DISABLE TRIGGER weather_station_prevent_delete_t").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM sensor_value WHERE road_station_id IN (SELECT id FROM road_station WHERE road_station_type = 'WEATHER_STATION')").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station_sensors WHERE road_station_id IN (SELECT id FROM road_station WHERE road_station_type = 'WEATHER_STATION')").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station_sensor WHERE lotju_id >= " + MIN_LOTJU_ID + " AND road_station_type = 'WEATHER_STATION'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM weather_station WHERE created > '1970-01-01T00:00Z'").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM road_station where road_station_type = 'WEATHER_STATION'").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE weather_station ENABLE TRIGGER weather_station_prevent_delete_t").executeUpdate();
        entityManager.flush();
    }

    public static void truncateTrafficMessageData(final EntityManager entityManager) {
        entityManager.createNativeQuery("DELETE FROM datex2 WHERE id > 0").executeUpdate();
        entityManager.flush();
    }

    public static void flushCommitEndTransactionAndStartNew(final EntityManager entityManager) {
        entityManager.flush();
        entityManager.clear();
        commitAndEndTransactionAndStartNew();
    }

    public static void commitAndEndTransactionAndStartNew() {
        if (!TestTransaction.isActive()) {
            TestTransaction.start();
        } else {
            TestTransaction.flagForCommit();
            try {
                TestTransaction.end();
            } catch (final UnexpectedRollbackException e) {
                // Don't care as now transaction is rolled back and ended
                // This sometimes happens in test cleanup as the transaction is marked as roll back only and is ok
            }
            TestTransaction.start();
            TestTransaction.flagForCommit();
        }
    }

    public static void entityManagerFlushAndClear(final EntityManager entityManager) {
        entityManager.flush();
        entityManager.clear();
    }

    public static List<TiesaaLaskennallinenAnturiVO> createTiesaaLaskennallinenAnturis(final int count) {
        return IntStream.range(0,count).mapToObj(i -> TestUtils.createTiesaaLaskennallinenAnturi()).collect(Collectors.toList());
    }

    public static List<LamLaskennallinenAnturiVO> createLamLaskennallinenAnturis(final int count) {
        return IntStream.range(0,count).mapToObj(i -> TestUtils.createLamLaskennallinenAnturi()).collect(Collectors.toList());
    }

    public static TiesaaLaskennallinenAnturiVO createTiesaaLaskennallinenAnturi() {
        final TiesaaLaskennallinenAnturiVO anturi = new TiesaaLaskennallinenAnturiVO();
        anturi.setId(getRandomLotjuId());
        anturi.setJulkinen(true);
        anturi.setLyhytNimi("TSA_" + anturi.getId());
        anturi.setNimi("TiesaaLaskennallinenAnturi_" + anturi.getId());
        anturi.setVanhaId(anturi.getId().intValue());
        anturi.setEsitysnimiFi(anturi.getNimi() + "_fi");
        anturi.setEsitysnimiEn(anturi.getNimi() + "_en");
        anturi.setEsitysnimiSe(anturi.getNimi() + "_sv");
        anturi.setLaskentaKaava("1+1=3");
        anturi.setKuvausFi(anturi.getEsitysnimiFi());
        anturi.setKuvausEn(anturi.getEsitysnimiEn());
        anturi.setKuvausSe(anturi.getEsitysnimiSe());
        return anturi;
    }

    public static LamLaskennallinenAnturiVO createLamLaskennallinenAnturi() {
        final LamLaskennallinenAnturiVO anturi = new LamLaskennallinenAnturiVO();
        anturi.setId((long)getRandom(6003, 60000)); // in db there is natural-id's between 5016-6002 and 60000-60002
        anturi.setJulkinen(true);
        anturi.setLyhytNimi("TSA_" + anturi.getId());
        anturi.setNimi("TiesaaLaskennallinenAnturi_" + anturi.getId());
        anturi.setVanhaId(anturi.getId().intValue());
        anturi.setEsitysnimiFi(anturi.getNimi() + "_fi");
        anturi.setEsitysnimiEn(anturi.getNimi() + "_en");
        anturi.setEsitysnimiSe(anturi.getNimi() + "_sv");
        anturi.setLaskentaKaava("1+1=3");
        anturi.setKuvausFi(anturi.getEsitysnimiFi());
        anturi.setKuvausEn(anturi.getEsitysnimiEn());
        anturi.setKuvausSe(anturi.getEsitysnimiSe());
        return anturi;
    }

    public static void updateCameraHistoryModified(final String presetId, final String versionId, final Instant modified,
                                                   final EntityManager entityManager) {
        entityManager.createNativeQuery("ALTER TABLE camera_preset_history DISABLE TRIGGER camera_preset_history_modified_t").executeUpdate();
        entityManager.createNativeQuery(String.format("UPDATE camera_preset_history set modified = '%s' where preset_id = '%s' and version_id = '%s'",
                                                      modified.toString(), presetId, versionId)).executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE camera_preset_history ENABLE TRIGGER camera_preset_history_modified_t").executeUpdate();
        entityManager.flush();
    }
}
