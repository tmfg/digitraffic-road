package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.HaeKaikkiLaskennallisetAnturit;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.lotju.wsdl.tiesaa.TiesaaLaskennallinenAnturiVO;

public class LotjuRoadWeatherStationClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(LotjuRoadWeatherStationClient.class);

    private String address;

    public void setAddress(final String address) {
        this.address = address;
    }

    public List<TiesaaAsemaVO> getTiesaaAsemmas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();

        log.info("Fetching TiesaaAsemas");
        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiTiesaaAsemat(request));

        log.info("Fetched " + response.getValue().getTiesaaAsema().size() + " TiesaaAsemas");
        return response.getValue().getTiesaaAsema();
    }

    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturis(Set<Long> tiesaaAsemaLotjuIds) {

        log.info("Fetching TiesaaLaskennallinenAnturis for " + tiesaaAsemaLotjuIds.size() + " TiesaaAsemas");

        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();

        int counter = 0;
        for (Long tiesaaAsemaLotjuId : tiesaaAsemaLotjuIds) {
            request.setId(tiesaaAsemaLotjuId);

            final JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                    getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));
            final List<TiesaaLaskennallinenAnturiVO> anturis = response.getValue().getLaskennallinenAnturi();
            currentRwsLotjuIdToTiesaaAnturiMap.put(tiesaaAsemaLotjuId, anturis);
            counter += anturis.size();
        }

        log.info("Fetched " + counter + " TiesaaAnturis");
        return currentRwsLotjuIdToTiesaaAnturiMap;
    }

    public List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {

        log.info("Fetching all LaskennallisetAnturit");

        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLaskennallisetAnturit request = new HaeKaikkiLaskennallisetAnturit();

        final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiLaskennallisetAnturit(request));
        final List<TiesaaLaskennallinenAnturiVO> anturis = response.getValue().getLaskennallinenAnturi();

        log.info("Fetched " + anturis.size() + " LaskennallisetAnturits");
        return anturis;
    }

}
