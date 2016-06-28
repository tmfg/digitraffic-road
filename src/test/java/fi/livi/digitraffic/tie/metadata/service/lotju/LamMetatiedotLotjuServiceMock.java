package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.lotju.wsdl.lam.ArvoVastaavuusVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LAMMetatiedotEndpoint;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LAMMetatiedotV1;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAnturiVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAsemaLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.lotju.wsdl.lam.ObjectFactory;

@Service
public class LamMetatiedotLotjuServiceMock extends LotjuServiceMock implements LAMMetatiedotEndpoint {

    private static final Logger log = Logger.getLogger(LamMetatiedotLotjuServiceMock.class);

    private List<LamAsemaVO> lamAsemasInitial;
    private List<LamAsemaVO> afterChangelamAsemas;

    @Autowired
    public LamMetatiedotLotjuServiceMock(@Value("${metadata.server.address.lam}")
                                         final String metadataServerAddressCamera,
                                         final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressCamera, LAMMetatiedotEndpoint.class, LAMMetatiedotV1.SERVICE);

    }

    @Override
    public void initDataAndService() {
        if (!isInited()) {
            initService();
            setLamAsemasInitial(readLamAsemas("lotju/lam/HaeKaikkiLAMAsematResponseInitial.xml"));
            setAfterChangelamAsemas(readLamAsemas("lotju/lam/HaeKaikkiLAMAsematResponseChanged.xml"));
        }
    }

    private List<LamAsemaVO> readLamAsemas(String filePath) {
            HaeKaikkiLAMAsematResponse responseValue = (HaeKaikkiLAMAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( LamAsemaVO k : responseValue.getAsemat() ) {
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

    public List<LamAsemaVO> getLamAsemasInitial() {
        return lamAsemasInitial;
    }

    public void setLamAsemasInitial(List<LamAsemaVO> lamAsemasInitial) {
        this.lamAsemasInitial = lamAsemasInitial;
    }

    public List<LamAsemaVO> getAfterChangelamAsemas() {
        return afterChangelamAsemas;
    }

    public void setAfterChangelamAsemas(List<LamAsemaVO> afterChangelamAsemas) {
        this.afterChangelamAsemas = afterChangelamAsemas;
    }

    /* LAMMetatiedot Service methods */

    @Override
    public LamAnturiVakioArvoVO haeAnturiVakioArvot(Long anturiVakioId, Integer paiva, Integer kuukausi) {
        throw new NotImplementedException("haeAnturiVakioArvot");
    }

    @Override
    public LamAsemaVO haeLAMAsema(Long id) {
        throw new NotImplementedException("haeLAMAsema");
    }

    @Override
    public List<LamAnturiVakioArvoVO> haeKaikkiAnturiVakioArvot(Integer paiva, Integer kuukausi) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public ArvoVastaavuusVO haeArvovastaavuus(Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVakioArvoVO> haeAsemanAnturiVakioArvot(Long asemaId, Integer paiva, Integer kuukausi) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public LamAnturiVO haeLAMAnturi(Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamLaskennallinenAnturiVO> haeKaikkiLAMLaskennallisetAnturit() {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<ArvoVastaavuusVO> haeLaskennallisenAnturinArvovastaavuudet(Long arg0) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<ArvoVastaavuusVO> haeKaikkiArvovastaavuudet() {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public LamAnturiVakioVO haeAnturiVakio(Long anturiVakioId) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVO> haeKaikkiLAMAnturit() {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamLaskennallinenAnturiVO> haeLAMAsemanLaskennallisetAnturit(Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAsemaLaskennallinenAnturiVO> haeLAMAsemanLaskennallistenAntureidenTilat(Long asemaId) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVO> haeLAMAsemanAnturit(Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAsemaVO> haeKaikkiLAMAsemat() {
        log.info("haeKaikkiLAMAsemat isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangelamAsemas();
        }
        return getLamAsemasInitial();
    }

    @Override
    public LamLaskennallinenAnturiVO haeLAMLaskennallinenAnturi(Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVakioVO> haeAsemanAnturiVakio(Long asemaId) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

}
