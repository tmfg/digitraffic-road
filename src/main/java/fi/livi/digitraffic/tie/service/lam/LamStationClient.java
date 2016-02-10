package fi.livi.digitraffic.tie.service.lam;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.wsdl.lam.HaeKaikkiLAMAsemat;
import fi.livi.digitraffic.tie.wsdl.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import fi.livi.digitraffic.tie.wsdl.lam.ObjectFactory;

public class LamStationClient extends WebServiceGatewaySupport {
    private String address;

    public List<LamAsema> getLamStations() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLAMAsemat(request));

        return response.getValue().getAsemat();
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
