package fi.livi.digitraffic.tie.service.camera;

import java.util.List;

import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CameraUpdater {

    private static final Logger LOG = Logger.getLogger(CameraUpdater.class);

    private final CameraClient cameraClient;

    @Autowired
    public CameraUpdater(final CameraClient cameraClient) {
        this.cameraClient = cameraClient;
    }

    // 5 min
    @Scheduled(fixedRate = 5*60*1000)
    @Transactional
    public void updateLamStations() {
        LOG.info("updateCameras start");

        if (cameraClient == null) {
            LOG.warn("Not updating camera metadatas because no cameraClient defined");
            return;
        }

        final List<Kamera> cameras = cameraClient.getCameras();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetched Cameras:");
            for (Kamera camera : cameras) {
                LOG.debug(ToStringBuilder.reflectionToString(camera));
            }
        }

        // TODO update camera metadata

        LOG.info("updateCameras done");
    }

}
