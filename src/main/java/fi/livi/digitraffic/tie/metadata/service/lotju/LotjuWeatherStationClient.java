package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiTiesaaAsemat;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeKaikkiTiesaaAsematResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.ObjectFactory;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06.TiesaaLaskennallinenAnturiVO;

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

    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getTiesaaLaskennallinenAnturisMappedByAsemaLotjuId(final Set<Long> tiesaaAsemaLotjuIds) {

        log.info("Fetching TiesaaLaskennallinenAnturis for " + tiesaaAsemaLotjuIds.size() + " TiesaaAsemas");

        final Map<Long, List<TiesaaLaskennallinenAnturiVO>> currentRwsLotjuIdToTiesaaAnturiMap =
                new HashMap<>();

        final ObjectFactory objectFactory = new ObjectFactory();
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();

        int counter = 0;
        for (final Long tiesaaAsemaLotjuId : tiesaaAsemaLotjuIds) {
            request.setId(tiesaaAsemaLotjuId);

            int triesLeft = 5;
            while (triesLeft > 0) {
                triesLeft--;
                try {
                    final JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response =
                            (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                            getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));
                    final List<TiesaaLaskennallinenAnturiVO> anturis = response.getValue().getLaskennallinenAnturi();
                    currentRwsLotjuIdToTiesaaAnturiMap.put(tiesaaAsemaLotjuId, anturis);
                    counter += anturis.size();
                    triesLeft = 0;
                } catch (Exception fail) {
                    log.info("HaeTiesaaAsemanLaskennallisetAnturit failed for tiesaaAsemaLotjuId {}, {} tries left", tiesaaAsemaLotjuId, triesLeft);
                    if (triesLeft <= 0) {
                        throw fail;
                    }
                }
            }

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

        int triesLeft = 5;
        while (true) {
            triesLeft--;
            try {
                final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiLaskennallisetAnturit(request));
                final List<TiesaaLaskennallinenAnturiVO> anturis = response.getValue().getLaskennallinenAnturi();
                log.info(FETCHED + anturis.size() + " LaskennallisetAnturits");
                return anturis;
            } catch (Exception fail) {
                log.info("HaeKaikkiLaskennallisetAnturit failed, {} tries left", triesLeft);
                if (triesLeft <= 0) {
                    throw fail;
                }
            }
        }
    }

}
