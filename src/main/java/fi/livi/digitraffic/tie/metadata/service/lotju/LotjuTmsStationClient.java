package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMAsemat;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMAsematResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeLAMAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.ObjectFactory;

public class LotjuTmsStationClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationClient.class);
    public static final String FETCHED = "Fetched ";

    public List<LamAsemaVO> getLamAsemas() {

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        log.info("Fetching LamAsemas");

        int triesLeft = 5;
        while (true) {
            triesLeft--;
            try {
                final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMAsemat(request));
                log.info(FETCHED + response.getValue().getAsemat().size() + " LamAsemas");
                return response.getValue().getAsemat();
            } catch (Exception fail) {
                log.info("HaeKaikkiLAMAsemat failed {}, tries left", triesLeft);
                if (triesLeft < 1) {
                    throw fail;
                }
            }
        }
    }

    private List<LamLaskennallinenAnturiVO> getTiesaaLaskennallinenAnturis(final Long lamAsemaLotjuId) {

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeLAMAsemanLaskennallisetAnturit request = new HaeLAMAsemanLaskennallisetAnturit();
        request.setId(lamAsemaLotjuId);
        int triesLeft = 5;
        while (true) {
            triesLeft--;
            try {
                final JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeLAMAsemanLaskennallisetAnturit(request));
                return response.getValue().getLamlaskennallisetanturit();
            } catch (Exception fail) {
                log.info("HaeLAMAsemanLaskennallisetAnturit failed for lamAsemaLotjuId {}, {} tries left", lamAsemaLotjuId, triesLeft);
                if (triesLeft <= 0) {
                    throw fail;
                }
            }
        }
    }

    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMLaskennallisetAnturit request = new HaeKaikkiLAMLaskennallisetAnturit();
        log.info("Fetching LAMLaskennallisetAnturis");

        int triesLeft = 5;
        while (true) {
            triesLeft--;
            try {
                final JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMLaskennallisetAnturit(request));
                log.info(FETCHED + response.getValue().getLaskennallinenAnturi().size() + " LAMLaskennallisetAnturis");
                return response.getValue().getLaskennallinenAnturi();
            } catch (Exception fail) {
                log.info("HaeKaikkiLAMLaskennallisetAnturit failed, {} tries left", triesLeft);
                if (triesLeft <= 0) {
                    throw fail;
                }
            }
        }
    }

    public Map<Long, List<LamLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(Set<Long> tmsLotjuIds) {
        final Map<Long, List<LamLaskennallinenAnturiVO>> currentLamAnturisMappedByTmsLotjuId = new HashMap<>();
        final AtomicInteger counter = new AtomicInteger();
        tmsLotjuIds.stream().forEach(tmsStationLotjuId -> {
            final List<LamLaskennallinenAnturiVO> anturis = getTiesaaLaskennallinenAnturis(tmsStationLotjuId);
            currentLamAnturisMappedByTmsLotjuId.put(tmsStationLotjuId, anturis);
            counter.addAndGet(1);
        });
        return currentLamAnturisMappedByTmsLotjuId;
    }
}
