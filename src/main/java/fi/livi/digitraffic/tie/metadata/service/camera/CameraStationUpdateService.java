package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;

@Service
public class CameraStationUpdateService extends AbstractCameraStationAttributeUpdater {

    private final CameraPresetService cameraPresetService;
    private final WeatherStationService weatherStationService;

    @Autowired
    public CameraStationUpdateService(final CameraPresetService cameraPresetService,
                                      final RoadStationService roadStationService,
                                      final WeatherStationService weatherStationService) {
        super(roadStationService, LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class));
        this.cameraPresetService = cameraPresetService;
        this.weatherStationService = weatherStationService;
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
                log.info("Fixed {} missing RoadStation with exiting {}", cameraPreset, rs);
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

        log.info("Obsoleted {} camera presets", obsoleted);
        log.info("Fixed {} camera presets without lotjuId", updated);

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
                log.info("Created new CameraPreset {}{}", cp, (roadStationNew ? " and RoadStation " + rs : ""));
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
            log.warn("Update camera preset (id: {}, presetId: {}) cameraId from {} to {}",
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
            logWarnIf(!isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila()),
                "Update: CameraPresetId doesn't match old: {} vs new {}", to.getPresetId(), presetId);
        }
        to.setPresetId(presetId);

        to.setLotjuId(esiasentoFrom.getId());
        to.setObsolete(isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila()) ||
                       Objects.equals(isPublic(esiasentoFrom), false));
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
                logErrorIf(nearestWs == null && !isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila()),
                    "Could not set set nearest Weather Station for cameraPreset {}. Weather station with lotjuId {} not found.",
                    to.getPresetId(), tsaLotjuId);
                to.setNearestWeatherStation(nearestWs);
            }
        } else {
            to.setNearestWeatherStation(null);
        }

        // Update RoadStation
        return updateRoadStationAttributes(kameraFrom, to.getRoadStation()) ||
            hash != HashCodeBuilder.reflectionHashCode(to);
    }
}
