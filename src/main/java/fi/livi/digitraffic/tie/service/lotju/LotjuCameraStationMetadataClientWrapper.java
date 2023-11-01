package fi.livi.digitraffic.tie.service.lotju;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;

@ConditionalOnNotWebApplication
@Component
public class LotjuCameraStationMetadataClientWrapper {
    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataClientWrapper.class);
    private final LotjuCameraStationMetadataClient lotjuCameraStationClient;

    @Autowired
    public LotjuCameraStationMetadataClientWrapper(final LotjuCameraStationMetadataClient lotjuCameraStationClient) {
        this.lotjuCameraStationClient = lotjuCameraStationClient;
    }

    public List<KameraVO> getKameras() {
        return lotjuCameraStationClient.getKameras();
    }

    public KameraVO getKamera(final long kameraLotjuId) {
        return lotjuCameraStationClient.getKamera(kameraLotjuId);
    }

    public List<EsiasentoVO> getEsiasentos(final Long kameraId) {
        return lotjuCameraStationClient.getEsiasentos(kameraId);
    }

    public EsiasentoVO getEsiasento(final long esiasentoLotjuId) {
        return lotjuCameraStationClient.getEsiasento(esiasentoLotjuId);
    }
}
