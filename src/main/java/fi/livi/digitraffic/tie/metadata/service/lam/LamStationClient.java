package fi.livi.digitraffic.tie.metadata.service.lam;

import java.util.List;

import javax.xml.bind.JAXBElement;

import fi.livi.digitraffic.tie.wsdl.lam.HaeKaikkiLAMAsemat;
import fi.livi.digitraffic.tie.wsdl.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import fi.livi.digitraffic.tie.wsdl.lam.ObjectFactory;
import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class LamStationClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(LamStationClient.class);

    private String address;

    public List<LamAsema> getLamAsemas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLAMAsemat(request));

        log.info("Fetched " + response.getValue().getAsemat().size() + " LamAsemas");
        return response.getValue().getAsemat();
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
