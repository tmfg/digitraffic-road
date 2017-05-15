package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiTiesaaAsemat;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiTiesaaAsematResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.ObjectFactory;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

@Service
public class LotjuWeatherStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuWeatherStationMetadataClient.class);
    final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuWeatherStationMetadataClient(Jaxb2Marshaller marshaller,
                                             @Value("${metadata.server.address.weather}") String weatherMetadataServerAddress) {
        super(marshaller, weatherMetadataServerAddress, log);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    List<TiesaaAsemaVO> getTiesaaAsemmas() {
        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();
        log.info("Fetching TiesaaAsemas");
        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiTiesaaAsemat(request));
        log.info("Fetched {} TiesaaAsemas", response.getValue().getTiesaaAsema().size());
        return response.getValue().getTiesaaAsema();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {

        log.info("Fetching all LaskennallisetAnturit");
        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();
        final HaeKaikkiLaskennallisetAnturit request = new HaeKaikkiLaskennallisetAnturit();

        final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLaskennallisetAnturit(request));
        return response.getValue().getLaskennallinenAnturi();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    List<TiesaaLaskennallinenAnturiVO> getTiesaaAsemanLaskennallisetAnturit(Long tiesaaAsemaLotjuId) {
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();
        request.setId(tiesaaAsemaLotjuId);
        final JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response =
                (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));
        return response.getValue().getLaskennallinenAnturi();
    }

}
