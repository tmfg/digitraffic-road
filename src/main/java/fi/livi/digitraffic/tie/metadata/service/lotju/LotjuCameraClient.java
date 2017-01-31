package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.livi.digitraffic.tie.helper.CameraHelper;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.HaeEsiasennotKameranTunnuksella;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.HaeKaikkiKamerat;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.HaeKaikkiKameratResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.ObjectFactory;

public class LotjuCameraClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(LotjuCameraClient.class);

    final ObjectFactory objectFactory = new ObjectFactory();

    public Map<Long, Pair<KameraVO, List<EsiasentoVO>>> getLotjuIdToKameraAndEsiasentoMap() {

        final Map<Long, Pair<KameraVO, List<EsiasentoVO>>> lotjuIdToKameraAndEsiasentoMap = new HashMap<>();

        log.info("Fetch Kameras");

        final List<KameraVO> kamerat = getKameras();

        log.info("Fetched " + kamerat.size() + " cameras");

        log.info("Fetch Esiasentos for cameras");

        final AtomicInteger counter = new AtomicInteger();

        kamerat.parallelStream().forEach(kamera -> {

            if (kamera.getVanhaId() == null) {
                log.error("Cannot update " + ToStringHelpper.toString(kamera) + " is invalid: has null vanhaId");
            } else {

                final List<EsiasentoVO> esiasennot = getEsiasentos(kamera.getId());
                counter.addAndGet(esiasennot.size());

                final String kameraId = CameraHelper.convertVanhaIdToKameraId(kamera.getVanhaId());
                for (final EsiasentoVO esiasento : esiasennot) {

                    final String presetId = CameraHelper.convertCameraIdToPresetId(kameraId, esiasento.getSuunta());

                    if (CameraHelper.validatePresetId(presetId)) {
                        Pair<KameraVO, List<EsiasentoVO>> kameraPair = lotjuIdToKameraAndEsiasentoMap.get(kamera.getId());
                        if (kameraPair == null) {
                            kameraPair = Pair.of(kamera, new ArrayList<EsiasentoVO>());
                            lotjuIdToKameraAndEsiasentoMap.put(kamera.getId(), kameraPair);
                        }
                        kameraPair.getRight().add(esiasento);
                    } else {
                        log.error("Invalid cameraPresetId for {} and {}",
                                  ToStringHelpper.toString(kamera),
                                  ToStringHelpper.toString(esiasento));
                    }
                }
            }
        });

        log.info("Fetched {} Esiasentos", counter.get());

        return lotjuIdToKameraAndEsiasentoMap;
    }

    public List<KameraVO> getKameras() {

        final HaeKaikkiKamerat request = new HaeKaikkiKamerat();
        int triesLeft = 5;
        while (true) {
            triesLeft--;
            try {
                final JAXBElement<HaeKaikkiKameratResponse> response = (JAXBElement<HaeKaikkiKameratResponse>)
                        getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeKaikkiKamerat(request));
                return response.getValue().getKamerat();
            } catch (Exception fail) {
                log.error("HaeKaikkiKamerat failed, {} tries left", triesLeft);
                if (triesLeft <= 0) {
                    throw fail;
                }
            }
        }
    }

    private List<EsiasentoVO> getEsiasentos(Long kameraId) {
        final HaeEsiasennotKameranTunnuksella haeEsiasennotKameranTunnuksellaRequest =
                new HaeEsiasennotKameranTunnuksella();
        haeEsiasennotKameranTunnuksellaRequest.setId(kameraId);

        int triesLeft = 5;
        while (true) {
            triesLeft--;
            try {
                final JAXBElement<HaeEsiasennotKameranTunnuksellaResponse> haeEsiasennotResponse =
                        (JAXBElement<HaeEsiasennotKameranTunnuksellaResponse>)
                                getWebServiceTemplate().marshalSendAndReceive(objectFactory.createHaeEsiasennotKameranTunnuksella(haeEsiasennotKameranTunnuksellaRequest));
                return haeEsiasennotResponse.getValue().getEsiasennot();
            } catch (Exception fail) {
                log.error("HaeEsiasennotKameranTunnuksella failed with kameraId {}, {} tries left", kameraId, triesLeft);
                if (triesLeft <= 0) {
                    throw fail;
                }
            }
        }

    }

}
