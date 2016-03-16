package fi.livi.digitraffic.tie.service.camera;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.model.CameraPreset;
import fi.livi.digitraffic.tie.model.CameraType;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.service.CameraPresetService;
import fi.livi.digitraffic.tie.service.RoadStationService;
import fi.livi.digitraffic.tie.service.RoadWeatherStationService;
import fi.livi.digitraffic.tie.wsdl.kamera.Esiasento;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.KeruunTILA;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraUpdater {

    private static final Logger log = Logger.getLogger(CameraUpdater.class);

    private CameraPresetService cameraPresetService;
    private RoadStationService roadStationService;
    private RoadWeatherStationService roadWeatherStationService;
    private final CameraClient cameraClient;

    private static EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    @Autowired
    public CameraUpdater(final CameraPresetService cameraPresetService,
                         final RoadStationService roadStationService,
                         final RoadWeatherStationService roadWeatherStationService,
                         final CameraClient cameraClient) {
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.roadWeatherStationService = roadWeatherStationService;
        this.cameraClient = cameraClient;
    }

    // 5 min
    @Scheduled(fixedRate = 5*60*1000)
    @Transactional
    public void updateLamStations() {
        log.info("UpdateCameras start");


        if (cameraClient == null) {
            log.warn("Not updating cameraPresets metadatas because no cameraClient defined");
            return;
        }

        final Map<String, Pair<Kamera, Esiasento>> presetIdToKameraAndEsiasento =
                cameraClient.getPresetIdToKameraAndEsiasentoMap();

        if (log.isDebugEnabled()) {
            log.debug("Fetched Cameras:");
            for (Pair<Kamera, Esiasento> cameraPreset : presetIdToKameraAndEsiasento.values()) {
                log.info(ToStringBuilder.reflectionToString(cameraPreset.getLeft().getVanhaId()) + " : " + ToStringBuilder.reflectionToString(cameraPreset.getRight()));
            }
        }

        final Map<String, CameraPreset> currentPresetIdToCameraPresets = cameraPresetService.finAllCamerasMappedByPresetId();

        final boolean updateStaticDataStatus = updateCameras(presetIdToKameraAndEsiasento, currentPresetIdToCameraPresets);
        //updateStaticDataStatus(updateStaticDataStatus);

        log.info("UpdateCameras end");
    }

    private boolean updateCameras(Map<String, Pair<Kamera, Esiasento>> presetIdToKameraAndEsiasento,
                                  Map<String, CameraPreset> currentPresetIdToCameraPresets) {

        final List<CameraPreset> obsolete = new ArrayList<>(); // obsolete presets
        final List<Pair<Pair<Kamera, Esiasento>, CameraPreset>> update = new ArrayList<>(); // camera presets to update
        final List<Pair<Kamera, Esiasento>> insert = new ArrayList<>(); // new lam-stations


        for (final String presetId : presetIdToKameraAndEsiasento.keySet()) {
            Pair<Kamera, Esiasento> kameraEsiasentoPair = presetIdToKameraAndEsiasento.get(presetId);
            Kamera kamera = kameraEsiasentoPair.getLeft();

            final CameraPreset currentSaved = currentPresetIdToCameraPresets.remove(presetId);

            if (currentSaved != null) {
                if (POISTETUT.contains(kamera.getKeruunTila())) {
                    obsolete.add(currentSaved);
                } else {
                    update.add(Pair.of(kameraEsiasentoPair, currentSaved));
                }
            } else {
                insert.add(kameraEsiasentoPair);
            }
        }

        Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap = roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        List<RoadStation> cameraRoadStations = roadStationService.findByType(RoadStationType.CAMERA);
        Map<Long, RoadStation> naturalIdToRoadStationMap = new HashMap<>();
        for (RoadStation roadStation : cameraRoadStations) {
            naturalIdToRoadStationMap.put(roadStation.getNaturalId(), roadStation);
        }

        // camera presets in database, but not in server
        obsolete.addAll(currentPresetIdToCameraPresets.values());

        obsoleteCameraPresets(obsolete);
        updateCameraPresets(update, lotjuIdToRoadWeatherStationMap, naturalIdToRoadStationMap);
        final boolean inserted = false;
//                insertCameraPresets(insert, lotjuIdToRoadWeatherStationMap, naturalIdToRoadStationMap);

        return !obsolete.isEmpty() || inserted;
    }

    private void updateCameraPresets(List<Pair<Pair<Kamera, Esiasento>, CameraPreset>> update,
                                     Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                     Map<Long, RoadStation> naturalIdToRoadStationMap) {



        for (final Pair<Pair<Kamera, Esiasento>, CameraPreset> pair : update) {

            Pair<Kamera, Esiasento> kameraEsiasentoPair = pair.getLeft();
            Kamera kamera = kameraEsiasentoPair.getLeft();
            Esiasento esiasento = kameraEsiasentoPair.getRight();
            CameraPreset cameraPreset = pair.getRight();

            Integer naturalId = kamera.getVanhaId();
            if (naturalId != null) {
                RoadStation rs = naturalIdToRoadStationMap.get(naturalId.longValue());
                if (rs == null) {
                    rs = new RoadStation();
                    naturalIdToRoadStationMap.put(naturalId.longValue(), rs);
                }
                cameraPreset.setRoadStation(rs);
            } else {
                cameraPreset.setRoadStation(null);
            }

            log.debug("Updating camera preset " + cameraPreset.getPresetId());

            updateCameraPresetAtributes(cameraPreset, kamera, esiasento, lotjuIdToRoadWeatherStationMap);
            if (cameraPreset.getRoadStation().getId() == null) {
                roadStationService.save(cameraPreset.getRoadStation());
                log.info("Created new RoadStation " + cameraPreset.getRoadStation().getId());
            }
        }
    }

    private boolean insertCameraPresets(List<Pair<Kamera, Esiasento>> insert,
                                        Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                        Map<Long, RoadStation> naturalIdToRoadStationMap) {

        for (Pair<Kamera, Esiasento> kameraEsiasentoPair : insert) {
            Kamera kamera = kameraEsiasentoPair.getLeft();
            Esiasento esiasento = kameraEsiasentoPair.getRight();


            // Default setPublicInternal(true); external is read from esiasento
            CameraPreset cp = new CameraPreset();
            cp.setPublicInternal(true);

            RoadStation rs = naturalIdToRoadStationMap.get(kamera.getVanhaId().longValue());
            boolean roadStationNew = false;
            if (rs == null) {
                rs = new RoadStation();
                naturalIdToRoadStationMap.put(kamera.getVanhaId().longValue(), rs);
                roadStationNew = true;
            }
            cp.setRoadStation(rs);

            updateCameraPresetAtributes(cp, kamera, esiasento, lotjuIdToRoadWeatherStationMap);

            roadStationService.save(cp.getRoadStation());
            cameraPresetService.save(cp);
            log.info("Created new CameraPreset " + cp.getId() + (roadStationNew ? " and RoadStation " + rs.getId() : "") );
        }


        return false;
    }

    private void updateCameraPresetAtributes(CameraPreset cameraPresetTo,
                                             Kamera kameraFrom,
                                             Esiasento esiasentoFrom,
                                             Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap ) {

        String cameraId = convertVanhaIdToKameraId(kameraFrom.getVanhaId());
        String presetId = convertCameraIdToPresetId(cameraId, esiasentoFrom.getSuunta());

        if ( cameraPresetTo.getCameraId() != null && !cameraPresetTo.getCameraId().equals(cameraId) ) {
            log.info("Update camera preset (id:" + cameraPresetTo.getId() + ", presetId: " + cameraPresetTo.getPresetId() + ") cameraId from " + cameraPresetTo.getCameraId() + " to " + cameraId);
            log.debug("\nOld preset: " + ToStringBuilder.reflectionToString(cameraPresetTo) +
                      "\nnew kamera: " + ToStringBuilder.reflectionToString(kameraFrom) +
                      "\nnew ea:     " + ToStringBuilder.reflectionToString(esiasentoFrom));
            cameraPresetTo.setCameraId(cameraId);
//            log.error("Update: CameraId doesn't match old: " + cameraPresetTo.getCameraId() + " vs new " + cameraId);

//            Update: CameraId doesn't match old: C0360 vs new C03609
//            Update: CameraId doesn't match old: C1456 vs new C14569
//            Update: CameraId doesn't match old: C1456 vs new C14569
//            log.error("Update: CameraId doesn't match old: " + cameraPresetTo.getCameraId() + " vs new " + cameraId + ", vanhaId: " + kameraFrom.getVanhaId());
        } else {
            cameraPresetTo.setCameraId(cameraId);
        }

        if ( cameraPresetTo.getPresetId() != null && !cameraPresetTo.getPresetId().equals(presetId) ) {
            log.info("\nOld preset: " + ToStringBuilder.reflectionToString(cameraPresetTo) +
                     "\nnew kamera: " + ToStringBuilder.reflectionToString(kameraFrom) +
                     "\nnew ea:     " + ToStringBuilder.reflectionToString(esiasentoFrom));
            log.error("Update: CameraPresetId doesn't match old: " + cameraPresetTo.getPresetId() + " vs new " + presetId);

        } else {
            cameraPresetTo.setPresetId(presetId);
        }

        cameraPresetTo.setLotjuCameraId(kameraFrom.getId());
        cameraPresetTo.setLotjuId(esiasentoFrom.getId());

        cameraPresetTo.setPresetOrder(esiasentoFrom.getJarjestys());
        cameraPresetTo.setPublicExternal(esiasentoFrom.isJulkinen());
        cameraPresetTo.setInCollection(esiasentoFrom.isKeruussa());
        cameraPresetTo.setCompression(esiasentoFrom.getKompressio());
        cameraPresetTo.setDescription(esiasentoFrom.getKuvaus());
        cameraPresetTo.setLotjuId(esiasentoFrom.getId());
        cameraPresetTo.setNameOnDevice(esiasentoFrom.getNimiLaitteella());
        cameraPresetTo.setPresetName2(esiasentoFrom.getNimiEsitys());
        cameraPresetTo.setDefaultDirection(esiasentoFrom.isOletussuunta());
        cameraPresetTo.setResolution(esiasentoFrom.getResoluutio());
        cameraPresetTo.setDirection(esiasentoFrom.getSuunta());
        cameraPresetTo.setDelay(esiasentoFrom.getViive());

        cameraPresetTo.setCameraType(CameraType.convertFromKameraTyyppi(kameraFrom.getTyyppi()));

        Long tsaLotjuId = kameraFrom.getLahinTiesaaAsemaId();
        if (tsaLotjuId != null) {
            if (cameraPresetTo.getNearestRoadWeatherStation() == null || !tsaLotjuId.equals(cameraPresetTo.getLotjuId())) {
                RoadWeatherStation nearestRWS = lotjuIdToRoadWeatherStationMap.get(tsaLotjuId);
                if (nearestRWS == null) {
                    log.error("Could not set set nearest road weather station for cameraPreset " + cameraPresetTo.getPresetId() + ". Weather station with lotjuId " + tsaLotjuId + " not found.");
                }
                cameraPresetTo.setNearestRoadWeatherStation(nearestRWS);
            }
        } else {
            cameraPresetTo.setNearestRoadWeatherStation(null);
        }

        // RoadStation
        updateRoadStationAttributes(cameraPresetTo.getRoadStation(), kameraFrom);
    }

    private static void updateRoadStationAttributes(final RoadStation rsTo, final Kamera kameraFrom) {
        rsTo.setNaturalId(kameraFrom.getVanhaId());
        rsTo.setObsolete(false);
        rsTo.setObsoleteDate(null);
        rsTo.setType(RoadStationType.CAMERA);
        rsTo.setName(kameraFrom.getNimi());
        rsTo.setNameFi(kameraFrom.getNimiFi());
        rsTo.setNameSe(kameraFrom.getNimiSe());
        rsTo.setNameEn(kameraFrom.getNimiEn());
        rsTo.setLatitude(kameraFrom.getLatitudi());
        rsTo.setLongitude(kameraFrom.getLongitudi());
        rsTo.setAltitude(kameraFrom.getKorkeus());
        rsTo.setRoadNumber(kameraFrom.getTieosoite().getTienumero());
        rsTo.setRoadPart(kameraFrom.getTieosoite().getTieosa());
        rsTo.setDistance(kameraFrom.getTieosoite().getEtaisyysTieosanAlusta());
    }

    private static void obsoleteCameraPresets(final List<CameraPreset> obsolete) {
        for (final CameraPreset cameraPreset : obsolete) {
            log.debug("Obsolete cameraPreset " + cameraPreset.getPresetId());
            cameraPreset.obsolete();
        }
    }

    public static String convertVanhaIdToKameraId(Integer vanhaId) {
        String vanha = vanhaId.toString();
        String cameraId = StringUtils.leftPad(vanha, 6, "C00000");
        log.debug("vanhaId " + vanhaId + " -> " + cameraId);
        return cameraId;
    }

    public static String convertCameraIdToPresetId(String cameraId, String suunta) {
        String presetId = cameraId + StringUtils.leftPad(suunta, 2, "00");
        log.debug("cameraId " + cameraId + " suunta " + suunta + " -> " + presetId);
        return presetId;
    }
}
