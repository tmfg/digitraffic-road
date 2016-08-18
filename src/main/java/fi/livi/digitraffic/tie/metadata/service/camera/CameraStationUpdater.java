package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.WeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.weather.WeatherStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;

@Service
public class CameraStationUpdater extends AbstractCameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationUpdater.class);

    private final CameraPresetService cameraPresetService;
    private final WeatherStationService weatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuCameraClient lotjuCameraClient;

    @Autowired
    public CameraStationUpdater(final CameraPresetService cameraPresetService,
                                final RoadStationService roadStationService,
                                final WeatherStationService weatherStationService,
                                final StaticDataStatusService staticDataStatusService,
                                final LotjuCameraClient lotjuCameraClient) {
        super(roadStationService);
        this.cameraPresetService = cameraPresetService;
        this.weatherStationService = weatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuCameraClient = lotjuCameraClient;
    }

    @Transactional
    public boolean updateCameras() {
        log.info("Update Cameras start");

        if (lotjuCameraClient == null) {
            log.warn("Not updating cameraPresets metadatas because no lotjuCameraClient defined");
            return false;
        }

        final Map<String, Pair<KameraVO, EsiasentoVO>> presetIdToKameraAndEsiasento =
                lotjuCameraClient.getPresetIdToKameraAndEsiasentoMap();

        if (log.isDebugEnabled()) {
            log.debug("Fetched Cameras:");
            for (final Pair<KameraVO, EsiasentoVO> cameraPreset : presetIdToKameraAndEsiasento.values()) {
                log.info(ToStringBuilder.reflectionToString(cameraPreset.getLeft().getVanhaId()) + " : " + ToStringBuilder.reflectionToString(cameraPreset.getRight()));
            }
        }

        final boolean updateStaticDataStatus = updateCameras(presetIdToKameraAndEsiasento);
        updateStaticDataStatus(updateStaticDataStatus);
        log.info("UpdateCameras end");
        return updateStaticDataStatus;
    }

    @Transactional
    public boolean fixCameraPresetsWithMissingRoadStations() {

        final List<CameraPreset> currentCameraPresetsWithOutRoadStation =
                cameraPresetService.finAllCameraPresetsWithOutRoadStation();

        final Map<Long, RoadStation> cameraRoadStationseMappedByNaturalId =
                roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA_STATION);

        for (final CameraPreset cameraPreset : currentCameraPresetsWithOutRoadStation) {

            // Convert presetId to naturalId because using cameraId is not reliable before first run
            final long naturalId = convertPresetIdToVanhaId(cameraPreset.getPresetId());
            // Fix cameraId for all
            cameraPreset.setCameraId(convertPresetIdToCameraId(cameraPreset.getPresetId()));

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
        }
        return currentCameraPresetsWithOutRoadStation.size() > 0;
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.CAMERA_PRESET, updateStaticDataStatus);
    }

    private boolean updateCameras(final Map<String, Pair<KameraVO, EsiasentoVO>> presetIdToKameraAndEsiasento) {

        final Map<String, CameraPreset> presetsMappedByPresetId = cameraPresetService.finAllCamerasMappedByPresetId();

        final List<CameraPreset> obsolete = new ArrayList<>(); // obsolete presets
        List<RoadStation> obsoleteRoadStations = new ArrayList<>(); // obsolete presets
        final Set<Long> nonObsoleteRoadStations = new HashSet<>(); // obsolete presets
        final List<Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset>> update = new ArrayList<>(); // camera presets to update
        final List<Pair<KameraVO, EsiasentoVO>> insert = new ArrayList<>(); // new lam-stations

        int invalid = 0;
        for (final Map.Entry<String, Pair<KameraVO, EsiasentoVO>> presetIdEntrySet : presetIdToKameraAndEsiasento.entrySet()) {
            final Pair<KameraVO, EsiasentoVO> kameraEsiasentoPair = presetIdEntrySet.getValue();
            final EsiasentoVO esiasento = kameraEsiasentoPair.getRight();
            final KameraVO kamera = kameraEsiasentoPair.getLeft();

            if (validate(kamera) ) {

                final CameraPreset currentSaved = presetsMappedByPresetId.remove(presetIdEntrySet.getKey());

                if ( currentSaved != null
                     && ( CollectionStatus.isPermanentlyDeletedKeruunTila(kamera.getKeruunTila())
                        || Objects.equals(esiasento.isJulkinen(), false) ) ) {
                    // If station is not used or preset is not public -> obsolete preset
                    obsoleteRoadStations.add(currentSaved.getRoadStation());
                    obsolete.add(currentSaved);
                } else if (currentSaved != null) {
                    // Roadstation can have public and non public presets: gather here all that have one or more public presets
                    nonObsoleteRoadStations.add(currentSaved.getRoadStationId());
                    update.add(Pair.of(kameraEsiasentoPair, currentSaved));
                } else {
                    insert.add(kameraEsiasentoPair);
                }
            } else {
                invalid++;
            }
        }

        // Filter only obsolete stations that doesn't have a single public presets
        obsoleteRoadStations = obsoleteRoadStations.stream()
                .filter(p -> !nonObsoleteRoadStations.contains(p.getId()))
                .collect(Collectors.toList());

        if (invalid > 0) {
            log.error("Found " + invalid + " invalid Kameras from LOTJU");
        }

        final Map<Long, WeatherStation> lotjuIdToWeatherStationMap =
                weatherStationService.findAllWeatherStationsMappedByLotjuId();

        final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId =
                roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA_STATION);

        // camera presets in database, but not in server
        obsolete.addAll(presetsMappedByPresetId.values());

        final int obsoleted = obsoleteCameraPresets(obsolete);
        final int obsoletedRs = obsoleteRoadStations(obsoleteRoadStations);
        final int updated = updateCameraPresets(update, lotjuIdToWeatherStationMap, cameraRoadStationsMappedByNaturalId);
        final int inserted = insertCameraPresets(insert, lotjuIdToWeatherStationMap, cameraRoadStationsMappedByNaturalId);

        log.info("Obsoleted " + obsoleted + " CameraPresets");
        log.info("Obsoleted " + obsoletedRs + " RoadStations");
        log.info("Updated " + updated + " CameraPresets");
        log.info("Inserted " + inserted + " CameraPresets");

        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " CameraPresets");
        }

        return obsoleted > 0 || obsoletedRs > 0 || updated > 0 || inserted > 0;
    }

    private static boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid) {
            log.error(ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
        }
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
                log.info("Updated CameraPreset:\n" + before + " -> \n" + ReflectionToStringBuilder.toString(cameraPreset));
            }

            if (rs.getRoadAddress() != null && rs.getRoadAddress().getId() == null) {
                roadStationService.save(rs.getRoadAddress());
                log.info("Created new RoadAddress " + rs.getRoadAddress());
            }
            if (rs.getId() == null) {
                roadStationService.save(rs);
                log.info("Created new RoadStation " + rs);
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
            log.info("Created new CameraPreset " + cp + (roadStationNew ? " and RoadStation " + rs : "") );
        }
        return insert.size();
    }

    private static boolean updateCameraPresetAtributes(
            final KameraVO kameraFrom,
            final EsiasentoVO esiasentoFrom,
            final Map<Long, WeatherStation> lotjuIdToWeatherStationMap,
            final CameraPreset to) {

        final int hash = HashCodeBuilder.reflectionHashCode(to);

        final String cameraId = convertVanhaIdToKameraId(kameraFrom.getVanhaId());
        final String presetId = convertCameraIdToPresetId(cameraId, esiasentoFrom.getSuunta());

        if ( to.getCameraId() != null && !to.getCameraId().equals(cameraId) ) {
            log.warn("Update camera preset (id:" + to.getId() + ", presetId: " + to.getPresetId() + ") cameraId from " + to.getCameraId() + " to " + cameraId);
            log.debug("\nOld preset: " + ToStringBuilder.reflectionToString(to) +
                      "\nnew kamera: " + ToStringBuilder.reflectionToString(kameraFrom) +
                      "\nnew ea:     " + ToStringBuilder.reflectionToString(esiasentoFrom));
        }

        // Preset properties
        if ( to.getPresetId() != null && !to.getPresetId().equals(presetId) ) {
            log.info("\nOld preset: " + ToStringBuilder.reflectionToString(to) +
                     "\nnew kamera: " + ToStringBuilder.reflectionToString(kameraFrom) +
                     "\nnew ea:     " + ToStringBuilder.reflectionToString(esiasentoFrom));
            log.error("Update: CameraPresetId doesn't match old: " + to.getPresetId() + " vs new " + presetId);
        } else {
            to.setPresetId(presetId);
        }
        to.setLotjuId(esiasentoFrom.getId());
        to.setObsoleteDate(null);
        to.setPresetOrder(esiasentoFrom.getJarjestys());
        to.setPublicExternal(esiasentoFrom.isJulkinen());
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

        final Long tsaLotjuId = kameraFrom.getLahinTiesaaAsemaId();
        if (tsaLotjuId != null) {
            if (to.getNearestWeatherStation() == null || !tsaLotjuId.equals(to.getLotjuId())) {
                final WeatherStation nearestRWS = lotjuIdToWeatherStationMap.get(tsaLotjuId);
                if (nearestRWS == null) {
                    log.error("Could not set set nearest Weather Station for cameraPreset " + to.getPresetId() + ". Weather station with lotjuId " + tsaLotjuId + " not found.");
                }
                to.setNearestWeatherStation(nearestRWS);
            }
        } else {
            to.setNearestWeatherStation(null);
        }

        // Update RoadStation
        return updateRoadStationAttributes(kameraFrom, to.getRoadStation()) ||
                hash != HashCodeBuilder.reflectionHashCode(to);
    }



    private static int obsoleteCameraPresets(final List<CameraPreset> obsolete) {
        int counter = 0;
        for (final CameraPreset cameraPreset : obsolete) {
            if (cameraPreset.obsolete()) {
                log.debug("Obsolete CameraPreset id: " + cameraPreset.getId() + " naturalId: " + cameraPreset.getRoadStation().getNaturalId());
                counter++;
            }
        }
        return counter;
    }
}
