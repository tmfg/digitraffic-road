package fi.livi.digitraffic.tie.service.camera;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.model.CameraPreset;
import fi.livi.digitraffic.tie.model.CameraType;
import fi.livi.digitraffic.tie.model.CollectionStatus;
import fi.livi.digitraffic.tie.model.RoadStation;
import fi.livi.digitraffic.tie.model.RoadStationType;
import fi.livi.digitraffic.tie.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.service.roadweather.RoadWeatherStationService;
import fi.livi.digitraffic.tie.wsdl.kamera.Esiasento;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.KeruunTILA;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    private StaticDataStatusService staticDataStatusService;
    private final CameraClient cameraClient;

    private static EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

    @Autowired
    public CameraUpdater(final CameraPresetService cameraPresetService,
                         final RoadStationService roadStationService,
                         final RoadWeatherStationService roadWeatherStationService,
                         final StaticDataStatusService staticDataStatusService,
                         final CameraClient cameraClient) {
        this.cameraPresetService = cameraPresetService;
        this.roadStationService = roadStationService;
        this.roadWeatherStationService = roadWeatherStationService;
        this.staticDataStatusService = staticDataStatusService;
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

        List<CameraPreset> currentCameraPresetsWithOutRoadStation = cameraPresetService.finAllCameraPresetsWithOutRoadStation();

        for (CameraPreset cameraPreset : currentCameraPresetsWithOutRoadStation) {
            RoadStation rs = new RoadStation();
            rs.setType(RoadStationType.CAMERA);
            rs.setName("DUMMY");
            rs.setNaturalId(cameraPreset.getId() * -1);
            cameraPreset.setRoadStation(rs);
            rs.obsolete();
            roadStationService.save(rs);
            log.info("Fixed " + cameraPreset.toString() + " missing RoadStation");
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
        updateStaticDataStatus(updateStaticDataStatus);

        log.info("UpdateCameras end");
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.CAMERA_PRESET, updateStaticDataStatus);
    }

    private boolean updateCameras(Map<String, Pair<Kamera, Esiasento>> presetIdToKameraAndEsiasento,
                                  Map<String, CameraPreset> currentPresetIdToCameraPresets) {

        final List<CameraPreset> obsolete = new ArrayList<>(); // obsolete presets
        final List<Pair<Pair<Kamera, Esiasento>, CameraPreset>> update = new ArrayList<>(); // camera presets to update
        final List<Pair<Kamera, Esiasento>> insert = new ArrayList<>(); // new lam-stations

        int invalid = 0;
        for (final String presetId : presetIdToKameraAndEsiasento.keySet()) {
            Pair<Kamera, Esiasento> kameraEsiasentoPair = presetIdToKameraAndEsiasento.get(presetId);
            Kamera kamera = kameraEsiasentoPair.getLeft();

            if ( validate(kameraEsiasentoPair.getLeft()) ) {

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
            } else {
                invalid++;
            }
        }

        if (invalid > 0) {
            log.warn("Found " + invalid + " Kameras from LOTJU");
        }

        Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap = roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        List<RoadStation> cameraRoadStations = roadStationService.findByType(RoadStationType.CAMERA);
        Map<Long, RoadStation> naturalIdToRoadStationMap = new HashMap<>();
        for (RoadStation roadStation : cameraRoadStations) {
            naturalIdToRoadStationMap.put(roadStation.getNaturalId(), roadStation);
        }

        // camera presets in database, but not in server
        obsolete.addAll(currentPresetIdToCameraPresets.values());

        int obsoleted = obsoleteCameraPresets(obsolete);
        log.info("Osoleted " + obsoleted + " CameraPresets");

        int uptaded = updateCameraPresets(update, lotjuIdToRoadWeatherStationMap, naturalIdToRoadStationMap);
        log.info("Uptaded " + uptaded + " CameraPresets");

        final int inserted = insertCameraPresets(insert, lotjuIdToRoadWeatherStationMap, naturalIdToRoadStationMap);
        log.info("Inserted " + inserted + " CameraPresets");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " CameraPresets");
        }

        return obsoleted > 0 || inserted > 0;
    }

    private boolean validate(Kamera kamera) {
        boolean valid = kamera.getVanhaId() != null;
        if (!valid) {
            log.error(ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
        }
        return false;
    }

    private int updateCameraPresets(List<Pair<Pair<Kamera, Esiasento>, CameraPreset>> update,
                                    Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                    Map<Long, RoadStation> naturalIdToRoadStationMap) {


        int counter = 0;
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

            if ( updateCameraPresetAtributes(kamera, esiasento, lotjuIdToRoadWeatherStationMap, cameraPreset) ) {
                counter++;
            }
            if (cameraPreset.getRoadStation().getId() == null) {
                roadStationService.save(cameraPreset.getRoadStation());
                log.info("Created new RoadStation " + cameraPreset.getRoadStation().getId());
            }
        }
        return counter;
    }

    private int insertCameraPresets(List<Pair<Kamera, Esiasento>> insert,
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

            updateCameraPresetAtributes(kamera, esiasento, lotjuIdToRoadWeatherStationMap, cp);

            roadStationService.save(cp.getRoadStation());
            cameraPresetService.save(cp);
            log.info("Created new CameraPreset " + cp.getId() + (roadStationNew ? " and RoadStation " + rs.getId() : "") );
        }
        return insert.size();
    }

    private boolean updateCameraPresetAtributes(Kamera kameraFrom,
                                                Esiasento esiasentoFrom,
                                                Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                                CameraPreset to) {

        int hash = HashCodeBuilder.reflectionHashCode(to);

        String cameraId = convertVanhaIdToKameraId(kameraFrom.getVanhaId());
        String presetId = convertCameraIdToPresetId(cameraId, esiasentoFrom.getSuunta());

        if ( to.getCameraId() != null && !to.getCameraId().equals(cameraId) ) {
            log.warn("Update camera preset (id:" + to.getId() + ", presetId: " + to.getPresetId() + ") cameraId from " + to.getCameraId() + " to " + cameraId);
            log.debug("\nOld preset: " + ToStringBuilder.reflectionToString(to) +
                      "\nnew kamera: " + ToStringBuilder.reflectionToString(kameraFrom) +
                      "\nnew ea:     " + ToStringBuilder.reflectionToString(esiasentoFrom));
        }
        to.setCameraId(cameraId);

        if ( to.getPresetId() != null && !to.getPresetId().equals(presetId) ) {
            log.info("\nOld preset: " + ToStringBuilder.reflectionToString(to) +
                     "\nnew kamera: " + ToStringBuilder.reflectionToString(kameraFrom) +
                     "\nnew ea:     " + ToStringBuilder.reflectionToString(esiasentoFrom));
            log.error("Update: CameraPresetId doesn't match old: " + to.getPresetId() + " vs new " + presetId);
        } else {
            to.setPresetId(presetId);
        }

        to.setLotjuCameraId(kameraFrom.getId());
        to.setLotjuId(esiasentoFrom.getId());

        to.setPresetOrder(esiasentoFrom.getJarjestys());
        to.setPublicExternal(esiasentoFrom.isJulkinen());
        to.setInCollection(esiasentoFrom.isKeruussa());
        to.setCompression(esiasentoFrom.getKompressio());
        to.setDescription(esiasentoFrom.getKuvaus());
        to.setLotjuId(esiasentoFrom.getId());
        to.setNameOnDevice(esiasentoFrom.getNimiLaitteella());
        to.setPresetName2(esiasentoFrom.getNimiEsitys());
        to.setDefaultDirection(esiasentoFrom.isOletussuunta());
        to.setResolution(esiasentoFrom.getResoluutio());
        to.setDirection(esiasentoFrom.getSuunta());
        to.setDelay(esiasentoFrom.getViive());

        to.setCameraType(CameraType.convertFromKameraTyyppi(kameraFrom.getTyyppi()));

        Long tsaLotjuId = kameraFrom.getLahinTiesaaAsemaId();
        if (tsaLotjuId != null) {
            if (to.getNearestRoadWeatherStation() == null || !tsaLotjuId.equals(to.getLotjuId())) {
                RoadWeatherStation nearestRWS = lotjuIdToRoadWeatherStationMap.get(tsaLotjuId);
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

    private static boolean updateRoadStationAttributes(final RoadStation to, final Kamera from) {
        int hash = HashCodeBuilder.reflectionHashCode(to);
        to.setNaturalId(from.getVanhaId());
        to.setType(RoadStationType.LAM_STATION);
        to.setObsolete(false);
        to.setObsoleteDate(null);
        to.setName(from.getNimi());
        to.setNameFi(from.getNimiFi());
        to.setNameSv(from.getNimiSe());
        to.setNameEn(from.getNimiEn());
        to.setDescription(from.getKuvaus());
        to.setLatitude(from.getLatitudi());
        to.setLongitude(from.getLongitudi());
        to.setAltitude(from.getKorkeus());
        to.setRoadNumber(from.getTieosoite().getTienumero());
        to.setRoadPart(from.getTieosoite().getTieosa());
        to.setDistance(from.getTieosoite().getEtaisyysTieosanAlusta());
        to.setCollectionInterval(from.getKeruuVali());
        to.setCollectionStatus(CollectionStatus.convertKeruunTila(from.getKeruunTila()));
        to.setMunicipality(from.getKunta());
        to.setMunicipalityCode(from.getKuntaKoodi());
        to.setProvince(from.getMaakunta());
        to.setProvinceCode(from.getMaakuntaKoodi());
        return hash != HashCodeBuilder.reflectionHashCode(to);
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
