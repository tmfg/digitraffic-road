package fi.livi.digitraffic.tie.service.lotju;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.conf.properties.LotjuMetadataProperties;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeEsiasennotKameranTunnuksella;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeEsiasento;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeEsiasentoResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeKaikkiKamerat;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeKamera;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeKameraResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.ObjectFactory;
import jakarta.xml.bind.JAXBElement;

@ConditionalOnNotWebApplication
@Component
public class LotjuCameraStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataClient.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuCameraStationMetadataClient(@Qualifier("kameraMetadataJaxb2Marshaller")
                                            final Jaxb2Marshaller kameraMetadataJaxb2Marshaller,
                                            final LotjuMetadataProperties lotjuMetadataProperties) {
        super(kameraMetadataJaxb2Marshaller, lotjuMetadataProperties, lotjuMetadataProperties.getPath().camera);
    }

    @Retryable(maxAttempts = 5)
    public List<KameraVO> getKameras() {
        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();
        final StopWatch start = StopWatch.createStarted();
        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                marshalSendAndReceive(objectFactory.createHaeKaikkiKamerat(request));
        log.info("cameraFetchedCount={} Cameras tookMs={}", response.getValue().getKamerat().size(), start.getDuration().toMillis());
        return response.getValue().getKamerat();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    public List<EsiasentoVO> getEsiasentos(final Long cameraLotjuId) {
        final StopWatch start = StopWatch.createStarted();
        final HaeEsiasennotKameranTunnuksella haeEsiasennotKameranTunnuksellaRequest =
                new HaeEsiasennotKameranTunnuksella();
        haeEsiasennotKameranTunnuksellaRequest.setId(cameraLotjuId);

        final JAXBElement<HaeEsiasennotKameranTunnuksellaResponse> haeEsiasennotResponse =
                (JAXBElement<HaeEsiasennotKameranTunnuksellaResponse>)
                        marshalSendAndReceive(objectFactory.createHaeEsiasennotKameranTunnuksella(haeEsiasennotKameranTunnuksellaRequest));
        log.info("method=getEsiasentos Fetched cameraLotjuId={} tookMs={}", cameraLotjuId, start.getDuration().toMillis());
        return haeEsiasennotResponse.getValue().getEsiasennot();
    }

    @PerformanceMonitor()
    @Retryable(maxAttempts = 5)
    public KameraVO getKamera(final long cameraLotjuId) {
        final HaeKamera request = new HaeKamera();
        request.setId(cameraLotjuId);
        final StopWatch start = StopWatch.createStarted();
        final JAXBElement<HaeKameraResponse> response = (JAXBElement<HaeKameraResponse>)
            marshalSendAndReceive(objectFactory.createHaeKamera(request));
        log.info("method=getKamera Fetched cameraLotjuId={} tookMs={}", cameraLotjuId, start.getDuration().toMillis());
        return response.getValue().getKamera();
    }

    @PerformanceMonitor()
    @Retryable(maxAttempts = 5)
    public EsiasentoVO getEsiasento(final long cameraLotjuId) {
        final HaeEsiasento request = new HaeEsiasento();
        request.setId(cameraLotjuId);
        final StopWatch start = StopWatch.createStarted();
        final JAXBElement<HaeEsiasentoResponse> response = (JAXBElement<HaeEsiasentoResponse>)
            marshalSendAndReceive(objectFactory.createHaeEsiasento(request));
        log.info("method=getEsiasento Fetched cameraLotjuId={} tookMs={}", cameraLotjuId, start.getDuration().toMillis());
        return response.getValue().getEsiasento();
    }
}
