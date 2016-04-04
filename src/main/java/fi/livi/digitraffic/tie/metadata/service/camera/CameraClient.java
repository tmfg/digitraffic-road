package fi.livi.digitraffic.tie.metadata.service.camera;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.wsdl.kamera.Esiasento;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeEsiasennotKameranTunnuksella;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeKaikkiKamerat;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.ObjectFactory;

public class CameraClient extends WebServiceGatewaySupport {

    private static final Logger log = Logger.getLogger(CameraClient.class);

    private String address;

    final ObjectFactory objectFactory = new ObjectFactory();

    public void setAddress(final String address) {
        this.address = address;
    }

    public List<Kamera> getKameras() {
        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();

        final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeKaikkiKamerat(request));

        return response.getValue().getKamerat();
    }


    public Map<String, Pair<Kamera, Esiasento>> getPresetIdToKameraAndEsiasentoMap() {

        final Map<String, Pair<Kamera, Esiasento>> presetIdToKameraMap = new HashMap<>();

        log.info("Fetch cameras");

        final List<Kamera> kamerat = getKameras();

        log.info("Fetched " + kamerat.size() + " cameras");

        log.info("Fetch presets for cameras");
        int counter = 0;
        for (final Kamera kamera : kamerat) {

            if (kamera.getVanhaId() == null) {
                log.error("Cannot update " + ToStringHelpper.toString(kamera) + " with vanhaId null");
            } else {
                final HaeEsiasennotKameranTunnuksella haeEsiasennotKameranTunnuksellaRequest =
                        new HaeEsiasennotKameranTunnuksella();
                haeEsiasennotKameranTunnuksellaRequest.setId(kamera.getId());

                final JAXBElement<HaeEsiasennotKameranTunnuksellaResponse> haeEsiasennotResponse = (JAXBElement<HaeEsiasennotKameranTunnuksellaResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(address, objectFactory.createHaeEsiasennotKameranTunnuksella(haeEsiasennotKameranTunnuksellaRequest));
                final List<Esiasento> esiasennot = haeEsiasennotResponse.getValue().getEsiasennot();
                counter += esiasennot.size();

                final String kameraId = CameraUpdater.convertVanhaIdToKameraId(kamera.getVanhaId());
                for (final Esiasento esiasento : esiasennot) {
                    final String presetId = CameraUpdater.convertCameraIdToPresetId(kameraId, esiasento.getSuunta());

                    presetIdToKameraMap.put(presetId, Pair.of(kamera, esiasento));
                }
            }
        }

        log.info("Fetched " + counter + " Esiasentos");

        return presetIdToKameraMap;
    }

}
