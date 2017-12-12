package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioArvoVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamAnturiVakioVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06.LamLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.LamAsemaLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.ArvoVastaavuusVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMAsematResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LAMMetatiedotEndpoint;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LAMMetatiedotV3;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.LamAsemaVO;
import fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06.ObjectFactory;


public class LotjuLAMMetatiedotServiceMockEndpoint extends LotjuServiceEndpoint implements LAMMetatiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuLAMMetatiedotServiceMockEndpoint.class);
    private static LotjuLAMMetatiedotServiceMockEndpoint instance;

    private List<LamAsemaVO> initialLamAsemas;
    private List<LamAsemaVO> afterChangeLamAsemas;
    private List<LamLaskennallinenAnturiVO> initialLAMLaskennallisetAnturis;
    private List<LamLaskennallinenAnturiVO> afterChangeLAMLaskennallisetAnturis;
    private Map<Long, List<LamLaskennallinenAnturiVO>> initialLamAsemasSensorsMap = new HashMap<>();
    private final Map<Long, List<LamLaskennallinenAnturiVO>> afterChangeLamAsemasAnturisMap = new HashMap<>();


    public static LotjuLAMMetatiedotServiceMockEndpoint getInstance(final String metadataServerAddressCamera, final ResourceLoader resourceLoader,
                                                                final Jaxb2Marshaller jaxb2Marshaller) {
        if (instance == null) {
            instance = new LotjuLAMMetatiedotServiceMockEndpoint(metadataServerAddressCamera, resourceLoader, jaxb2Marshaller);
        }
        return instance;
    }

    private LotjuLAMMetatiedotServiceMockEndpoint(final String metadataServerAddressCamera, final ResourceLoader resourceLoader,
                                              final Jaxb2Marshaller jaxb2Marshaller) {
        super(resourceLoader, metadataServerAddressCamera, LAMMetatiedotEndpoint.class, LAMMetatiedotV3.SERVICE, jaxb2Marshaller);
    }

    @Override
    public void initDataAndService() {
        if (!isInited()) {
            initService();
            setInitialLamAsemas(readLamAsemas("lotju/lam/HaeKaikkiLAMAsematResponseInitial.xml"));
            setAfterChangeLamAsemas(readLamAsemas("lotju/lam/HaeKaikkiLAMAsematResponseChanged.xml"));
            setInitialLAMLaskennallisetAnturis(readLamLaskennallinenAnturis("lotju/lam/HaeKaikkiLAMLaskennallisetAnturitResponse.xml"));
            setAfterChangeLAMLaskennallisetAnturis(readLamLaskennallinenAnturis("lotju/lam/HaeKaikkiLAMLaskennallisetAnturitResponseChanged.xml"));

            appendTiesaaAnturis(
                    1,
                    readLamAsemasLaskennallinenAnturis("lotju/lam/HaeLAMAsemanLaskennallisetAnturitResponse1.xml"),
                    initialLamAsemasSensorsMap);
            appendTiesaaAnturis(
                1,
                readLamAsemasLaskennallinenAnturis("lotju/lam/HaeLAMAsemanLaskennallisetAnturitResponse1Changed.xml"),
                afterChangeLamAsemasAnturisMap);
            appendTiesaaAnturis(
                    310,
                    readLamAsemasLaskennallinenAnturis("lotju/lam/HaeLAMAsemanLaskennallisetAnturitResponse310.xml"),
                    initialLamAsemasSensorsMap,
                    afterChangeLamAsemasAnturisMap);
        }
        setStateAfterChange(false);
    }

    private List<LamAsemaVO> readLamAsemas(final String filePath) {
            final HaeKaikkiLAMAsematResponse responseValue = (HaeKaikkiLAMAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( final LamAsemaVO k : responseValue.getAsemat() ) {
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

    private List<LamLaskennallinenAnturiVO> readLamLaskennallinenAnturis(final String filePath) {
        final HaeKaikkiLAMLaskennallisetAnturitResponse
                responseValue = (HaeKaikkiLAMLaskennallisetAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getLaskennallinenAnturi();
    }

    private List<LamLaskennallinenAnturiVO> readLamAsemasLaskennallinenAnturis(final String filePath) {
        final HaeLAMAsemanLaskennallisetAnturitResponse responseValue = (HaeLAMAsemanLaskennallisetAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getLamlaskennallisetanturit();
    }

    private void appendTiesaaAnturis(final long tsaId, final List<LamLaskennallinenAnturiVO> tiesaaLaskennallinenAnturis, final Map<Long, List<LamLaskennallinenAnturiVO>>... tiesaaAnturisMaps) {
        for (final LamLaskennallinenAnturiVO tsa : tiesaaLaskennallinenAnturis) {

            for (final Map<Long, List<LamLaskennallinenAnturiVO>> tiesaaAnturisMap : tiesaaAnturisMaps) {
                List<LamLaskennallinenAnturiVO> eas = tiesaaAnturisMap.get(tsaId);
                if (eas == null) {
                    eas = new ArrayList<>();
                    tiesaaAnturisMap.put(tsaId, eas);
                }
                eas.add(tsa);
            }
        }
    }

    public List<LamAsemaVO> getInitialLamAsemas() {
        return initialLamAsemas;
    }

    public void setInitialLamAsemas(final List<LamAsemaVO> initialLamAsemas) {
        this.initialLamAsemas = initialLamAsemas;
    }

    public List<LamAsemaVO> getAfterChangeLamAsemas() {
        return afterChangeLamAsemas;
    }

    public void setAfterChangeLamAsemas(final List<LamAsemaVO> afterChangeLamAsemas) {
        this.afterChangeLamAsemas = afterChangeLamAsemas;
    }

    public void setInitialLAMLaskennallisetAnturis(List<LamLaskennallinenAnturiVO> initialLAMLaskennallisetAnturis) {
        this.initialLAMLaskennallisetAnturis = initialLAMLaskennallisetAnturis;
    }

    public List<LamLaskennallinenAnturiVO> getInitialLAMLaskennallisetAnturis() {
        return initialLAMLaskennallisetAnturis;
    }

    public void setAfterChangeLAMLaskennallisetAnturis(List<LamLaskennallinenAnturiVO> afterChangeLAMLaskennallisetAnturis) {
        this.afterChangeLAMLaskennallisetAnturis = afterChangeLAMLaskennallisetAnturis;
    }

    public List<LamLaskennallinenAnturiVO> getAfterChangeLAMLaskennallisetAnturis() {
        return afterChangeLAMLaskennallisetAnturis;
    }

    /* LAMMetatiedot Service methods */

    @Override
    public LamAnturiVakioArvoVO haeAnturiVakioArvot(final Long anturiVakioId, final Integer paiva, final Integer kuukausi) {
        throw new NotImplementedException("haeAnturiVakioArvot");
    }

    @Override
    public LamAsemaVO haeLAMAsema(final Long id) {
        throw new NotImplementedException("haeLAMAsema");
    }

    @Override
    public List<LamAnturiVakioArvoVO> haeKaikkiAnturiVakioArvot(final Integer paiva, final Integer kuukausi) {
        throw new NotImplementedException("haeKaikkiAnturiVakioArvot");
    }

    @Override
    public ArvoVastaavuusVO haeArvovastaavuus(final Long id) {
        throw new NotImplementedException("haeArvovastaavuus");
    }

    @Override
    public List<LamAnturiVakioArvoVO> haeAsemanAnturiVakioArvot(final Long asemaId, final Integer paiva, final Integer kuukausi) {
        throw new NotImplementedException("haeAsemanAnturiVakioArvot");
    }

    @Override
    public LamAnturiVO haeLAMAnturi(final Long id) {
        throw new NotImplementedException("haeLAMAnturi");
    }

    @Override
    public List<LamLaskennallinenAnturiVO> haeKaikkiLAMLaskennallisetAnturit() {
        log.info("haeKaikkiLAMLaskennallisetAnturit isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangeLAMLaskennallisetAnturis();
        }
        return getInitialLAMLaskennallisetAnturis();
    }

    @Override
    public List<ArvoVastaavuusVO> haeLaskennallisenAnturinArvovastaavuudet(final Long arg0) {
        throw new NotImplementedException("haeLaskennallisenAnturinArvovastaavuudet");
    }

    @Override
    public List<ArvoVastaavuusVO> haeKaikkiArvovastaavuudet() {
        throw new NotImplementedException("haeKaikkiArvovastaavuudet");
    }

    @Override
    public LamAnturiVakioVO haeAnturiVakio(final Long anturiVakioId) {
        throw new NotImplementedException("haeAnturiVakio");
    }

    @Override
    public List<LamAnturiVO> haeKaikkiLAMAnturit() {
        throw new NotImplementedException("haeKaikkiLAMAnturit");
    }

    @Override
    public List<LamLaskennallinenAnturiVO> haeLAMAsemanLaskennallisetAnturit(final Long id) {
        log.info("haeLAMAsemanLaskennallisetAnturit " + id + " isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return afterChangeLamAsemasAnturisMap.get(id);
        }
        return initialLamAsemasSensorsMap.get(id);
    }

    @Override
    public List<LamAsemaLaskennallinenAnturiVO> haeLAMAsemanLaskennallistenAntureidenTilat(final Long asemaId) {
        throw new NotImplementedException("haeLAMAsemanLaskennallistenAntureidenTilat");
    }

    @Override
    public List<LamAnturiVO> haeLAMAsemanAnturit(final Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAsemaVO> haeKaikkiLAMAsemat() {
        log.info("haeKaikkiLAMAsemat isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangeLamAsemas();
        }
        return getInitialLamAsemas();
    }

    @Override
    public LamLaskennallinenAnturiVO haeLAMLaskennallinenAnturi(final Long id) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

    @Override
    public List<LamAnturiVakioVO> haeAsemanAnturiVakio(final Long asemaId) {
        throw new NotImplementedException("haeKaikkiVideopalvelimet");
    }

}
