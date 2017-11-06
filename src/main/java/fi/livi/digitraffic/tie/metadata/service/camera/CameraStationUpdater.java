package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.metadata.model.CollectionStatus.isPermanentlyDeletedKeruunTila;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;

@Service
public class CameraStationUpdater {
    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class);

    private final DataStatusService dataStatusService;
    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final CameraStationUpdateService cameraStationUpdateService;
    private final CameraPresetService cameraPresetService;

    @Autowired
    public CameraStationUpdater(final DataStatusService dataStatusService,
                                final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                final CameraStationUpdateService cameraStationUpdateService,
                                final CameraPresetService cameraPresetService) {
        this.dataStatusService = dataStatusService;
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.cameraStationUpdateService = cameraStationUpdateService;
        this.cameraPresetService = cameraPresetService;
    }

    public boolean updateCameras() {
        log.info("Update Cameras start");

        Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentos =
            lotjuCameraStationMetadataService.getLotjuIdToKameraAndEsiasentoMap();

        final boolean fixedRoadStations = cameraStationUpdateService.fixCameraPresetsWithMissingRoadStations();
        final boolean fixedPresets = cameraStationUpdateService.fixPresetsWithoutLotjuIds(lotjuIdToKameraAndEsiasentos);
        final boolean updatedCameras = updateCamerasAndPresets(lotjuIdToKameraAndEsiasentos);
        final boolean updated = fixedRoadStations || fixedPresets || updatedCameras;
        updateStaticDataStatus(updated);
        log.info("UpdateCameras end");
        return updated;
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        dataStatusService.updateStaticDataStatus(DataStatusService.StaticStatusType.CAMERA_PRESET, updateStaticDataStatus);
    }

    public boolean updateCamerasAndPresets(final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentos) {

        final Set<Long> presetsLotjuIdsNotToObsolete = new HashSet<>();
        int updated = 0;
        int inserted = 0;
        int invalidCount = 0;

        for (Pair<KameraVO, List<EsiasentoVO>> kameraAndEsiasento : lotjuIdToKameraAndEsiasentos.values()) {

            final KameraVO kamera = kameraAndEsiasento.getLeft();
            final List<EsiasentoVO> esiasentos = kameraAndEsiasento.getRight();

            if (validate(kamera)) {
                presetsLotjuIdsNotToObsolete.addAll(esiasentos.stream().map(EsiasentoVO::getId).collect(Collectors.toList()));
                try {
                    Pair<Integer, Integer> updatedObsoleted =
                        cameraStationUpdateService.updateOrInsert(kamera, esiasentos);
                    updated += updatedObsoleted.getLeft();
                    inserted += updatedObsoleted.getRight();
                } catch (Exception e) {
                    log.error("Update or insert {} failed", ToStringHelper.toString(kamera));
                    throw e;
                }

            } else {
                invalidCount++;
            }
        }

        if (invalidCount > 0) {
            log.error("invalidCount={} invalid Kameras from LOTJU", invalidCount);
        }

        // camera presets in database, but not in server
        long obsoletePresets = cameraPresetService.obsoletePresetsExcludingLotjuIds(presetsLotjuIdsNotToObsolete);

        long obsoletedRoadStations = cameraPresetService.obsoleteCameraRoadStationsWithoutPublishablePresets();
        long nonOsoletedRoadStations = cameraPresetService.nonObsoleteCameraRoadStationsWithPublishablePresets();

        log.info("obsoletedCameraPresetsCount={} CameraPresets that are not active", obsoletePresets);
        log.info("obsoletedRoadStationsCount={} Camera RoadStations without active presets", obsoletedRoadStations);
        log.info("nonObsoletedCameraRoadStationsCount={} Camera RoadStations with active presets", nonOsoletedRoadStations);
        log.info("updatedCameraPresetsCount={} CameraPresets", updated);
        log.info("insertedCameraPresetsCount={} CameraPresets", inserted);

        return obsoletePresets > 0 || obsoletedRoadStations > 0 || nonOsoletedRoadStations > 0 || updated > 0 || inserted > 0;
    }

    private boolean validate(final KameraVO kamera) {
        final boolean valid = kamera.getVanhaId() != null;
        if (!valid && !isPermanentlyDeletedKeruunTila(kamera.getKeruunTila())) {
            log.error("{} is invalid: has null vanhaId", ToStringHelper.toString(kamera));
        }
        return valid;
    }
}
