package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.HaeKaikkiKameratResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraKokoonpanoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraPerustiedotEndpoint;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraPerustiedotException;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraPerustiedotV2;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.ObjectFactory;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29.VideopalvelinVO;

@Service
public class KameraPerustiedotLotjuServiceMock extends LotjuServiceMock implements KameraPerustiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(KameraPerustiedotLotjuServiceMock.class);

    private List<KameraVO> initialKameras;
    private List<KameraVO> afterChangeKameras;
    private Map<Long, List<EsiasentoVO>> initialEsiasentos = new HashMap<>();
    private Map<Long, List<EsiasentoVO>> afterChangeEsiasentos = new HashMap<>();
    private boolean inited;

    @Autowired
    public KameraPerustiedotLotjuServiceMock(@Value("${metadata.server.address.camera}")
                                             final String metadataServerAddressCamera,
                                             final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressCamera, KameraPerustiedotEndpoint.class, KameraPerustiedotV2.SERVICE);
    }

    @Override
    public void initDataAndService() {
        if (!isInited()) {
            initService();
            setInitialKameras(readKameras("lotju/kamera/HaeKaikkiKameratResponseInitial.xml"));
            setAfterChangeKameras(readKameras("lotju/kamera/HaeKaikkiKameratResponseChanged.xml"));
            appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse2.xml"), getInitialEsiasentos(), getAfterChangeEsiasentos());
            appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse121.xml"), getInitialEsiasentos(), getAfterChangeEsiasentos());
            appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse56.xml"), getInitialEsiasentos(), getAfterChangeEsiasentos());
            appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse443.xml"), getInitialEsiasentos());
            appendEsiasentos(readEsiasentos("lotju/kamera/HaeEsiasennotKameranTunnuksellaResponse443Changed.xml"), getAfterChangeEsiasentos());
        }
    }

    private void appendEsiasentos(List<EsiasentoVO> esiasentos, Map<Long, List<EsiasentoVO>>...esiasentosMap) {
        for (EsiasentoVO ea : esiasentos) {
            long kId = ea.getKameraId();
            for (Map<Long, List<EsiasentoVO>> eaMap : esiasentosMap) {
                List<EsiasentoVO> eas = eaMap.get(Long.valueOf(kId));
                if (eas == null) {
                    eas = new ArrayList<>();
                    eaMap.put(kId, eas);
                }
                eas.add(ea);
            }
        }
    }

    private List<KameraVO> readKameras(String filePath) {
        HaeKaikkiKameratResponse responseValue = (HaeKaikkiKameratResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( KameraVO k : responseValue.getKamerat() ) {
            Assert.assertEquals(0, k.getKokoonpanoId());
            Assert.assertEquals(0, k.getVideopalvelinId());

            Assert.assertNull(k.getAikakatkaisu());
            Assert.assertNull(k.getAliverkonPeite());
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

    private List<EsiasentoVO> readEsiasentos(String filePath) {
        HaeEsiasennotKameranTunnuksellaResponse responseValue = (HaeEsiasennotKameranTunnuksellaResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getEsiasennot();
    }

    public List<KameraVO> getInitialKameras() {
        return initialKameras;
    }

    public void setInitialKameras(List<KameraVO> initialKameras) {
        this.initialKameras = initialKameras;
    }

    public void setAfterChangeKameras(List<KameraVO> afterChangeKameras) {
        this.afterChangeKameras = afterChangeKameras;
    }

    public List<KameraVO> getAfterChangeKameras() {
        return afterChangeKameras;
    }

    public void setInitialEsiasentos(Map<Long, List<EsiasentoVO>> initialEsiasentos) {
        this.initialEsiasentos = initialEsiasentos;
    }

    public Map<Long, List<EsiasentoVO>> getInitialEsiasentos() {
        return initialEsiasentos;
    }

    /* KameraPerustiedot Service methods */

    @Override
    public KameraVO haeKamera(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKamera");
    }

    @Override
    public List<KameraVO> haeKameratVideopalvelimenTunnuksella(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public VideopalvelinVO haeVideopalvelin(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeVideopalvelin");
    }

    @Override
    public List<EsiasentoVO> haeEsiasennotKameranTunnuksella(Long id) throws KameraPerustiedotException {
        log.info("haeEsiasennotKameranTunnuksella " + id);
        if (isStateAfterChange()) {
            return getAfterChangeEsiasentos().get(id);
        }
        return getInitialEsiasentos().get(id);
    }

    @Override
    public EsiasentoVO haeEsiasento(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeEsiasento");
    }

    @Override
    public KameraKokoonpanoVO haeKokoonpanoKameranTunnuksella(Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKokoonpanoKameranTunnuksella");
    }

    @Override
    public List<KameraVO> haeKaikkiKamerat() throws KameraPerustiedotException {
        log.info("haeKaikkiKamerat isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangeKameras();
        }
        return getInitialKameras();
    }

    @Override
    public List<VideopalvelinVO> haeKaikkiVideopalvelimet() throws KameraPerustiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    public Map<Long, List<EsiasentoVO>> getAfterChangeEsiasentos() {
        return afterChangeEsiasentos;
    }

    public void setAfterChangeEsiasentos(Map<Long, List<EsiasentoVO>> afterChangeEsiasentos) {
        this.afterChangeEsiasentos = afterChangeEsiasentos;
    }
}
