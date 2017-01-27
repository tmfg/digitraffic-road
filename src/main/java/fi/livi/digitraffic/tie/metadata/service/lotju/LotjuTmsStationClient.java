package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

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
        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMAsemat(request));

        log.info(FETCHED + response.getValue().getAsemat().size() + " LamAsemas");
        return response.getValue().getAsemat();
    }

    public List<LamLaskennallinenAnturiVO> getTiesaaLaskennallinenAnturis(final Long lamAsemaLotjuId) {

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeLAMAsemanLaskennallisetAnturit request = new HaeLAMAsemanLaskennallisetAnturit();

        request.setId(lamAsemaLotjuId);
        final JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse>)
                    getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeLAMAsemanLaskennallisetAnturit(request));
        return response.getValue().getLamlaskennallisetanturit();
    }

    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMLaskennallisetAnturit request = new HaeKaikkiLAMLaskennallisetAnturit();

        log.info("Fetching LAMLaskennallisetAnturis");
        final JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLAMLaskennallisetAnturit(request));

        log.info(FETCHED + response.getValue().getLaskennallinenAnturi().size() + " LAMLaskennallisetAnturis");
        return response.getValue().getLaskennallinenAnturi();
    }

}
