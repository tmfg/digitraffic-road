package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.List;

import javax.xml.bind.JAXBElement;

import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;
import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class RoadWeatherStationClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(RoadWeatherStationClient.class);

    private String address;

    public void setAddress(final String address) {
        this.address = address;
    }

    public List<TiesaaAsema> getTiesaaAsemmas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();

        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiTiesaaAsemat(request));

        log.info("Fetched " + response.getValue().getTiesaaAsema().size() + " TiesaaAsemas");
        return response.getValue().getTiesaaAsema();
    }

}
