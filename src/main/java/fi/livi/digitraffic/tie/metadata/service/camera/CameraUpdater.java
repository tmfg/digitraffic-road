package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.KameraVO;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadAddress;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraClient;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationService;

@Service
public class CameraUpdater extends CameraRoadStationAttributeUpdater {
    private static final Logger log = Logger.getLogger(CameraUpdater.class);

    private final CameraPresetService cameraPresetService;
    private final RoadWeatherStationService roadWeatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final LotjuCameraClient lotjuCameraClient;

    @Autowired
    public CameraUpdater(final CameraPresetService cameraPresetService,
                                             final RoadStationService roadStationService,
                                             final RoadWeatherStationService roadWeatherStationService,
                                             final StaticDataStatusService staticDataStatusService,
                                             final LotjuCameraClient lotjuCameraClient) {
        super(roadStationService);
        this.cameraPresetService = cameraPresetService;
        this.roadWeatherStationService = roadWeatherStationService;
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuCameraClient = lotjuCameraClient;
    }

    @Transactional
    public void updateCameras() {
        log.info("Update Cameras start");

        if (lotjuCameraClient == null) {
            log.warn("Not updating cameraPresets metadatas because no lotjuCameraClient defined");
            return;
        }

        fixCameraPresetsWithMissingRoadStations();

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
    }

    private void fixCameraPresetsWithMissingRoadStations() {

        final List<CameraPreset> currentCameraPresetsWithOutRoadStation =
                cameraPresetService.finAllCameraPresetsWithOutRoadStation();

        Map<Long, RoadStation> cameraRoadStationseMappedByNaturalId =
                roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA);

        for (final CameraPreset cameraPreset : currentCameraPresetsWithOutRoadStation) {

            // Convert presetId to naturalId because using cameraId is not reliable before first run
            long naturalId = convertPresetIdToVanhaId(cameraPreset.getPresetId());
            // Fix cameraId for all
            cameraPreset.setCameraId(convertPresetIdToCameraId(cameraPreset.getPresetId()));

            RoadStation existingRs = cameraRoadStationseMappedByNaturalId.get(Long.valueOf(naturalId));

            if (existingRs != null) {
                cameraPreset.setRoadStation(existingRs);
                log.info("Fixed " + cameraPreset + " missing RoadStation with exiting " + existingRs);
            } else {
                final RoadStation rs = new RoadStation(RoadStationType.CAMERA);
                rs.setName("GENERATED");
                rs.setNaturalId(naturalId);
                cameraPreset.setRoadStation(rs);
                rs.obsolete();
                roadStationService.save(rs);
                cameraRoadStationseMappedByNaturalId.put(naturalId, rs);
                log.info("Fixed " + cameraPreset + " missing RoadStation with new " + rs);
            }
        }
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.CAMERA_PRESET, updateStaticDataStatus);
    }

    private boolean updateCameras(final Map<String, Pair<KameraVO, EsiasentoVO>> presetIdToKameraAndEsiasento) {

        final Map<String, CameraPreset> presetsMappedByPresetId = cameraPresetService.finAllCamerasMappedByPresetId();

        final List<CameraPreset> obsolete = new ArrayList<>(); // obsolete presets
        final List<RoadStation> obsoleteRoadStations = new ArrayList<>(); // obsolete presets
        final List<Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset>> update = new ArrayList<>(); // camera presets to update
        final List<Pair<KameraVO, EsiasentoVO>> insert = new ArrayList<>(); // new lam-stations

        int invalid = 0;
        for (final Map.Entry<String, Pair<KameraVO, EsiasentoVO>> presetIdEntrySet : presetIdToKameraAndEsiasento.entrySet()) {
            final Pair<KameraVO, EsiasentoVO> kameraEsiasentoPair = presetIdEntrySet.getValue();
            final KameraVO kamera = kameraEsiasentoPair.getLeft();

            if (validate(kamera) ) {

                final CameraPreset currentSaved = presetsMappedByPresetId.remove(presetIdEntrySet.getKey());

                if (currentSaved != null && CollectionStatus.isPermanentlyDeletedKeruunTila(kamera.getKeruunTila()) ) {
                    // If station is not used, obsolete also preset
                    obsoleteRoadStations.add(currentSaved.getRoadStation());
                    obsolete.add(currentSaved);
                } else if (currentSaved != null) {
                    update.add(Pair.of(kameraEsiasentoPair, currentSaved));
                } else {
                    insert.add(kameraEsiasentoPair);
                }
            } else {
                invalid++;
            }
        }

        if (invalid > 0) {
            log.error("Found " + invalid + " invalid Kameras from LOTJU");
        }

        final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId =
                roadStationService.findByTypeMappedByNaturalId(RoadStationType.CAMERA);

        // camera presets in database, but not in server
        obsolete.addAll(presetsMappedByPresetId.values());

        final int obsoleted = obsoleteCameraPresets(obsolete);
        log.info("Obsoleted " + obsoleted + " CameraPresets");

        final int obsoletedRs = obsoleteRoadStations(obsoleteRoadStations);
        log.info("Obsoleted " + obsoletedRs + " RoadStations");

        final int updated = updateCameraPresets(update, lotjuIdToRoadWeatherStationMap, cameraRoadStationsMappedByNaturalId);
        log.info("Updated " + updated + " CameraPresets");

        final int inserted = insertCameraPresets(insert, lotjuIdToRoadWeatherStationMap, cameraRoadStationsMappedByNaturalId);
        log.info("Inserted " + inserted + " CameraPresets");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " CameraPresets");
        }

        return obsoleted > 0 || obsoletedRs > 0 || inserted > 0;
    }

    private static boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid) {
            log.error(ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
        }
        return valid;
    }

    private int updateCameraPresets(final List<Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset>> update,
                                    final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                    final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId) {

        Map<Long, RoadAddress> roadAddressesMappedByLotjuId =
                roadStationService.findAllRoadAddressesMappedByLotjuId();

        int counter = 0;
        for (final Pair<Pair<KameraVO, EsiasentoVO>, CameraPreset> pair : update) {
            final Pair<KameraVO, EsiasentoVO> kameraEsiasentoPair = pair.getLeft();
            final KameraVO kamera = kameraEsiasentoPair.getLeft();
            final EsiasentoVO esiasento = kameraEsiasentoPair.getRight();
            final CameraPreset cameraPreset = pair.getRight();

            if (cameraPreset.getRoadStation() == null) {
                final long cameraNaturalId = kamera.getVanhaId().longValue();

                RoadStation rs = cameraRoadStationsMappedByNaturalId.get(cameraNaturalId);
                if (rs == null) {
                    rs = new RoadStation(RoadStationType.CAMERA);
                    cameraRoadStationsMappedByNaturalId.put(cameraNaturalId, rs);
                }
                cameraPreset.setRoadStation(rs);
            }

            cameraPreset.getRoadStation().setRoadAddress(resolveOrCreateRoadAddress(kamera, roadAddressesMappedByLotjuId));

            log.debug("Updating camera preset " + cameraPreset);

            if (updateCameraPresetAtributes(kamera, esiasento, lotjuIdToRoadWeatherStationMap, cameraPreset) ) {
                counter++;
            }
        }
        return counter;
    }

    private int insertCameraPresets(final List<Pair<KameraVO, EsiasentoVO>> insert,
                                    final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                    final Map<Long, RoadStation> cameraRoadStationsMappedByNaturalId) {

        Map<Long, RoadAddress> roadAddressesMappedByLotjuId =
                roadStationService.findAllRoadAddressesMappedByLotjuId();

        for (final Pair<KameraVO, EsiasentoVO> kameraEsiasentoPair : insert) {
            final KameraVO kamera = kameraEsiasentoPair.getLeft();
            final EsiasentoVO esiasento = kameraEsiasentoPair.getRight();


            // Default setPublicInternal(true); external is read from esiasento
            final CameraPreset cp = new CameraPreset();
            cp.setPublicInternal(true);

            // Do not remove from map. because one roadstation can have multiple presets
            long roadStationNaturalId = kamera.getVanhaId().longValue();
            RoadStation rs = cameraRoadStationsMappedByNaturalId.get( roadStationNaturalId );
            boolean roadStationNew = false;
            if (rs == null) {
                rs = new RoadStation(RoadStationType.CAMERA);
                cameraRoadStationsMappedByNaturalId.put(roadStationNaturalId, rs);
                roadStationNew = true;
            }
            cp.setRoadStation(rs);

            cp.getRoadStation().setRoadAddress(resolveOrCreateRoadAddress(kamera, roadAddressesMappedByLotjuId));

            updateCameraPresetAtributes(kamera, esiasento, lotjuIdToRoadWeatherStationMap, cp);

            // Save only transient object
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
            final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
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
            if (to.getNearestRoadWeatherStation() == null || !tsaLotjuId.equals(to.getLotjuId())) {
                final RoadWeatherStation nearestRWS = lotjuIdToRoadWeatherStationMap.get(tsaLotjuId);
                if (nearestRWS == null) {
                    log.error("Could not set set nearest road weather station for cameraPreset " + to.getPresetId() + ". Weather station with lotjuId " + tsaLotjuId + " not found.");
                }
                to.setNearestRoadWeatherStation(nearestRWS);
            }
        } else {
            to.setNearestRoadWeatherStation(null);
        }

        // Update RoadStation
        return updateRoadStationAttributes(to.getRoadStation(), kameraFrom) ||
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

    private static int obsoleteRoadStations(List<RoadStation> obsoleteRoadStations) {
        int counter = 0;
        for (final RoadStation rs : obsoleteRoadStations) {
            if (rs.obsolete()) {
                log.debug("Obsolete " + rs);
                counter++;
            }
        }
        return counter;
    }

    public static long convertCameraIdToVanhaId(final String cameraId) {
        // Starts either C0 or C
        String vanhaId = StringUtils.removeStart(cameraId, "C0");
        vanhaId = StringUtils.removeStart(vanhaId, "C");
        return Long.parseLong(vanhaId);
    }

    public static String convertVanhaIdToKameraId(final Integer vanhaId) {
        final String vanha = vanhaId.toString();
        final String cameraId = StringUtils.leftPad(vanha, 6, "C00000");
        return cameraId;
    }

    public static String convertCameraIdToPresetId(final String cameraId, final String suunta) {
        final String presetId = cameraId + StringUtils.leftPad(suunta, 2, "00");
        return presetId;
    }

    public static String convertPresetIdToCameraId(final String presetId) {
        return presetId.substring(0, 6);
    }

    public static long convertPresetIdToVanhaId(final String presetId) {
        String cameraId = convertPresetIdToCameraId(presetId);
        cameraId = StringUtils.removeStart(cameraId, "C0");
        return Long.parseLong(StringUtils.removeStart(cameraId, "C"));
    }
}
