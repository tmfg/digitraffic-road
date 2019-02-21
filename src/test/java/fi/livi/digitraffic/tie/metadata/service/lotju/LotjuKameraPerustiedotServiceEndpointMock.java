package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.NotImplementedException;
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
    private static final String LOTJU_KAMERA_RESOURCE_PATH = "lotju/kamera/";

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
        super(resourceLoader, metadataServerAddressCamera, KameraPerustiedotEndpoint.class,
              KameraPerustiedotEndpointImplService.SERVICE, jaxb2Marshaller, LOTJU_KAMERA_RESOURCE_PATH);
    }

    @Override
    protected Class<?> getObjectFactoryClass() {
        return ObjectFactory.class;
    }

    @Override
    public void initStateAndService() {
        if (!isInited()) {
            initService();
        }
        setStateAfterChange(false);
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
        HaeEsiasennotKameranTunnuksellaResponse response = readLotjuSoapResponse(HaeEsiasennotKameranTunnuksellaResponse.class, id);
        if (response != null) {
            return response.getEsiasennot();
        }
        return Collections.emptyList();
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
        HaeKaikkiKameratResponse response = readLotjuSoapResponse(HaeKaikkiKameratResponse.class);
        if (response != null) {
            return response.getKamerat();
        }
        return Collections.emptyList();
    }

    @Override
    public List<VideopalvelinVO> haeKaikkiVideopalvelimet() throws KameraPerustiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public EsiasentoVO muuttaaEsiasennonJulkisuus(Long id, boolean julkinen) throws KameraPerustiedotException {
        throw new NotImplementedException("muuttaaEsiasennonJulkisuus");
    }
}
