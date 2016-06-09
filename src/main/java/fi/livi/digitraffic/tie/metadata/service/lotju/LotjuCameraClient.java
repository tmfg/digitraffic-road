package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.HaeEsiasennotKameranTunnuksella;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.HaeKaikkiKamerat;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.KameraVO;
import fi.livi.digitraffic.tie.lotju.wsdl.kamera.ObjectFactory;
import fi.livi.digitraffic.tie.metadata.service.camera.CameraUpdater;

public class LotjuCameraClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(LotjuCameraClient.class);

    private String address;

    final ObjectFactory objectFactory = new ObjectFactory();

    public void setAddress(final String address) {
        this.address = address;
    }

    public List<KameraVO> getKameras() {
        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();

        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiKamerat(request));

        return response.getValue().getKamerat();
    }


    public Map<String, Pair<KameraVO, EsiasentoVO>> getPresetIdToKameraAndEsiasentoMap() {

        final Map<String, Pair<KameraVO, EsiasentoVO>> presetIdToKameraMap = new HashMap<>();

        log.info("Fetch cameras");

        final List<KameraVO> kamerat = getKameras();

        log.info("Fetched " + kamerat.size() + " cameras");

        log.info("Fetch presets for cameras");
        int counter = 0;
        for (final KameraVO kamera : kamerat) {

            if (kamera.getVanhaId() == null) {
                log.error("Cannot update " + ToStringHelpper.toString(kamera) + " with vanhaId null");
            } else {
                final HaeEsiasennotKameranTunnuksella haeEsiasennotKameranTunnuksellaRequest =
                        new HaeEsiasennotKameranTunnuksella();
                haeEsiasennotKameranTunnuksellaRequest.setId(kamera.getId());

                final JAXBElement<HaeEsiasennotKameranTunnuksellaResponse> haeEsiasennotResponse = (JAXBElement<HaeEsiasennotKameranTunnuksellaResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeEsiasennotKameranTunnuksella(haeEsiasennotKameranTunnuksellaRequest));
                final List<EsiasentoVO> esiasennot = haeEsiasennotResponse.getValue().getEsiasennot();
                counter += esiasennot.size();

                final String kameraId = CameraUpdater.convertVanhaIdToKameraId(kamera.getVanhaId());
                for (final EsiasentoVO esiasento : esiasennot) {
                    final String presetId = CameraUpdater.convertCameraIdToPresetId(kameraId, esiasento.getSuunta());

                    presetIdToKameraMap.put(presetId, Pair.of(kamera, esiasento));
                }
            }
        }

        log.info("Fetched " + counter + " Esiasentos");

        return presetIdToKameraMap;
    }

}
