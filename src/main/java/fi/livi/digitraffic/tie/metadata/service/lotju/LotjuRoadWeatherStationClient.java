package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiLaskennallisetAnturit;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaLaskennallinenAnturi;

public class LotjuRoadWeatherStationClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(LotjuRoadWeatherStationClient.class);

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

    public Map<Long, List<TiesaaLaskennallinenAnturi>> getTiesaaLaskennallinenAnturis(Set<Long> tiesaaAsemaLotjuIds) {

        log.info("Fetching TiesaaLaskennallinenAnturis for " + tiesaaAsemaLotjuIds.size() + " tiesaaAsemas");

        final Map<Long, List<TiesaaLaskennallinenAnturi>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();

        int counter = 0;
        for (Long tiesaaAsemaLotjuId : tiesaaAsemaLotjuIds) {
            request.setId(tiesaaAsemaLotjuId);

            final JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                    getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));
            final List<TiesaaLaskennallinenAnturi> anturis = response.getValue().getLaskennallinenAnturi();
            currentRwsLotjuIdToTiesaaAnturiMap.put(tiesaaAsemaLotjuId, anturis);
            counter += anturis.size();
        }

        log.info("Fetched " + counter + " TiesaaAnturis");
        return currentRwsLotjuIdToTiesaaAnturiMap;
    }

    public List<TiesaaLaskennallinenAnturi> getAllTiesaaLaskennallinenAnturis() {

        log.info("Fetching all LaskennallisetAnturit");

        final Map<Long, List<TiesaaLaskennallinenAnturi>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLaskennallisetAnturit request = new HaeKaikkiLaskennallisetAnturit();

        final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLaskennallisetAnturit(request));
        final List<TiesaaLaskennallinenAnturi> anturis = response.getValue().getLaskennallinenAnturi();

        log.info("Fetched " + anturis.size() + " LaskennallisetAnturits");
        return anturis;
    }

}
