package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeKaikkiLAMAsemat;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.HaeKaikkiLAMAsematResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.ObjectFactory;

public class LotjuLamStationClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(LotjuLamStationClient.class);

    private String address;

    public List<LamAsemaVO> getLamAsemas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        log.info("Fetching LamAsemas");
        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLAMAsemat(request));

        log.info("Fetched " + response.getValue().getAsemat().size() + " LamAsemas");
        return response.getValue().getAsemat();
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
