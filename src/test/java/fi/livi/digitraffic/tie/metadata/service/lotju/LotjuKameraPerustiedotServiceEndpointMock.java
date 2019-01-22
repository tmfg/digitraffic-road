package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.ely.lotju.kamera.meta.service.ws.v7.KameraPerustiedotEndpointImplService;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06.EsiasentoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.HaeKaikkiKameratResponse;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.JulkisuusTaso;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraKokoonpanoVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraPerustiedotEndpoint;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraPerustiedotException;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.KameraVO;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.ObjectFactory;
import fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15.VideopalvelinVO;

public class LotjuKameraPerustiedotServiceEndpointMock extends LotjuServiceEndpointMock implements KameraPerustiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuKameraPerustiedotServiceEndpointMock.class);
    private static LotjuKameraPerustiedotServiceEndpointMock instance;

    private List<KameraVO> initialKameras;
    private List<KameraVO> afterChangeKameras;
    private Map<Long, List<EsiasentoVO>> initialEsiasentos = new HashMap<>();
    private Map<Long, List<EsiasentoVO>> afterChangeEsiasentos = new HashMap<>();

    public static LotjuKameraPerustiedotServiceEndpointMock getInstance(final String metadataServerAddressCamera, final ResourceLoader resourceLoader,
                                                                        final Jaxb2Marshaller jaxb2Marshaller) {
        if (instance == null) {
            instance = new LotjuKameraPerustiedotServiceEndpointMock(metadataServerAddressCamera, resourceLoader, jaxb2Marshaller);
        }
        return instance;
    }

    private LotjuKameraPerustiedotServiceEndpointMock(final String metadataServerAddressCamera, final ResourceLoader resourceLoader,
                                                      final Jaxb2Marshaller jaxb2Marshaller) {
        super(resourceLoader, metadataServerAddressCamera, KameraPerustiedotEndpoint.class, KameraPerustiedotEndpointImplService.SERVICE, jaxb2Marshaller);
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
        setStateAfterChange(false);
    }

    private void appendEsiasentos(final List<EsiasentoVO> esiasentos, final Map<Long, List<EsiasentoVO>>...esiasentosMap) {
        for (final EsiasentoVO ea : esiasentos) {
            final long kId = ea.getKameraId();
            for (final Map<Long, List<EsiasentoVO>> eaMap : esiasentosMap) {
                List<EsiasentoVO> eas = eaMap.get(kId);
                if (eas == null) {
                    eas = new ArrayList<>();
                    eaMap.put(kId, eas);
                }
                eas.add(ea);
            }
        }
    }

    private List<KameraVO> readKameras(final String filePath) {
        final HaeKaikkiKameratResponse responseValue = (HaeKaikkiKameratResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        // Tests that there isn't any forbidden fields included in test data
        for ( final KameraVO k : responseValue.getKamerat() ) {
            Assert.assertEquals(0, k.getKokoonpanoId());
            Assert.assertEquals(0, k.getVideopalvelinId());

            Assert.assertNull(k.getAliverkonPeite());
            Assert.assertNull(k.getHuoltolevikkeenEtaisyysAsemasta());
            Assert.assertNull(k.getHuoltoPuutteet());
            Assert.assertNull(k.getKorjaushuolto());
            Assert.assertNull(k.getLaitekaappiId());
            Assert.assertNull(k.getLisatieto());
            Assert.assertNull(k.getOhjelmistoversio());
            Assert.assertNull(k.getPaattymisPaiva());
            Assert.assertNull(k.getKuvaus());
            Assert.assertNull(k.getTakuunPaattymisPvm());
            Assert.assertNull(k.getVerkkolaiteId());
            Assert.assertNull(k.getYhdyskaytava());
            Assert.assertNull(k.getYhteysTapa());
            Assert.assertNull(k.isRiittavanKokoinenHuoltolevike());
        }
        return responseValue.getKamerat();
    }

    private List<EsiasentoVO> readEsiasentos(final String filePath) {
        final HaeEsiasennotKameranTunnuksellaResponse responseValue = (HaeEsiasennotKameranTunnuksellaResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getEsiasennot();
    }

    public List<KameraVO> getInitialKameras() {
        return initialKameras;
    }

    public void setInitialKameras(final List<KameraVO> initialKameras) {
        this.initialKameras = initialKameras;
    }

    public void setAfterChangeKameras(final List<KameraVO> afterChangeKameras) {
        this.afterChangeKameras = afterChangeKameras;
    }

    public List<KameraVO> getAfterChangeKameras() {
        return afterChangeKameras;
    }

    public void setInitialEsiasentos(final Map<Long, List<EsiasentoVO>> initialEsiasentos) {
        this.initialEsiasentos = initialEsiasentos;
    }

    public Map<Long, List<EsiasentoVO>> getInitialEsiasentos() {
        return initialEsiasentos;
    }

    /* KameraPerustiedot Service methods */

    @Override
    public KameraVO haeKamera(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKamera");
    }

    @Override
    public List<KameraVO> haeKameratVideopalvelimenTunnuksella(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public VideopalvelinVO haeVideopalvelin(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeVideopalvelin");
    }

    @Override
    public List<EsiasentoVO> haeEsiasennotKameranTunnuksella(final Long id) throws KameraPerustiedotException {
        log.info("haeEsiasennotKameranTunnuksella " + id);
        if (isStateAfterChange()) {
            return getAfterChangeEsiasentos().get(id);
        }
        return getInitialEsiasentos().get(id);
    }

    @Override
    public EsiasentoVO haeEsiasento(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeEsiasento");
    }

    @Override
    public KameraKokoonpanoVO haeKokoonpanoKameranTunnuksella(final Long id) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKokoonpanoKameranTunnuksella");
    }

    @Override
    public KameraVO muutaKameranJulkisuus(Long id, JulkisuusTaso julkisuusTaso, XMLGregorianCalendar alkaen) throws KameraPerustiedotException {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
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

    public void setAfterChangeEsiasentos(final Map<Long, List<EsiasentoVO>> afterChangeEsiasentos) {
        this.afterChangeEsiasentos = afterChangeEsiasentos;
    }

    @Override
    public EsiasentoVO muuttaaEsiasennonJulkisuus(Long id, boolean julkinen) throws KameraPerustiedotException {
        throw new NotImplementedException("muuttaaEsiasennonJulkisuus");
    }

    public Map<Long, List<EsiasentoVO>> getAfterChangeEsiasentos() {
        return afterChangeEsiasentos;
    }
}
