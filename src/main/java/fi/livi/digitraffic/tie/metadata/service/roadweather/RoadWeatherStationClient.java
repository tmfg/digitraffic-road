package fi.livi.digitraffic.tie.metadata.service.roadweather;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeTiesaaAsemanAnturit;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeTiesaaAsemanAnturitResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAnturi;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;

public class RoadWeatherStationClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(RoadWeatherStationClient.class);

    private String address;

    public void setAddress(final String address) {
        this.address = address;
    }

    public List<TiesaaAsema> getTiesaaAsemmas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();

        log.info("Fetching TiesaaAsemas");
        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiTiesaaAsemat(request));

        log.info("Fetched " + response.getValue().getTiesaaAsema().size() + " TiesaaAsemas");
        return response.getValue().getTiesaaAsema();
    }


    public Map<Long, List<TiesaaAnturi>> getTiesaaAnturis(Set<Long> tiesaaAsemaLotjuIds) {

        log.info("Fetching TiesaaAnturis for " + tiesaaAsemaLotjuIds.size() + " tiesaaAsemas");

        final Map<Long, List<TiesaaAnturi>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeTiesaaAsemanAnturit request = new HaeTiesaaAsemanAnturit();

        int counter = 0;
        for (Long tiesaaAsemaLotjuId : tiesaaAsemaLotjuIds) {
            request.setId(tiesaaAsemaLotjuId);

            final JAXBElement<HaeTiesaaAsemanAnturitResponse> response = (JAXBElement<HaeTiesaaAsemanAnturitResponse>)
                    getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeTiesaaAsemanAnturit(request));
            final List<TiesaaAnturi> anturis = response.getValue().getTiesaaAnturi();
            currentRwsLotjuIdToTiesaaAnturiMap.put(tiesaaAsemaLotjuId, anturis);
            counter += anturis.size();
        }

        log.info("Fetched " + counter + " TiesaaAnturis");
        return currentRwsLotjuIdToTiesaaAnturiMap;
    }
}
