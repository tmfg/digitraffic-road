package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.HaeEsiasennotKameranTunnuksella;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.HaeKaikkiKamerat;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.HaeKaikkiKameratResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.ObjectFactory;

public class LotjuCameraClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(LotjuCameraClient.class);

    final ObjectFactory objectFactory = new ObjectFactory();

    public List<KameraVO> getKameras() {
        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();

        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiKamerat(request));

        return response.getValue().getKamerat();
    }


    public Map<String, Pair<KameraVO, EsiasentoVO>> getPresetIdToKameraAndEsiasentoMap() {

        final Map<String, Pair<KameraVO, EsiasentoVO>> presetIdToKameraMap = new HashMap<>();

        log.info("Fetch Kameras");

        final List<KameraVO> kamerat = getKameras();

        log.info("Fetched " + kamerat.size() + " cameras");

        log.info("Fetch Esiasentos for cameras");
        int counter = 0;
        for (final KameraVO kamera : kamerat) {

            if (kamera.getVanhaId() == null) {
                log.error("Cannot update " + ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
            } else {
                final HaeEsiasennotKameranTunnuksella haeEsiasennotKameranTunnuksellaRequest =
                        new HaeEsiasennotKameranTunnuksella();
                haeEsiasennotKameranTunnuksellaRequest.setId(kamera.getId());

                final JAXBElement<HaeEsiasennotKameranTunnuksellaResponse> haeEsiasennotResponse = (JAXBElement<HaeEsiasennotKameranTunnuksellaResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeEsiasennotKameranTunnuksella(haeEsiasennotKameranTunnuksellaRequest));
                final List<EsiasentoVO> esiasennot = haeEsiasennotResponse.getValue().getEsiasennot();
                counter += esiasennot.size();

                final String kameraId = CameraHelper.convertVanhaIdToKameraId(kamera.getVanhaId());
                for (final EsiasentoVO esiasento : esiasennot) {
                    final String presetId = CameraHelper.convertCameraIdToPresetId(kameraId, esiasento.getSuunta());
                    if (CameraHelper.validatePresetId(presetId)) {
                        presetIdToKameraMap.put(presetId, Pair.of(kamera, esiasento));
                    } else {
                        log.error("Invalid cameraPresetId for " + ToStringHelpper.toString(kamera) + " and " + ToStringHelpper.toString(esiasento));
                    }
                }
            }
        }

        log.info("Fetched " + counter + " Esiasentos");

        return presetIdToKameraMap;
    }

}
