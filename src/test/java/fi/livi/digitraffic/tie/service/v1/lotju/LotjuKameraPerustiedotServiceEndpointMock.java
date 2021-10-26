package fi.livi.digitraffic.tie.service.v1.lotju;

import java.util.Collections;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.EsiasentoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeEsiasennotKameranTunnuksellaResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.HaeKaikkiKameratResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.JulkisuusTaso;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraKokoonpanoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraPerustiedotEndpoint;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraPerustiedotException;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraPerustiedotV7;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.KameraVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.kamera.VideopalvelinVO;

public class LotjuKameraPerustiedotServiceEndpointMock extends LotjuServiceEndpointMock implements KameraPerustiedotEndpoint {

    private static LotjuKameraPerustiedotServiceEndpointMock instance;
    private static final String LOTJU_KAMERA_RESOURCE_PATH = "lotju/kamera/";

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
              KameraPerustiedotV7.SERVICE, jaxb2Marshaller, LOTJU_KAMERA_RESOURCE_PATH);
    }

    @Override
    public void initStateAndService() {
        if (isNotInited()) {
            initService();
        }
        setStateAfterChange(false);
    }

    /* KameraPerustiedot Service methods */

    @Override
    public KameraVO haeKamera(final Long id) throws KameraPerustiedotException {
        return haeKaikkiKamerat().stream().filter(k -> k.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<KameraVO> haeKameratVideopalvelimenTunnuksella(final Long id) {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public VideopalvelinVO haeVideopalvelin(final Long id) {
        throw new NotImplementedException("haeVideopalvelin");
    }

    @Override
    public List<EsiasentoVO> haeEsiasennotKameranTunnuksella(final Long id) {
        HaeEsiasennotKameranTunnuksellaResponse response = readLotjuSoapResponse(HaeEsiasennotKameranTunnuksellaResponse.class, id);
        if (response != null) {
            return response.getEsiasennot();
        }
        return Collections.emptyList();
    }

    @Override
    public EsiasentoVO haeEsiasento(final Long id) {
        throw new NotImplementedException("haeEsiasento");
    }

    @Override
    public KameraKokoonpanoVO haeKokoonpanoKameranTunnuksella(final Long id) {
        throw new NotImplementedException("haeKokoonpanoKameranTunnuksella");
    }

    @Override
    public KameraVO muutaKameranJulkisuus(Long id, JulkisuusTaso julkisuusTaso, XMLGregorianCalendar alkaen) {
        throw new NotImplementedException("haeKameratVideopalvelimenTunnuksella");
    }

    @Override
    public List<KameraVO> haeKaikkiKamerat() {
        HaeKaikkiKameratResponse response = readLotjuSoapResponse(HaeKaikkiKameratResponse.class);
        if (response != null) {
            return response.getKamerat();
        }
        return Collections.emptyList();
    }

    @Override
    public List<VideopalvelinVO> haeKaikkiVideopalvelimet() {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public EsiasentoVO muuttaaEsiasennonJulkisuus(Long id, boolean julkinen) {
        throw new NotImplementedException("muuttaaEsiasennonJulkisuus");
    }
}
