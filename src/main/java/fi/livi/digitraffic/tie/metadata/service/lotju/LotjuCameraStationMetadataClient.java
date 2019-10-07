package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.HaeEsiasennotKameranTunnuksella;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.HaeKaikkiKamerat;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.HaeKaikkiKameratResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.ObjectFactory;

@ConditionalOnNotWebApplication
@Service
public class LotjuCameraStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuCameraStationMetadataClient.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuCameraStationMetadataClient(Jaxb2Marshaller marshaller,
                                            @Value("${metadata.server.address.camera}") final String cameraMetadataServerAddress) {
        super(marshaller, cameraMetadataServerAddress, log);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    public List<KameraVO> getKameras() {
        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();
        final StopWatch start = StopWatch.createStarted();
        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiKamerat(request));
        log.info("cameraFetchedCount={} Cameras tookMs={}", response.getValue().getKamerat().size(), start.getTime());
        return response.getValue().getKamerat();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    public List<EsiasentoVO> getEsiasentos(Long kameraId) {
        final HaeEsiasennotKameranTunnuksella haeEsiasennotKameranTunnuksellaRequest =
                new HaeEsiasennotKameranTunnuksella();
        haeEsiasennotKameranTunnuksellaRequest.setId(kameraId);

        final JAXBElement<HaeEsiasennotKameranTunnuksellaResponse> haeEsiasennotResponse =
                (JAXBElement<HaeEsiasennotKameranTunnuksellaResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeEsiasennotKameranTunnuksella(haeEsiasennotKameranTunnuksellaRequest));
        return haeEsiasennotResponse.getValue().getEsiasennot();
    }

}
