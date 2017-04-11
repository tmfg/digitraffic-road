package fi.livi.digitraffic.tie.data.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.lotju.xsd.kamera.Kuva;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraPresetService;

@Service
public class CameraDataUpdateService {
    private static final Logger log = LoggerFactory.getLogger(CameraDataUpdateService.class);

    private final CameraPresetService cameraPresetService;
    private final CameraImageUpdateService cameraImageUpdateService;

    @Autowired
    CameraDataUpdateService(final CameraPresetService cameraPresetService,
                            final CameraImageUpdateService cameraImageUpdateService) {
        this.cameraPresetService = cameraPresetService;
        this.cameraImageUpdateService = cameraImageUpdateService;
    }

    @Transactional
    public void updateCameraData(final List<Kuva> data) throws SQLException {

        final HashMap<Long, Kuva> latestKuvasMappedByPresetLotjuId = filterLatestKuvasAndMapByPresetId(data);

        final List<CameraPreset> cameraPresets = cameraPresetService.findPublishableCameraPresetByLotjuIdIn(latestKuvasMappedByPresetLotjuId.keySet());

        final List<Future<Boolean>> futures = new ArrayList<>();
        final StopWatch start = StopWatch.createStarted();

        for (final CameraPreset cameraPreset : cameraPresets) {
            final Kuva kuva = latestKuvasMappedByPresetLotjuId.remove(cameraPreset.getLotjuId());
            if (kuva == null) {
                log.error("No kuva for preset {}", cameraPreset.toString());
            } else {
                futures.add(cameraImageUpdateService.handleKuva(kuva, cameraPreset));
            }
        }

        // Handle missing presets to delete possible images from disk
        for (Kuva notFoundPresetsKuva : latestKuvasMappedByPresetLotjuId.values()) {
            futures.add(cameraImageUpdateService.handleKuva(notFoundPresetsKuva, null));
        }

        while ( futures.parallelStream().filter(f -> !f.isDone()).findFirst().isPresent() ) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                log.debug("InterruptedException", e);
            }
        }

        log.info("Updating {} weather camera images took {} ms", futures.size(), start.getTime());
    }

    private HashMap<Long, Kuva> filterLatestKuvasAndMapByPresetId(final List<Kuva> data) {
        // Collect newest kuva per preset
        final HashMap<Long, Kuva> kuvaMappedByPresetLotjuId = new HashMap<>();
        data.forEach(kuva -> {
            if (kuva.getEsiasentoId() != null) {
                Kuva currentKamera = kuvaMappedByPresetLotjuId.get(kuva.getEsiasentoId());
                if ( currentKamera == null || kuva.getAika().compare(currentKamera.getAika()) > 0 ) {
                    if (currentKamera != null) {
                        log.info("Replace " + currentKamera.getAika() + " with " + kuva.getAika());
                    }
                    kuvaMappedByPresetLotjuId.put(kuva.getEsiasentoId(), kuva);
                }
            } else {
                log.warn("Kuva esiasentoId is null: {}", ToStringHelper.toString(kuva));
            }
        });
        return kuvaMappedByPresetLotjuId;
    }
}
