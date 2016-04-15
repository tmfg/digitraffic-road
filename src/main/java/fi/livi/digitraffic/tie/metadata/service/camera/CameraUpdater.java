package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
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
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraType;
import fi.livi.digitraffic.tie.metadata.model.CollectionStatus;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;
import fi.livi.digitraffic.tie.metadata.model.RoadStationType;
import fi.livi.digitraffic.tie.metadata.model.RoadWeatherStation;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationService;
import fi.livi.digitraffic.tie.wsdl.kamera.Esiasento;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.KeruunTILA;

@Service
public class CameraUpdater {
    private static final Logger log = Logger.getLogger(CameraUpdater.class);

    private final CameraPresetService cameraPresetService;
    private final RoadStationService roadStationService;
    private final RoadWeatherStationService roadWeatherStationService;
    private final StaticDataStatusService staticDataStatusService;
    private final CameraClient cameraClient;

    private static final EnumSet<KeruunTILA> POISTETUT = EnumSet.of(KeruunTILA.POISTETTU_PYSYVASTI, KeruunTILA.POISTETTU_TILAPAISESTI);

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

    @Transactional
    public void updateCameras() {
        log.info("Update Cameras start");

        if (cameraClient == null) {
            log.warn("Not updating cameraPresets metadatas because no cameraClient defined");
            return;
        }

        final List<CameraPreset> currentCameraPresetsWithOutRoadStation = cameraPresetService.finAllCameraPresetsWithOutRoadStation();


        List<RoadStation> orphanRoadStations = roadStationService.findOrphanCameraStationRoadStations();
        final Map<Long, RoadStation> fetchedNaturalIdToRoadStationMap = new HashMap<>();
        for (RoadStation orphanRoadStation : orphanRoadStations) {
            fetchedNaturalIdToRoadStationMap.put(orphanRoadStation.getNaturalId(), orphanRoadStation);
        }

        for (final CameraPreset cameraPreset : currentCameraPresetsWithOutRoadStation) {

            long naturalId = convertCameraIdToVanhaId(cameraPreset.getCameraId());
            RoadStation existingRs = null;

            if ( fetchedNaturalIdToRoadStationMap.containsKey(Long.valueOf(naturalId)) ) {
                existingRs = fetchedNaturalIdToRoadStationMap.get(Long.valueOf(naturalId));
            } else {
                existingRs = roadStationService.findByTypeAndNaturalId(RoadStationType.CAMERA, naturalId);
                fetchedNaturalIdToRoadStationMap.put(naturalId, existingRs);
            }

            if (existingRs != null) {
                cameraPreset.setRoadStation(existingRs);
                log.info("Fixed " + cameraPreset + " missing RoadStation with exiting " + existingRs);
            } else {
                final RoadStation rs = new RoadStation();
                rs.setType(RoadStationType.CAMERA);
                rs.setName("DUMMY");
                rs.setNaturalId(naturalId);
                cameraPreset.setRoadStation(rs);
                rs.obsolete();
                roadStationService.save(rs);
                fetchedNaturalIdToRoadStationMap.put(naturalId, rs);
                log.info("Fixed " + cameraPreset + " missing RoadStation with new " + rs);
            }
        }

        final Map<String, Pair<Kamera, Esiasento>> presetIdToKameraAndEsiasento =
                cameraClient.getPresetIdToKameraAndEsiasentoMap();

        if (log.isDebugEnabled()) {
            log.debug("Fetched Cameras:");
            for (final Pair<Kamera, Esiasento> cameraPreset : presetIdToKameraAndEsiasento.values()) {
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

    private boolean updateCameras(final Map<String, Pair<Kamera, Esiasento>> presetIdToKameraAndEsiasento,
                                  final Map<String, CameraPreset> currentPresetIdToCameraPresets) {

        final List<CameraPreset> obsolete = new ArrayList<>(); // obsolete presets
        final List<Pair<Pair<Kamera, Esiasento>, CameraPreset>> update = new ArrayList<>(); // camera presets to update
        final List<Pair<Kamera, Esiasento>> insert = new ArrayList<>(); // new lam-stations

        int invalid = 0;
        for (final Map.Entry<String, Pair<Kamera, Esiasento>> stringPairEntry : presetIdToKameraAndEsiasento.entrySet()) {
            final Pair<Kamera, Esiasento> kameraEsiasentoPair = stringPairEntry.getValue();
            final Kamera kamera = kameraEsiasentoPair.getLeft();

            if (validate(kameraEsiasentoPair.getLeft()) ) {

                final CameraPreset currentSaved = currentPresetIdToCameraPresets.remove(stringPairEntry.getKey());

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
            log.error("Found " + invalid + " invalid Kameras from LOTJU");
        }

        final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap =
                roadWeatherStationService.findAllRoadWeatherStationsMappedByLotjuId();

        final List<RoadStation> cameraRoadStations = roadStationService.findByType(RoadStationType.CAMERA);
        final Map<Long, RoadStation> naturalIdToRoadStationMap = new HashMap<>();
        for (final RoadStation roadStation : cameraRoadStations) {
            naturalIdToRoadStationMap.put(roadStation.getNaturalId(), roadStation);
        }

        // camera presets in database, but not in server
        obsolete.addAll(currentPresetIdToCameraPresets.values());

        final int obsoleted = obsoleteCameraPresets(obsolete);
        log.info("Obsoleted " + obsoleted + " CameraPresets");

        final int updated = updateCameraPresets(update, lotjuIdToRoadWeatherStationMap, naturalIdToRoadStationMap);
        log.info("Updated " + updated + " CameraPresets");

        final int inserted = insertCameraPresets(insert, lotjuIdToRoadWeatherStationMap, naturalIdToRoadStationMap);
        log.info("Inserted " + inserted + " CameraPresets");
        if (insert.size() > inserted) {
            log.warn("Insert failed for " + (insert.size()-inserted) + " CameraPresets");
        }

        return obsoleted > 0 || inserted > 0;
    }

    private static boolean validate(final Kamera kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid) {
            log.error(ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
        }
        return valid;
    }

    private int updateCameraPresets(final List<Pair<Pair<Kamera, Esiasento>, CameraPreset>> update,
                                    final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                    final Map<Long, RoadStation> naturalIdToRoadStationMap) {


        int counter = 0;
        for (final Pair<Pair<Kamera, Esiasento>, CameraPreset> pair : update) {
            final Pair<Kamera, Esiasento> kameraEsiasentoPair = pair.getLeft();
            final Kamera kamera = kameraEsiasentoPair.getLeft();
            final Esiasento esiasento = kameraEsiasentoPair.getRight();
            final CameraPreset cameraPreset = pair.getRight();

            final Integer naturalId = kamera.getVanhaId();
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

            log.debug("Updating camera preset " + cameraPreset);

            if (updateCameraPresetAtributes(kamera, esiasento, lotjuIdToRoadWeatherStationMap, cameraPreset) ) {
                counter++;
            }
            if (cameraPreset.getRoadStation().getId() == null) {
                roadStationService.save(cameraPreset.getRoadStation());
                log.info("Created new RoadStation " + cameraPreset.getRoadStation());
            }
        }
        return counter;
    }

    private int insertCameraPresets(final List<Pair<Kamera, Esiasento>> insert,
                                    final Map<Long, RoadWeatherStation> lotjuIdToRoadWeatherStationMap,
                                    final Map<Long, RoadStation> naturalIdToRoadStationMap) {

        for (final Pair<Kamera, Esiasento> kameraEsiasentoPair : insert) {
            final Kamera kamera = kameraEsiasentoPair.getLeft();
            final Esiasento esiasento = kameraEsiasentoPair.getRight();


            // Default setPublicInternal(true); external is read from esiasento
            final CameraPreset cp = new CameraPreset();
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
            log.info("Created new CameraPreset " + cp + (roadStationNew ? " and RoadStation " + rs : "") );
        }
        return insert.size();
    }

    private static boolean updateCameraPresetAtributes(final Kamera kameraFrom, final Esiasento esiasentoFrom, final Map<Long,
            RoadWeatherStation> lotjuIdToRoadWeatherStationMap, final CameraPreset to) {

        final int hash = HashCodeBuilder.reflectionHashCode(to);

        final String cameraId = convertVanhaIdToKameraId(kameraFrom.getVanhaId());
        final String presetId = convertCameraIdToPresetId(cameraId, esiasentoFrom.getSuunta());

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
        to.setPresetName1(esiasentoFrom.getNimiEsitys());
        to.setPresetName2(esiasentoFrom.getNimiLaitteella());
        to.setNameOnDevice(esiasentoFrom.getNimiLaitteella());
        to.setDefaultDirection(esiasentoFrom.isOletussuunta());
        to.setResolution(esiasentoFrom.getResoluutio());
        to.setDirection(esiasentoFrom.getSuunta());
        to.setDelay(esiasentoFrom.getViive());

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

    private static boolean updateRoadStationAttributes(final RoadStation to, final Kamera from) {
        final int hash = HashCodeBuilder.reflectionHashCode(to);

        // Can insert obsolete stations
        if (POISTETUT.contains(from.getKeruunTila())) {
            to.obsolete();
        } else {
            to.setObsolete(false);
            to.setObsoleteDate(null);
        }

        to.setNaturalId(from.getVanhaId());
        to.setType(RoadStationType.CAMERA);
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

    public static long convertCameraIdToVanhaId(final String cameraId) {
        // Starts either C0 or C
        String vanhaId = StringUtils.removeStart(cameraId, "C0");
        vanhaId = StringUtils.removeStart(vanhaId, "C");
        return Long.parseLong(vanhaId);
    }

    public static String convertVanhaIdToKameraId(final Integer vanhaId) {
        final String vanha = vanhaId.toString();
        final String cameraId = StringUtils.leftPad(vanha, 6, "C00000");
        log.debug("vanhaId " + vanhaId + " -> " + cameraId);
        return cameraId;
    }

    public static String convertCameraIdToPresetId(final String cameraId, final String suunta) {
        final String presetId = cameraId + StringUtils.leftPad(suunta, 2, "00");
        log.debug("cameraId " + cameraId + " suunta " + suunta + " -> " + presetId);
        return presetId;
    }
}
