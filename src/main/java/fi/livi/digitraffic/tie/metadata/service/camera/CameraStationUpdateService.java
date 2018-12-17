package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._03._12.KameraVO;

@Service
public class CameraStationUpdateService extends AbstractCameraStationAttributeUpdater {

    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;
    private final WeatherStationService weatherStationService;
    private final EntityManager entityManager;

    @Autowired
    public CameraStationUpdateService(final CameraPresetService cameraPresetService,
                                      final RoadStationService roadStationService,
                                      final WeatherStationService weatherStationService,
                                      final EntityManager entityManager) {
        super(LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class));
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.weatherStationService = weatherStationService;
        this.entityManager = entityManager;
    }

    /**
     * Adds road stations for presets without road station
     */
    @Transactional
    public boolean fixCameraPresetsWithMissingRoadStations() {

        final List<CameraPreset> currentCameraPresetsWithoutRoadStation =
            cameraPresetService.findAllCameraPresetsWithoutRoadStation();

        if (currentCameraPresetsWithoutRoadStation.isEmpty()) {
            return false;
        }

        final Map<Long, RoadStation> cameraRoadStationseMappedByNaturalId =
            roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA_STATION);

        for (CameraPreset cameraPreset : currentCameraPresetsWithoutRoadStation) {
            // Convert presetId to naturalId because using cameraId is not reliable before first run
            final long naturalId = CameraHelper.convertPresetIdToVanhaId(cameraPreset.getPresetId());
            // Fix cameraId for all
            cameraPreset.setCameraId(CameraHelper.convertPresetIdToCameraId(cameraPreset.getPresetId()));

            RoadStation rs = cameraRoadStationseMappedByNaturalId.get(naturalId);

            if (rs != null) {
                cameraPreset.setRoadStation(rs);
                log.info("Fixed cameraPreset={} missing RoadStation with exiting {}", cameraPreset, rs);
            } else {
                rs = new RoadStation(RoadStationType.CAMERA_STATION);
                rs.setName("GENERATED");
                rs.setNaturalId(naturalId);
                cameraPreset.setRoadStation(rs);
                rs.obsolete();
                cameraRoadStationseMappedByNaturalId.put(naturalId, rs);
                log.info("Fixed {} missing RoadStation with new {}", cameraPreset, rs);
            }
            cameraPresetService.save(cameraPreset);
        }
        return !currentCameraPresetsWithoutRoadStation.isEmpty();
    }

    /**
     * Sets presets and road stations lotjuIds and obsoletes missing
     */
    @Transactional
    public boolean fixPresetsWithoutLotjuIds(final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasento) {

        // CameraPresets in db without lotjuId
        Map<String, List<CameraPreset>> cameraPresetsWithoutLotjuId = cameraPresetService.findWithoutLotjuIdMappedByCameraId();

        if (cameraPresetsWithoutLotjuId.isEmpty()) {
            return false;
        }

        // Kameras and esiasentos from lotju mapped by cameraId
        Map<String, Pair<KameraVO, List<EsiasentoVO>>> cameraIdToKameraEsiasentosPair =
            lotjuIdToKameraAndEsiasento.values().stream().collect(Collectors.toMap(
                p -> CameraHelper.convertVanhaIdToKameraId(p.getKey().getVanhaId()),
                Function.identity()));

        int updated = 0;
        int obsoleted = 0;

        for (Map.Entry<String, List<CameraPreset>> entry : cameraPresetsWithoutLotjuId.entrySet()) {
            String cameraId = entry.getKey();
            Pair<KameraVO, List<EsiasentoVO>> kameraPair = cameraIdToKameraEsiasentosPair.get(cameraId);

            if (kameraPair == null) {
                // If kamera not found in lotju -> obsolete all presets
                obsoleted += entry.getValue().stream().filter(CameraPreset::obsolete).count();
            } else {
                // loop camera's presets and set their lotjuIds
                for (CameraPreset cameraPreset : entry.getValue()) {
                    String direction = CameraHelper.getDirectionFromPresetId(cameraPreset.getPresetId());
                    // Find esiasento for preset
                    Optional<EsiasentoVO> found =
                        kameraPair.getValue().stream().filter(esiasento -> CameraHelper.leftPadDirection(esiasento.getSuunta()).equals(direction))
                            .findFirst();

                    if (found.isPresent()) {
                        String before = ReflectionToStringBuilder.toString(cameraPreset);
                        final long cameraLotjuId = kameraPair.getKey().getId();
                        cameraPreset.setCameraLotjuId(cameraLotjuId);
                        cameraPreset.getRoadStation().setLotjuId(cameraLotjuId);
                        cameraPreset.setLotjuId(found.get().getId());
                        updated++;
                        log.info("Updated CameraPreset lotju id:\n{} -> \n{}", before, ReflectionToStringBuilder.toString(cameraPreset));
                        // if esiasento is not found -> obsolete preset
                    } else if (cameraPreset.obsolete()) {
                        obsoleted++;
                    }
                }
            }
        }

        log.info("obsoletedCameraPresets={} camera presets", obsoleted);
        log.info("fixedCameraPresets={} camera presets without lotjuId", updated);

        return updated > 0 || obsoleted > 0;
    }

    /**
     *
     * @return Pair of updated and inserted count of presets
     */
    @Transactional
    public Pair<Integer, Integer> updateOrInsert(KameraVO kamera, List<EsiasentoVO> esiasentos) {
        Map<Long, CameraPreset> presets = cameraPresetService.findAllCameraPresetsByCameraLotjuIdMappedByPresetLotjuId(kamera.getId());
        int updated = 0;
        int inserted = 0;

        // DPO-567 and DPO-681: Obsolete all presets before upgrading. Preset's LotjuIds and directions might change once in a while
        // so we want to get rid of ghosts and overlapping presetIds.
        presets.values().stream().forEach(e -> e.obsolete());
        entityManager.flush();

        for (EsiasentoVO esiasento : esiasentos) {

            final CameraPreset cameraPreset = presets.remove(esiasento.getId());

            // Existing preset
            if (cameraPreset != null) {

                final int hash = HashCodeBuilder.reflectionHashCode(cameraPreset);
                final String before = cameraPreset.toString();

                RoadStation rs = cameraPreset.getRoadStation();
                if (rs == null) {
                    final long cameraNaturalId = kamera.getVanhaId().longValue();
                    rs = roadStationService.findByTypeAndNaturalId(RoadStationType.CAMERA_STATION, cameraNaturalId);
                    if (rs == null) {
                        rs = new RoadStation(RoadStationType.CAMERA_STATION);
                    }
                    cameraPreset.setRoadStation(rs);
                }
                setRoadAddressIfNotSet(rs);
                if (rs.getId() == null) {
                    roadStationService.save(rs);
                }

                log.debug("Updating camera preset " + cameraPreset);

                if ( updateCameraPresetAtributes(kamera, esiasento, cameraPreset) ||
                    hash != HashCodeBuilder.reflectionHashCode(cameraPreset) ) {
                    log.info("Updated CameraPreset:\n{} -> \n{}", before, cameraPreset.toString());
                    updated++;
                    cameraPresetService.save(cameraPreset);
                }

            } else { // New preset

                // Default setPublicInternal(true); external is read from esiasento
                final CameraPreset cp = new CameraPreset();
                cp.setPublicInternal(true);

                // Do not remove from map. because one roadstation can have multiple presets
                final long cameraNaturalId = kamera.getVanhaId().longValue();
                RoadStation rs = roadStationService.findByTypeAndNaturalId(RoadStationType.CAMERA_STATION, cameraNaturalId);
                boolean roadStationNew = false;
                if (rs == null) {
                    rs = new RoadStation(RoadStationType.CAMERA_STATION);
                    roadStationNew = true;
                }
                setRoadAddressIfNotSet(rs);
                cp.setRoadStation(rs);

                updateCameraPresetAtributes(kamera, esiasento, cp);

                cameraPresetService.save(cp);
                if (roadStationNew) {
                    log.info("Created new {} and RoadStation {}", cp, rs);
                } else {
                    log.info("Created new {}", cp);
                }
                inserted++;
            }
        }

        return Pair.of(updated, inserted);
    }

    private boolean updateCameraPresetAtributes(final KameraVO kameraFrom, final EsiasentoVO esiasentoFrom,
                                                final CameraPreset to) {

        final int hash = HashCodeBuilder.reflectionHashCode(to);
        final String cameraId = CameraHelper.convertVanhaIdToKameraId(kameraFrom.getVanhaId());
        final String presetId = CameraHelper.convertCameraIdToPresetId(cameraId, esiasentoFrom.getSuunta());

        if ( to.getCameraId() != null && !to.getCameraId().equals(cameraId) ) {
            log.warn("Update camera preset ( toId={}, toPresetId={} ) toCameraId={} cameraId={}",
                to.getId(), to.getPresetId(), to.getCameraId(), cameraId);
            log.debug("Old preset: {}", ToStringBuilder.reflectionToString(to));
            log.debug("New kamera: {}", ToStringBuilder.reflectionToString(kameraFrom));
            log.debug("New esiasento: {}", ToStringBuilder.reflectionToString(esiasentoFrom));
        }

        // Preset properties
        if ( to.getPresetId() != null && !to.getPresetId().equals(presetId) ) {
            log.info("Old preset: {}", ToStringBuilder.reflectionToString(to));
            log.info("New kamera: {}", ToStringBuilder.reflectionToString(kameraFrom));
            log.info("New esiasento: {}", ToStringBuilder.reflectionToString(esiasentoFrom));
            if (!isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila())) {
                log.warn("Update: CameraPresetId doesn't match toPresetId={} vs new presetId={}", to.getPresetId(), presetId);
            }
        }
        to.setPresetId(presetId);

        to.setLotjuId(esiasentoFrom.getId());

        if (isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila()) ||
            Objects.equals(isPublic(esiasentoFrom), false)) {
            to.obsolete();
        } else {
            to.unobsolete();
        }
        to.setPresetOrder(esiasentoFrom.getJarjestys());
        to.setPublicExternal(isPublic(esiasentoFrom));
        to.setInCollection(esiasentoFrom.isKeruussa());
        to.setCompression(esiasentoFrom.getKompressio());
        to.setLotjuId(esiasentoFrom.getId());
        to.setPresetName1(esiasentoFrom.getNimiEsitys());
        to.setPresetName2(esiasentoFrom.getNimiLaitteella());
        to.setDefaultDirection(esiasentoFrom.isOletussuunta());
        to.setResolution(esiasentoFrom.getResoluutio());
        to.setDirection(esiasentoFrom.getSuunta());

        // Camera properties
        to.setCameraId(cameraId);
        to.setCameraLotjuId(kameraFrom.getId());
        to.setCameraType(CameraType.convertFromKameraTyyppi(kameraFrom.getTyyppi()));

        // For legacy
        to.setRoadStationId(kameraFrom.getVanhaId().longValue());

        final Long tsaLotjuId = kameraFrom.getLahinTiesaaAsemaId();
        if (tsaLotjuId != null) {
            if (to.getNearestWeatherStation() == null || !tsaLotjuId.equals(to.getNearestWeatherStation().getLotjuId())) {
                final WeatherStation nearestWs = weatherStationService.findWeatherStationByLotjuId(tsaLotjuId);
                if(nearestWs == null && !isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila())) {
                    log.error("Could not set set nearest Weather Station for cameraPresetId={}. Weather station with lotjuId={} not found.",
                              to.getPresetId(), tsaLotjuId);
                }
                to.setNearestWeatherStation(nearestWs);
            }
        } else {
            to.setNearestWeatherStation(null);
        }

        // Update RoadStation
        try {
            return updateRoadStationAttributes(kameraFrom, to.getRoadStation()) ||
                hash != HashCodeBuilder.reflectionHashCode(to);
        } catch (Exception e) {
            log.error("method=updateCameraPresetAtributes : Updating roadstation nimiFi=\"{}\" lotjuId={} naturalId={} keruunTila={} failed",
                kameraFrom.getNimiFi(), kameraFrom.getId(), kameraFrom.getVanhaId(), kameraFrom.getKeruunTila());
            throw e;
        }
    }
}
