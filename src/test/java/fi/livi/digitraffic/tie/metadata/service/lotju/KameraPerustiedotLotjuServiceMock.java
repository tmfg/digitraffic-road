package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
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
import fi.livi.digitraffic.tie.wsdl.kamera.KameraTyyppi;
import fi.livi.digitraffic.tie.wsdl.kamera.KeruunTILA;
import fi.livi.digitraffic.tie.wsdl.kamera.ObjectFactory;
import fi.livi.digitraffic.tie.wsdl.kamera.Tieosoite;
import fi.livi.digitraffic.tie.wsdl.kamera.Videopalvelin;

@Service
public class KameraPerustiedotLotjuServiceMock extends LotjuServiceMock implements KameraPerustiedot {

    private static final Logger log = Logger.getLogger(KameraPerustiedotLotjuServiceMock.class);

    private List<Kamera> initialKamerat;
    private List<Kamera> afterChangeKamerat;
    private Map<Long, List<Esiasento>> initialEsiasentos = new HashMap<>();

    @Autowired
    public KameraPerustiedotLotjuServiceMock(@Value("${metadata.server.address.camera}")
                                                 final String metadataServerAddressCamera,
                                             final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressCamera, KameraPerustiedot.class, KameraPerustiedotEndpointService.SERVICE);

        setInitialKamerat(readKameras("lotju/HaeKaikkiKameratResponseInitial.xml"));
        setAfterChangeKamerat(readKameras("lotju/HaeKaikkiKameratResponseChanged.xml"));
        appendEsiasentos(readEsiasentos("lotju/HaeEsiasennotKameranTunnuksellaResponse2.xml"), initialEsiasentos);
        appendEsiasentos(readEsiasentos("lotju/HaeEsiasennotKameranTunnuksellaResponse121.xml"), initialEsiasentos);
        appendEsiasentos(readEsiasentos("lotju/HaeEsiasennotKameranTunnuksellaResponse443.xml"), initialEsiasentos);

    }

    private void appendEsiasentos(List<Esiasento> esiasentos, Map<Long, List<Esiasento>> initialEsiasentos) {
        for (Esiasento ea : esiasentos) {
            long kId = ea.getKameraId();
            List<Esiasento> eas = initialEsiasentos.get(Long.valueOf(kId));
            if (eas == null) {
                eas = new ArrayList<Esiasento>();
                initialEsiasentos.put(kId, eas);
            }
            eas.add(ea);
        }
    }

    private List<Kamera> readKameras(String filePath) {
        HaeKaikkiKameratResponse responseValue = (HaeKaikkiKameratResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getKamerat();
    }

    private List<Esiasento> readEsiasentos(String filePath) {
        HaeEsiasennotKameranTunnuksellaResponse responseValue = (HaeEsiasennotKameranTunnuksellaResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getEsiasennot();
    }

    private void initKamerat() {
        ArrayList<Kamera> kamerat = new ArrayList<Kamera>();
        Kamera k1 = new Kamera();
        k1.setId(443L);
        k1.setKeruunTila(KeruunTILA.KERUUSSA);
        k1.setTyyppi(KameraTyyppi.VAPIX);
        k1.setKunta("Iisalmi");
        k1.setKuntaKoodi("140");
        k1.setMaakunta("Pohjois-Savo");
        k1.setMaakuntaKoodi("11");
        k1.setKeruuVali(720);
        k1.setVanhaId(8520);
        k1.setKorkeus(BigDecimal.valueOf(10));
        k1.setLatitudi(BigDecimal.valueOf(7053606));
        k1.setLongitudi(BigDecimal.valueOf(495841));
        k1.setLahinTiesaaAsemaId(424L);

        k1.setNimi("vt27_Runni");
        k1.setNimiFi("Tie 27 Iisalmi, Runni");
        k1.setNimiEn("Road 27 Iisalmi, Runni");
        k1.setNimiSe("Väg 27 Idensalmi, Runni");

        k1.setTieosoiteId(81L);
        Tieosoite to1 = new Tieosoite();
        to1.setEtaisyysTieosanAlusta(303);
        to1.setTienumero(27);
        to1.setTieosa(26);
        k1.setTieosoite(to1);
        kamerat.add(k1);

        Kamera k2 = new Kamera();
        k2.setId(121L);
        k2.setKeruunTila(KeruunTILA.KERUUSSA);
        k2.setTyyppi(KameraTyyppi.VAPIX);
        k2.setKunta("Kotka");
        k2.setKuntaKoodi("285");
        k2.setMaakunta("Uusimaa");
        k2.setMaakuntaKoodi("1");
        k2.setKeruuVali(720);
        k2.setVanhaId(1628);
        k2.setKorkeus(BigDecimal.valueOf(0));
        k2.setLatitudi(BigDecimal.valueOf(6702975));
        k2.setLongitudi(BigDecimal.valueOf(455588));
        k2.setLahinTiesaaAsemaId(22L);

        k2.setNimi("vt7_Loviisa_Länsi");
        k2.setNimiFi("Tie 7 Loviisa_Länsi<");
        k2.setNimiEn("Road 7 Loviisa_Länsi");
        k2.setNimiSe("Väg 7 Loviisa_Länsi");

        k2.setTieosoiteId(592L);
        Tieosoite to2 = new Tieosoite();
        to2.setEtaisyysTieosanAlusta(0);
        to2.setTienumero(7);
        to2.setTieosa(19);
        k2.setTieosoite(to2);
        kamerat.add(k2);

        setInitialKamerat(kamerat);
    }

    private void initEsiasennot() {
        HashMap esiasentos = new HashMap();

        // Iisalmi
        List<Esiasento> eas1 = new ArrayList<>();
        esiasentos.put(443L, eas1);
        Esiasento ea1 = new Esiasento();
        ea1.setId(509L);
        ea1.setJarjestys(1);
        ea1.setJulkinen(true);
        ea1.setKameraId(443);
        ea1.setKeruussa(true);
        ea1.setKompressio(30);
        ea1.setNimiEsitys("Kiuruvedelle");
        ea1.setNimiLaitteella("Kiuruvedelle");
        ea1.setResoluutio("704x576");
        ea1.setSuunta("2");
        ea1.setViive(10);
        eas1.add(ea1);

        Esiasento ea2 = new Esiasento();
        ea2.setId(508L);
        ea2.setJarjestys(2);
        ea2.setJulkinen(true);
        ea2.setKameraId(443);
        ea2.setKeruussa(true);
        ea2.setKompressio(30);
        ea2.setNimiEsitys("Iisalmeen");
        ea2.setNimiLaitteella("iisalmi");
        ea2.setResoluutio("704x576");
        ea2.setSuunta("1");
        ea2.setViive(10);
        eas1.add(ea2);

        // Loviisa
        List<Esiasento> eas2 = new ArrayList<>();
        esiasentos.put(121L, eas2);
        Esiasento ea3 = new Esiasento();
        ea3.setId(1663L);
        ea3.setJarjestys(1);
        ea3.setJulkinen(true);
        ea3.setKameraId(121);
        ea3.setKeruussa(true);
        ea3.setKompressio(30);
        ea3.setNimiEsitys("Kotkaan");
        ea3.setNimiLaitteella("Kotkaan");
        ea3.setResoluutio("704x576");
        ea3.setSuunta("1");
        ea3.setOletussuunta(false);
        ea3.setViive(10);
        eas2.add(ea3);

        Esiasento ea4 = new Esiasento();
        ea4.setId(1781L);
        ea4.setJarjestys(2);
        ea4.setJulkinen(true);
        ea4.setKameraId(121);
        ea4.setKeruussa(true);
        ea4.setKompressio(30);
        ea4.setNimiEsitys("Helsinkiin");
        ea4.setNimiLaitteella("Helsinkiin");
        ea4.setResoluutio("704x576");
        ea4.setSuunta("2");
        ea4.setViive(10);
        eas2.add(ea4);

        setInitialEsiasentos(esiasentos);
    }

    public List<Kamera> getInitialKamerat() {
        return initialKamerat;
    }

    public void setInitialKamerat(List<Kamera> initialKamerat) {
        this.initialKamerat = initialKamerat;
    }

    public void setAfterChangeKamerat(List<Kamera> afterChangeKamerat) {
        this.afterChangeKamerat = afterChangeKamerat;
    }

    public List<Kamera> getAfterChangeKamerat() {
        return afterChangeKamerat;
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
            return getAfterChangeKamerat();
        }
        return getInitialKamerat();
    }

    @Override
    public List<Videopalvelin> haeKaikkiVideopalvelimet() throws KameraPerustiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

}
