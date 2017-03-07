package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;
import fi.livi.digitraffic.tie.metadata.service.lotju.LotjuCameraStationMetadataService;
import fi.livi.digitraffic.tie.metadata.service.roadstation.RoadStationService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;

@Service
public class CameraStationUpdater extends AbstractCameraStationAttributeUpdater {
    private static final Logger log = LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class);

    private final StaticDataStatusService staticDataStatusService;
    private final LotjuCameraStationMetadataService lotjuCameraStationMetadataService;
    private final CameraStationUpdateService cameraStationUpdateService;

    @Autowired
    public CameraStationUpdater(final RoadStationService roadStationService,
                                final StaticDataStatusService staticDataStatusService,
                                final LotjuCameraStationMetadataService lotjuCameraStationMetadataService,
                                final CameraStationUpdateService cameraStationUpdateService) {
        super(roadStationService, LoggerFactory.getLogger(AbstractCameraStationAttributeUpdater.class));
        this.staticDataStatusService = staticDataStatusService;
        this.lotjuCameraStationMetadataService = lotjuCameraStationMetadataService;
        this.cameraStationUpdateService = cameraStationUpdateService;
    }

    public boolean updateCameras() {
        log.info("Update Cameras start");

        if (!lotjuCameraStationMetadataService.isEnabled()) {
            log.warn("Not updating cameraPresets metadatas because LotjuCameraStationMetadataService not enabled");
            return false;
        }

        Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentos =
                lotjuCameraStationMetadataService.getLotjuIdToKameraAndEsiasentoMap();

        if (log.isDebugEnabled()) {
            log.debug("Fetched Cameras:");
            for (final Pair<KameraVO, List<EsiasentoVO>> cameraPreset : lotjuIdToKameraAndEsiasentos.values()) {
                log.info(ToStringBuilder.reflectionToString(cameraPreset.getLeft().getVanhaId()) + " : " + ToStringBuilder.reflectionToString(cameraPreset.getRight()));
            }
        }

        final boolean fixedRoadStations = cameraStationUpdateService.fixCameraPresetsWithMissingRoadStations();
        final boolean fixedPresets = cameraStationUpdateService.fixPresetsWithoutLotjuIds(lotjuIdToKameraAndEsiasentos);
        final boolean updatedCameras = cameraStationUpdateService.updateCamerasAndPresets(lotjuIdToKameraAndEsiasentos);
        updateStaticDataStatus(fixedRoadStations || fixedPresets || updatedCameras);
        log.info("UpdateCameras end");
        return fixedPresets || updatedCameras;
    }

    private void updateStaticDataStatus(final boolean updateStaticDataStatus) {
        staticDataStatusService.updateStaticDataStatus(StaticDataStatusService.StaticStatusType.CAMERA_PRESET, updateStaticDataStatus);
    }
}
