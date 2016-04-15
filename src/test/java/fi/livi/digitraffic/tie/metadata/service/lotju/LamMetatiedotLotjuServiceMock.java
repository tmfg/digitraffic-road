package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.wsdl.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.wsdl.lam.LAMMetatiedot;
import fi.livi.digitraffic.tie.wsdl.lam.LAMMetatiedotException;
import fi.livi.digitraffic.tie.wsdl.lam.LAMMetatiedotService;
import fi.livi.digitraffic.tie.wsdl.lam.LamAnturi;
import fi.livi.digitraffic.tie.wsdl.lam.LamAnturiVakio;
import fi.livi.digitraffic.tie.wsdl.lam.LamAnturiVakioArvo;
import fi.livi.digitraffic.tie.wsdl.lam.LamArvoVastaavuus;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsemaLaskennallinenAnturi;
import fi.livi.digitraffic.tie.wsdl.lam.LamLaskennallinenAnturi;
import fi.livi.digitraffic.tie.wsdl.lam.ObjectFactory;

@Service
public class LamMetatiedotLotjuServiceMock extends LotjuServiceMock implements LAMMetatiedot {

    private static final Logger log = Logger.getLogger(LamMetatiedotLotjuServiceMock.class);

    private List<LamAsema> lamAsemasInitial;
    private List<LamAsema> afterChangelamAsemas;

    @Autowired
    public LamMetatiedotLotjuServiceMock(@Value("${metadata.server.address.lam}")
                                         final String metadataServerAddressCamera,
                                         final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressCamera, LAMMetatiedot.class, LAMMetatiedotService.SERVICE);

    }

    @Override
    public void initDataAndService() {
        if (!isInited()) {
            initService();
            setLamAsemasInitial(readLamAsemas("lotju/lam/HaeKaikkiLAMAsematResponseInitial.xml"));
            setAfterChangelamAsemas(readLamAsemas("lotju/lam/HaeKaikkiLAMAsematResponseChanged.xml"));
        }
    }

    private List<LamAsema> readLamAsemas(String filePath) {
            HaeKaikkiLAMAsematResponse responseValue = (HaeKaikkiLAMAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( LamAsema k : responseValue.getAsemat() ) {
            Assert.assertNull(k.getAkku());
            Assert.assertNull(k.getAkkuKayttoonottoVuosi());
            Assert.assertNull(k.getIp());
            Assert.assertNull(k.getLaskinlaite());
            Assert.assertNull(k.getLaskinlaiteSarjanumero());
            Assert.assertNull(k.getLaskinlaiteToimituspvm());
            Assert.assertNull(k.getLatauslaite());
            Assert.assertNull(k.getLatauslaiteKayttoonottoVuosi());
            Assert.assertNull(k.getLiitantayksikko());
            Assert.assertNull(k.isRiittavanKokoinenHuoltolevike());

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

        }

        return responseValue.getAsemat();
    }

    public List<LamAsema> getLamAsemasInitial() {
        return lamAsemasInitial;
    }

    public void setLamAsemasInitial(List<LamAsema> lamAsemasInitial) {
        this.lamAsemasInitial = lamAsemasInitial;
    }

    public List<LamAsema> getAfterChangelamAsemas() {
        return afterChangelamAsemas;
    }

    public void setAfterChangelamAsemas(List<LamAsema> afterChangelamAsemas) {
        this.afterChangelamAsemas = afterChangelamAsemas;
    }

    /* LAMMetatiedot Service methods */

    @Override
    public LamAnturiVakioArvo haeAnturiVakioArvot(Long anturiVakioId, Integer paiva, Integer kuukausi) throws LAMMetatiedotException {
        throw new NotImplementedException("haeAnturiVakioArvot");
    }

    @Override
    public LamAsema haeLAMAsema(Long id) throws LAMMetatiedotException {
        throw new NotImplementedException("haeLAMAsema");
    }

    @Override
    public List<LamAnturiVakioArvo> haeKaikkiAnturiVakioArvot(Integer paiva, Integer kuukausi) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public LamArvoVastaavuus haeArvovastaavuus(Long id) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVakioArvo> haeAsemanAnturiVakioArvot(Long asemaId, Integer paiva, Integer kuukausi) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public LamAnturi haeLAMAnturi(Long id) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamLaskennallinenAnturi> haeKaikkiLAMLaskennallisetAnturit() throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamArvoVastaavuus> haeLaskennallisenAnturinArvovastaavuudet(Long arg0) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamArvoVastaavuus> haeKaikkiArvovastaavuudet() throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public LamAnturiVakio haeAnturiVakio(Long anturiVakioId) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturi> haeKaikkiLAMAnturit() throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamLaskennallinenAnturi> haeLAMAsemanLaskennallisetAnturit(Long id) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAsemaLaskennallinenAnturi> haeLAMAsemanLaskennallistenAntureidenTilat(Long asemaId) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturi> haeLAMAsemanAnturit(Long id) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAsema> haeKaikkiLAMAsemat() throws LAMMetatiedotException {
        log.info("haeKaikkiLAMAsemat isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangelamAsemas();
        }
        return getLamAsemasInitial();
    }

    @Override
    public LamLaskennallinenAnturi haeLAMLaskennallinenAnturi(Long id) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVakio> haeAsemanAnturiVakio(Long asemaId) throws LAMMetatiedotException {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

}
