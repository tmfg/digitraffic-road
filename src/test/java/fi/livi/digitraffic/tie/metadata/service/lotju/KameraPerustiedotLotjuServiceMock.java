package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.wsdl.kamera.Esiasento;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.digitraffic.tie.wsdl.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.kamera.KameraKokoonpano;
import fi.livi.digitraffic.tie.wsdl.kamera.KameraPerustiedot;
import fi.livi.digitraffic.tie.wsdl.kamera.KameraPerustiedotEndpointService;
import fi.livi.digitraffic.tie.wsdl.kamera.KameraPerustiedotException;
import fi.livi.digitraffic.tie.wsdl.kamera.ObjectFactory;
import fi.livi.digitraffic.tie.wsdl.kamera.Videopalvelin;

@Service
public class KameraPerustiedotLotjuServiceMock extends LotjuServiceMock implements KameraPerustiedot {

    private static final Logger log = Logger.getLogger(KameraPerustiedotLotjuServiceMock.class);

    private List<Kamera> initialKameras;
    private List<Kamera> afterChangeKameras;
    private Map<Long, List<Esiasento>> initialEsiasentos = new HashMap<>();

    @Autowired
    public KameraPerustiedotLotjuServiceMock(@Value("${metadata.server.address.camera}")
                                             final String metadataServerAddressCamera,
                                             final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressCamera, KameraPerustiedot.class, KameraPerustiedotEndpointService.SERVICE);

        setInitialKameras(readKameras("lotju/kamera/HaeKaikkiKameratResponseInitial.xml"));
        setAfterChangeKameras(readKameras("lotju/kamera/HaeKaikkiKameratResponseChanged.xml"));
        appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse2.xml"), getInitialEsiasentos());
        appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse121.xml"), getInitialEsiasentos());
        appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse443.xml"), getInitialEsiasentos());

    }

    private void appendEsiasentos(List<Esiasento> esiasentos, Map<Long, List<Esiasento>> initialEsiasentos) {
        for (Esiasento ea : esiasentos) {
            long kId = ea.getKameraId();
            List<Esiasento> eas = getInitialEsiasentos().get(Long.valueOf(kId));
            if (eas == null) {
                eas = new ArrayList<>();
                initialEsiasentos.put(kId, eas);
            }
            eas.add(ea);
        }
    }

    private List<Kamera> readKameras(String filePath) {
        HaeKaikkiKameratResponse responseValue = (HaeKaikkiKameratResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( Kamera k : responseValue.getKamerat() ) {
            Assert.assertEquals(0, k.getKokoonpanoId());
            Assert.assertEquals(0, k.getVideopalvelinId());

            Assert.assertNull(k.getAikakatkaisu());
            Assert.assertNull(k.getAliverkonPeite());
            Assert.assertNull(k.getAlkamisPaiva());
            Assert.assertNull(k.getAsemanTila());
            Assert.assertNull(k.getHuoltolevikkeenEtaisyysAsemasta());
            Assert.assertNull(k.getHuoltoPuutteet());
            Assert.assertNull(k.getKorjaushuolto());
            Assert.assertNull(k.getLaitekaappiId());
            Assert.assertNull(k.getLiviId());
            Assert.assertNull(k.getOhjelmistoversio());
            Assert.assertNull(k.getPaattymisPaiva());
            Assert.assertNull(k.getTakuunPaattymisPvm());
            Assert.assertNull(k.getVuosihuolto());
            Assert.assertNull(k.getVerkkolaiteId());
            Assert.assertNull(k.getYhdyskaytava());
            Assert.assertNull(k.getYhteysTapa());
            Assert.assertNull(k.isRiittavanKokoinenHuoltolevike());
        }
        return responseValue.getKamerat();
    }

    private List<Esiasento> readEsiasentos(String filePath) {
        HaeEsiasennotKameranTunnuksellaResponse responseValue = (HaeEsiasennotKameranTunnuksellaResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getEsiasennot();
    }

    public List<Kamera> getInitialKameras() {
        return initialKameras;
    }

    public void setInitialKameras(List<Kamera> initialKameras) {
        this.initialKameras = initialKameras;
    }

    public void setAfterChangeKameras(List<Kamera> afterChangeKameras) {
        this.afterChangeKameras = afterChangeKameras;
    }

    public List<Kamera> getAfterChangeKameras() {
        return afterChangeKameras;
    }

    public void setInitialEsiasentos(Map<Long, List<Esiasento>> initialEsiasentos) {
        this.initialEsiasentos = initialEsiasentos;
    }

    public Map<Long, List<Esiasento>> getInitialEsiasentos() {
        return initialEsiasentos;
    }

    /* KameraPerustiedot Service methods */

    @Override
    public Kamera haeKamera(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKamera");
    }

    @Override
    public List<Kamera> haeKameratVideopalvelimenTunnuksella(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public Videopalvelin haeVideopalvelin(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeVideopalvelin");
    }

    @Override
    public List<Esiasento> haeEsiasennotKameranTunnuksella(Long id) throws KameraPerustiedotException {
        log.info("haeEsiasennotKameranTunnuksella " + id);
        List<Esiasento> eas = getInitialEsiasentos().get(id);
        return eas;
    }

    @Override
    public Esiasento haeEsiasento(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeEsiasento");
    }

    @Override
    public KameraKokoonpano haeKokoonpanoKameranTunnuksella(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKokoonpanoKameranTunnuksella");
    }

    @Override
    public List<Kamera> haeKaikkiKamerat() throws KameraPerustiedotException {
        if (isStateAfterChange()) {
            return getAfterChangeKameras();
        }
        return getInitialKameras();
    }

    @Override
    public List<Videopalvelin> haeKaikkiVideopalvelimet() throws KameraPerustiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

}
