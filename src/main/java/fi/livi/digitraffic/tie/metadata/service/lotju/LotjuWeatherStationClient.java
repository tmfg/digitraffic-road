package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeKaikkiLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeKaikkiTiesaaAsemat;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeKaikkiTiesaaAsematResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.ObjectFactory;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaLaskennallinenAnturiVO;

public class LotjuWeatherStationClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(LotjuWeatherStationClient.class);
    public static final String FETCHED = "Fetched ";

    public List<TiesaaAsemaVO> getTiesaaAsemmas() {
        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();

        log.info("Fetching TiesaaAsemas");
        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiTiesaaAsemat(request));

        log.info(FETCHED + response.getValue().getTiesaaAsema().size() + " TiesaaAsemas");
        return response.getValue().getTiesaaAsema();
    }

    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturis(final Set<Long> tiesaaAsemaLotjuIds) {

        log.info("Fetching TiesaaLaskennallinenAnturis for " + tiesaaAsemaLotjuIds.size() + " TiesaaAsemas");

        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();

        int counter = 0;
        for (final Long tiesaaAsemaLotjuId : tiesaaAsemaLotjuIds) {
            request.setId(tiesaaAsemaLotjuId);

            int triesLeft = 3;
            JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response = null;
            while (response == null && triesLeft > 0) {
                triesLeft--;
                try {
                    response = (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                            getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));
                } catch (Exception fail) {
                    if (triesLeft <= 0) {
                        throw new RuntimeException("HaeTiesaaAsemanLaskennallisetAnturit for failed for tiesaaAsemaLotjuId " + tiesaaAsemaLotjuId + " 5th time - giving up");
                    }
                    try {
                        log.info("HaeTiesaaAsemanLaskennallisetAnturit for failed for tiesaaAsemaLotjuId " + tiesaaAsemaLotjuId + " - " + triesLeft + " tries left");
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                }
            }
            final List<TiesaaLaskennallinenAnturiVO> anturis = response.getValue().getLaskennallinenAnturi();
            currentRwsLotjuIdToTiesaaAnturiMap.put(tiesaaAsemaLotjuId, anturis);
            counter += anturis.size();
        }

        log.info(FETCHED + counter + " TiesaaAnturis");
        return currentRwsLotjuIdToTiesaaAnturiMap;
    }

    public List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {

        log.info("Fetching all LaskennallisetAnturit");

        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeKaikkiLaskennallisetAnturit request = new HaeKaikkiLaskennallisetAnturit();

        final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLaskennallisetAnturit(request));
        final List<TiesaaLaskennallinenAnturiVO> anturis = response.getValue().getLaskennallinenAnturi();

        log.info(FETCHED + anturis.size() + " LaskennallisetAnturits");
        return anturis;
    }

}
