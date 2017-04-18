package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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
import fi.livi.digitraffic.tie.helper.ToStringHelper;
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

        final Map<Long, RoadStation> cameraRoadStationseMappedByNaturalId =
                roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA_STATION);

        currentCameraPresetsWithoutRoadStation.stream().forEach(cameraPreset -> {
            // Convert presetId to naturalId because using cameraId is not reliable before first run
            final long naturalId = CameraHelper.convertPresetIdToVanhaId(cameraPreset.getPresetId());
            // Fix cameraId for all
            cameraPreset.setCameraId(CameraHelper.convertPresetIdToCameraId(cameraPreset.getPresetId()));

            final RoadStation existingRs = cameraRoadStationseMappedByNaturalId.get(Long.valueOf(naturalId));

            if (existingRs != null) {
                cameraPreset.setRoadStation(existingRs);
                log.info("Fixed " + cameraPreset + " missing RoadStation with exiting " + existingRs);
            } else {
                final RoadStation rs = new RoadStation(RoadStationType.CAMERA_STATION);
                rs.setName("GENERATED");
                rs.setNaturalId(naturalId);
                cameraPreset.setRoadStation(rs);
                rs.obsolete();
                roadStationService.save(rs);
                cameraRoadStationseMappedByNaturalId.put(naturalId, rs);
                log.info("Fixed " + cameraPreset + " missing RoadStation with new " + rs);
            }
        });
        return !currentCameraPresetsWithoutRoadStation.isEmpty();
    }

    /**
     * Sets presets and road stations lotjuIds and obsoletes missing
     */
    @Transactional
    public boolean fixPresetsWithoutLotjuIds(final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasento) {

        // Kameras and esiasentos from lotju mapped by cameraId
        Map<String, Pair<KameraVO, List<EsiasentoVO>>> cameraIdToKameraEsiasentosPair =
                lotjuIdToKameraAndEsiasento.values().stream().collect(Collectors.toMap(
                        p -> CameraHelper.convertVanhaIdToKameraId(p.getKey().getVanhaId()),
                        Function.identity()));

        // CameraPresets in db without lotjuId
        Map<String, List<CameraPreset>> withoutLotjuIdMappedByCameraId = cameraPresetService.findWithoutLotjuIdMappedByCameraId();

        final AtomicInteger updated = new AtomicInteger();
        final AtomicInteger obsoleted = new AtomicInteger();

        withoutLotjuIdMappedByCameraId.entrySet().stream().forEach(entry -> {

            String cameraId = entry.getKey();
            Pair<KameraVO, List<EsiasentoVO>> kameraPair = cameraIdToKameraEsiasentosPair.get(cameraId);

            if (kameraPair == null) {
                // If kamera not found in lotju -> obsolete all presets
                entry.getValue().stream().forEach(CameraPreset::obsolete);
            } else {
                // loop camera's presets and set their lotjuIds
                entry.getValue().stream().forEach(cameraPreset -> {
                    String direction = CameraHelper.getDirectionFromPresetId(cameraPreset.getPresetId());
                    // Find esiasento for preset
                    Optional<EsiasentoVO> found =
                            kameraPair.getValue().stream().filter(esiasento -> CameraHelper.leftPadDirection(esiasento.getSuunta()).equals(direction))
                                    .findFirst();

                    if (found.isPresent()) {
                        String before = ReflectionToStringBuilder.toString(cameraPreset);
                        cameraPreset.setCameraLotjuId(kameraPair.getKey().getId());
                        cameraPreset.setLotjuId(found.get().getId());
                        updated.addAndGet(1);
                        log.info("Updated CameraPreset lotju id:\n{} -> \n{}", before, ReflectionToStringBuilder.toString(cameraPreset));
                    // if esiasento is not found -> obsolete preset
                    } else if (cameraPreset.obsolete()) {
                        obsoleted.addAndGet(1);
                    }
                });
            }
        });

        log.info("Obsoleted {} camera presets", obsoleted);
        log.info("Fixed {} camera presets without lotjuId", updated);

        return updated.get() > 0;
    }

    @Transactional
    public boolean updateCamerasAndPresets(final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentos) {

        final Map<Long, CameraPreset> presetsMappedByLotjuId = cameraPresetService.findAllCameraPresetsMappedByLotjuId();

        final List<Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset>> update = new ArrayList<>(); // camera presets to update
        final List<Pair<KameraVO, EsiasentoVO>> insert = new ArrayList<>(); // new camera presets

        final AtomicInteger invalid = new AtomicInteger();

        lotjuIdToKameraAndEsiasentos.values().stream().forEach(kameraEsiasentoPair -> {

            final KameraVO kamera = kameraEsiasentoPair.getLeft();

            if (validate(kamera)) {

                kameraEsiasentoPair.getRight().stream().forEach(esiasento -> {
                    final CameraPreset existingPreset = presetsMappedByLotjuId.remove(esiasento.getId());

                    if (existingPreset != null) {
                        update.add(Pair.of(Pair.of(kamera, esiasento), existingPreset));
                    } else {
                        insert.add(Pair.of(kamera, esiasento));
                    }
                });
            } else {
                invalid.addAndGet(1);
            }
        });

        if (invalid.get() > 0) {
            log.error("Found {} invalid Kameras from LOTJU", invalid);
        }

        // camera presets in database, but not in server
        long countObsoletePresets = presetsMappedByLotjuId.values().stream().filter(cp -> cp.obsolete()).count();

        final Map<Long, WeatherStation> lotjuIdToWeatherStationMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();

        final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId =
                roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA_STATION);

        final int updated = updateCameraPresets(update, lotjuIdToWeatherStationMap, cameraRoadStationsMappedByNaturalId);
        final int inserted = insertCameraPresets(insert, lotjuIdToWeatherStationMap, cameraRoadStationsMappedByNaturalId);

        AtomicInteger countObsoleteRs = new AtomicInteger();
        // Go through all camera presets' road stations and check for non obsolete presets.
        // If just one non obsolete preset exists set road station obsolete false.
        // If not found any non obsolete presets for road station then obsolete it.
        cameraPresetService.findAll().stream().filter(cp -> cp.getRoadStationId() != null).collect(Collectors.groupingBy(CameraPreset::getRoadStationId)).values().stream()
                .forEach(cpList -> {
                    Optional<CameraPreset> nonObsolete = cpList.stream().filter(cameraPreset -> !cameraPreset.isObsolete()).findFirst();
                    if (nonObsolete.isPresent()) {
                        nonObsolete.get().getRoadStation().setObsolete(false);
                    } else if (cpList.get(0).getRoadStation().obsolete()) {
                        countObsoleteRs.addAndGet(1);
                    }
                });

        log.info("Obsoleted {} CameraPresets not existing in LOTJU", countObsoletePresets);
        log.info("Obsoleted {} RoadStations without active presets", countObsoleteRs);
        log.info("Updated {} CameraPresets", updated);
        log.info("Inserted {} CameraPresets", inserted);

        if (insert.size() > inserted) {
            log.warn("Insert failed for {} CameraPresets", insert.size()-inserted);
        }

        return countObsoletePresets > 0 || countObsoleteRs.get() > 0 || updated > 0 || inserted > 0;
    }

    private boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        logErrorIf(!valid && !isPermanentlyDeletedKeruunTila(kamera.getKeruunTila()),
                   "{} is invalid: has null vanhaId",
                   ToStringHelper.toString(kamera));
        return valid;
    }

    private int updateCameraPresets(final List<Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset>> update,
                                    final Map<Long, WeatherStation> lotjuIdToWeatherStationMap,
                                    final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId) {

        int counter = 0;
        for (final Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset> pair : update) {
            final Pair<KameraVO, EsiasentoVO> kameraEsiasentoPair = pair.getLeft();
            final KameraVO kamera = kameraEsiasentoPair.getLeft();
            final EsiasentoVO esiasento = kameraEsiasentoPair.getRight();
            final CameraPreset cameraPreset = pair.getRight();

            final int hash = HashCodeBuilder.reflectionHashCode(cameraPreset);
            final String before = ReflectionToStringBuilder.toString(cameraPreset);

            RoadStation rs = cameraPreset.getRoadStation();
            if (rs == null) {
                final long cameraNaturalId = kamera.getVanhaId().longValue();

                rs = cameraRoadStationsMappedByNaturalId.get(cameraNaturalId);
                if (rs == null) {
                    rs = new RoadStation(RoadStationType.CAMERA_STATION);
                    cameraRoadStationsMappedByNaturalId.put(cameraNaturalId, rs);
                }
                cameraPreset.setRoadStation(rs);
            }

            setRoadAddressIfNotSet(rs);

            log.debug("Updating camera preset " + cameraPreset);

            if ( updateCameraPresetAtributes(kamera, esiasento, lotjuIdToWeatherStationMap, cameraPreset) ||
                 hash != HashCodeBuilder.reflectionHashCode(cameraPreset) ) {
                counter++;
                log.info("Updated CameraPreset:\n{} -> \n{}", before, ReflectionToStringBuilder.toString(cameraPreset));
            }

            if (rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress {}", rs.getRoadAddress());
            }
            if (rs.getId() == null) {
                roadStationService.save(rs);
                log.info("Created new RoadStation {}", rs);
            }
        }
        return counter;
    }

    private int insertCameraPresets(final List<Pair<KameraVO, EsiasentoVO>> insert,
                                    final Map<Long, WeatherStation> lotjuIdToWeatherStationMap,
                                    final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId) {

        for (final Pair<KameraVO, EsiasentoVO> kameraEsiasentoPair : insert) {
            final KameraVO kamera = kameraEsiasentoPair.getLeft();
            final EsiasentoVO esiasento = kameraEsiasentoPair.getRight();


            // Default setPublicInternal(true); external is read from esiasento
            final CameraPreset cp = new CameraPreset();
            cp.setPublicInternal(true);

            // Do not remove from map. because one roadstation can have multiple presets
            final long roadStationNaturalId = kamera.getVanhaId().longValue();
            RoadStation rs = cameraRoadStationsMappedByNaturalId.get( roadStationNaturalId );
            boolean roadStationNew = false;
            if (rs == null) {
                rs = new RoadStation(RoadStationType.CAMERA_STATION);
                cameraRoadStationsMappedByNaturalId.put(roadStationNaturalId, rs);
                roadStationNew = true;
            }
            cp.setRoadStation(rs);

            setRoadAddressIfNotSet(rs);

            updateCameraPresetAtributes(kamera, esiasento, lotjuIdToWeatherStationMap, cp);

            // Save only transient objects
            if (rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
            }
            if (cp.getRoadStation().getId() == null) {
                roadStationService.save(cp.getRoadStation());
            }
            cameraPresetService.save(cp);
            log.info("Created new CameraPreset {}{}", cp, (roadStationNew ? " and RoadStation " + rs : ""));
        }
        return insert.size();
    }

    private boolean updateCameraPresetAtributes(
            final KameraVO kameraFrom,
            final EsiasentoVO esiasentoFrom,
            final Map<Long, WeatherStation> lotjuIdToWeatherStationMap,
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
            if (to.getNearestWeatherStation() == null || !tsaLotjuId.equals(to.getLotjuId())) {
                final WeatherStation nearestRWS = lotjuIdToWeatherStationMap.get(tsaLotjuId);
                logErrorIf(nearestRWS == null && !isPermanentlyDeletedKeruunTila(kameraFrom.getKeruunTila()),
                          "Could not set set nearest Weather Station for cameraPreset {}. Weather station with lotjuId {} not found.",
                           to.getPresetId(), tsaLotjuId);
                to.setNearestWeatherStation(nearestRWS);
            }
        } else {
            to.setNearestWeatherStation(null);
        }

        // Update RoadStation
        return updateRoadStationAttributes(kameraFrom, to.getRoadStation()) ||
                hash != HashCodeBuilder.reflectionHashCode(to);
    }
}
