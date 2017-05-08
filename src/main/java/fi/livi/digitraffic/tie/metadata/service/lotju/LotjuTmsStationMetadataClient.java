package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMAsemat;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMAsematResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeLAMAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.ObjectFactory;

@Service
public class LotjuTmsStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataClient.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuTmsStationMetadataClient(Jaxb2Marshaller marshaller,
                                         @Value("${metadata.server.address.tms}") final String tmsMetadataServerAddress) {
        super(marshaller, tmsMetadataServerAddress, log);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<LamAsemaVO> getLamAsemas() {

        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        log.info("Fetching LamAsemas");
        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMAsemat(request));
        log.info("Fetched {} LamAsemas", response.getValue().getAsemat().size());
        return response.getValue().getAsemat();
    }

    @Retryable(maxAttempts = 5)
    List<LamLaskennallinenAnturiVO> getTiesaaLaskennallinenAnturis(final Long lamAsemaLotjuId) {

        final HaeLAMAsemanLaskennallisetAnturit request = new HaeLAMAsemanLaskennallisetAnturit();
        request.setId(lamAsemaLotjuId);
        final JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeLAMAsemanLaskennallisetAnturit(request));
        return response.getValue().getLamlaskennallisetanturit();
    }

    @Retryable(maxAttempts = 5)
    List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        final HaeKaikkiLAMLaskennallisetAnturit request = new HaeKaikkiLAMLaskennallisetAnturit();
        log.info("Fetching LAMLaskennallisetAnturis");
        final JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMLaskennallisetAnturit(request));
        log.info("Fetched {} LAMLaskennallisetAnturis", response.getValue().getLaskennallinenAnturi().size());
        return response.getValue().getLaskennallinenAnturi();
    }

}
