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

import fi.ely.lotju.tiesaa.meta.service.ws.v4.TiesaaPerustiedotEndpointImplService;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.AnturiSanomaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.ArvoVastaavuusVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.HaeKaikkiTiesaaAsematResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.ObjectFactory;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAsemaHakuparametrit;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAsemaLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02.TiesaaPerustiedotEndpoint;

public class LotjuTiesaaPerustiedotServiceEndpointMock extends LotjuServiceEndpointMock implements TiesaaPerustiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(LotjuTiesaaPerustiedotServiceEndpointMock.class);
    private static LotjuTiesaaPerustiedotServiceEndpointMock instance;

    private List<TiesaaAsemaVO> initialTiesaaAsemas;
    private List<TiesaaAsemaVO> afterChangeTiesaaAsemas;
    private Map<Long, List<TiesaaLaskennallinenAnturiVO>> initialTiesaaAnturisMap = new HashMap<>();
    private final Map<Long, List<TiesaaLaskennallinenAnturiVO>> afterChangeTiesaaAnturisMap = new HashMap<>();
    private List<TiesaaLaskennallinenAnturiVO> initialLaskennallisetAnturis;
    private List<TiesaaLaskennallinenAnturiVO> afterChangeLaskennallisetAnturis;

    public static LotjuTiesaaPerustiedotServiceEndpointMock getInstance(final String metadataServerAddressCamera,
                                                                        final ResourceLoader resourceLoader,
                                                                        final Jaxb2Marshaller jaxb2Marshaller) {
        if (instance == null) {
            instance = new LotjuTiesaaPerustiedotServiceEndpointMock(metadataServerAddressCamera, resourceLoader, jaxb2Marshaller);
        }
        return instance;
    }

    private LotjuTiesaaPerustiedotServiceEndpointMock(final String metadataServerAddressWeather, final ResourceLoader resourceLoader,
                                                      final Jaxb2Marshaller jaxb2Marshaller) {
        super(resourceLoader, metadataServerAddressWeather, TiesaaPerustiedotEndpoint.class, TiesaaPerustiedotEndpointImplService.SERVICE, jaxb2Marshaller);
    }

    @Override
    public void initDataAndService() {
        if (!isInited()) {
            initService();

            setInitialTiesaaAsemas(readTiesaaAsemas("lotju/tiesaa/HaeKaikkiTiesaaAsematResponseInitial.xml"));
            setAfterChangeTiesaaAsemas(readTiesaaAsemas("lotju/tiesaa/HaeKaikkiTiesaaAsematResponseChanged.xml"));

            setInitialLaskennallisetAnturis(readKaikkiLaskennallisetAnturit("lotju/tiesaa/HaeKaikkiLaskennallisetAnturitResponse.xml"));
            setAfterChangeLaskennallisetAnturis(readKaikkiLaskennallisetAnturit("lotju/tiesaa/HaeKaikkiLaskennallisetAnturitResponseChanged.xml"));

            appendTiesaaAnturis(33, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanLaskennallisetAnturitResponse33.xml"), initialTiesaaAnturisMap,
                    afterChangeTiesaaAnturisMap);
            appendTiesaaAnturis(34, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanLaskennallisetAnturitResponse34.xml"), initialTiesaaAnturisMap,
                    afterChangeTiesaaAnturisMap);
            appendTiesaaAnturis(35, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanLaskennallisetAnturitResponse35.xml"), initialTiesaaAnturisMap,
                    afterChangeTiesaaAnturisMap);
            appendTiesaaAnturis(36, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanLaskennallisetAnturitResponse36.xml"), initialTiesaaAnturisMap);
            appendTiesaaAnturis(36, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanLaskennallisetAnturitResponse36Changed.xml"), afterChangeTiesaaAnturisMap);
        }
        setStateAfterChange(false);
    }


    private void appendTiesaaAnturis(final long tsaId, final List<TiesaaLaskennallinenAnturiVO> tiesaaLaskennallinenAnturis, final Map<Long, List<TiesaaLaskennallinenAnturiVO>>... tiesaaAnturisMaps) {
        for (final TiesaaLaskennallinenAnturiVO tsa : tiesaaLaskennallinenAnturis) {

            for (final Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMap : tiesaaAnturisMaps) {
                List<TiesaaLaskennallinenAnturiVO> eas = tiesaaAnturisMap.get(Long.valueOf(tsaId));
                if (eas == null) {
                    eas = new ArrayList<>();
                    tiesaaAnturisMap.put(tsaId, eas);
                }
                eas.add(tsa);
            }
        }
    }

    private List<TiesaaAsemaVO> readTiesaaAsemas(final String filePath) {
        final HaeKaikkiTiesaaAsematResponse responseValue = (HaeKaikkiTiesaaAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        // Tests that there isn't any forbidden fields included in test data
        for ( final TiesaaAsemaVO k : responseValue.getTiesaaAsema() ) {
            Assert.assertNull(k.getAliasemaId());
            Assert.assertNull(k.getAnturiliitantaHuoltotarranMerkinnat());
            Assert.assertNull(k.getAnturiliitantaSarjanumero());
            Assert.assertNull(k.getAnturiliitantaValmistusviikko());
            Assert.assertNull(k.getAnturiliitantaValmistusvuosi());
            Assert.assertNull(k.getAnturiliitantayksikko());
            Assert.assertNull(k.isAntureillaPutkitukset());
            Assert.assertNull(k.isAntureillaKaapelikaivo());
            Assert.assertNull(k.getIp());
            Assert.assertNull(k.getKaapelikaivonKunto());
            Assert.assertNull(k.getKehikko());
            Assert.assertNull(k.getKehikonSarjanumero());
            Assert.assertNull(k.getKehikonHuoltotarranMerkinnat());
            Assert.assertNull(k.getKehikonValmistusviikko());
            Assert.assertNull(k.getKehikonValmistusvuosi());
            Assert.assertNull(k.getMerkittavyys());
            Assert.assertNull(k.getPortti());
            Assert.assertNull(k.getPutkienMateriaali());
            Assert.assertNull(k.getYmparistoKuvaus());

            Assert.assertNull(k.getAliverkonPeite());
            Assert.assertNull(k.getHuoltolevikkeenEtaisyysAsemasta());
            Assert.assertNull(k.getHuoltoPuutteet());
            Assert.assertNull(k.getLaitekaappiId());
            Assert.assertNull(k.getLisatieto());
            Assert.assertNull(k.getOhjelmistoversio());
            Assert.assertNull(k.getPaattymisPaiva());
            Assert.assertNull(k.getTakuunPaattymisPvm());
            Assert.assertNull(k.getVerkkolaiteId());
            Assert.assertNull(k.getYhdyskaytava());
            Assert.assertNull(k.getYhteysTapa());
            Assert.assertNull(k.isRiittavanKokoinenHuoltolevike());
        }
        return responseValue.getTiesaaAsema();
    }

    private List<TiesaaLaskennallinenAnturiVO> readKaikkiLaskennallisetAnturit(final String filePath) {
        final HaeKaikkiLaskennallisetAnturitResponse
                responseValue = (HaeKaikkiLaskennallisetAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getLaskennallinenAnturi();
    }

    private List<TiesaaLaskennallinenAnturiVO> readTiesaaAnturis(final String filePath) {
        final HaeTiesaaAsemanLaskennallisetAnturitResponse responseValue = (HaeTiesaaAsemanLaskennallisetAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getLaskennallinenAnturi();
    }


    /* TiesaaPerustiedot Service methods */

    public List<TiesaaAsemaVO> getInitialTiesaaAsemas() {
        return initialTiesaaAsemas;
    }

    public void setInitialTiesaaAsemas(final List<TiesaaAsemaVO> initialTiesaaAsemas) {
        this.initialTiesaaAsemas = initialTiesaaAsemas;
    }

    public void setAfterChangeTiesaaAsemas(final List<TiesaaAsemaVO> afterChangeTiesaaAsemas) {
        this.afterChangeTiesaaAsemas = afterChangeTiesaaAsemas;
    }

    public List<TiesaaAsemaVO> getAfterChangeTiesaaAsemas() {
        return afterChangeTiesaaAsemas;
    }

    public List<TiesaaLaskennallinenAnturiVO> getInitialLaskennallisetAnturis() {
        return initialLaskennallisetAnturis;
    }

    public void setInitialLaskennallisetAnturis(final List<TiesaaLaskennallinenAnturiVO> initialLaskennallisetAnturis) {
        this.initialLaskennallisetAnturis = initialLaskennallisetAnturis;
    }

    public void setAfterChangeLaskennallisetAnturis(final List<TiesaaLaskennallinenAnturiVO> afterChangeLaskennallisetAnturis) {
        this.afterChangeLaskennallisetAnturis = afterChangeLaskennallisetAnturis;
    }

    public List<TiesaaLaskennallinenAnturiVO> getAfterChangeLaskennallisetAnturis() {
        return afterChangeLaskennallisetAnturis;
    }

    public void setInitialTiesaaAnturisMap(final Map<Long, List<TiesaaLaskennallinenAnturiVO>> initialTiesaaAnturisMap) {
        this.initialTiesaaAnturisMap = initialTiesaaAnturisMap;
    }

    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getInitialTiesaaAnturisMap() {
        return initialTiesaaAnturisMap;
    }

    @Override
    public List<TiesaaLaskennallinenAnturiVO> haeTiesaaAsemanLaskennallisetAnturit(final Long id) {
        log.info("haeTiesaaAsemanLaskennallisetAnturit " + id);
        if (isStateAfterChange()) {
            return afterChangeTiesaaAnturisMap.get(Long.valueOf(id));
        }
        return initialTiesaaAnturisMap.get(Long.valueOf(id));
    }

    @Override
    public List<ArvoVastaavuusVO> haeKaikkiArvovastaavuudet() {
        throw new NotImplementedException("haeKaikkiArvovastaavuudet");
    }

    @Override
    public List<ArvoVastaavuusVO> haeLaskennallisenAnturinArvovastaavuudet(final Long arg0) {
        throw new NotImplementedException("haeLaskennallisenAnturinArvovastaavuudet");
    }

    @Override
    public ArvoVastaavuusVO haeArvovastaavuus(final Long id) {
        throw new NotImplementedException("haeArvovastaavuus");
    }

    @Override
    public List<TiesaaAnturiVO> haeTiesaaAsemanAnturit(final Long id) {
        throw new NotImplementedException("haeTiesaaAsemanAnturit");
    }

    @Override
    public List<TiesaaAsemaVO> haeKaikkiTiesaaAsemat() {
        log.info("haeKaikkiTiesaaAsemat isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangeTiesaaAsemas();
        }
        return getInitialTiesaaAsemas();
    }

    @Override
    public TiesaaAsemaVO haeTiesaaAsema(final Long id) {
        throw new NotImplementedException("haeTiesaaAsema");
    }

    @Override
    public TiesaaLaskennallinenAnturiVO haeLaskennallinenAnturi(final Long id) {
        throw new NotImplementedException("haeLaskennallinenAnturi");
    }

    @Override
    public List<TiesaaAsemaVO> haeTiesaaAsemat(final TiesaaAsemaHakuparametrit parametrit) {
        throw new NotImplementedException("haeTiesaaAsemat");
    }

    @Override
    public TiesaaAnturiVO haeAnturi(final Long id) {
        throw new NotImplementedException("haeAnturi");
    }

    @Override
    public List<AnturiSanomaVO> haeKaikkiAnturisanomat() {
        throw new NotImplementedException("haeKaikkiAnturisanomat");
    }

    @Override
    public List<TiesaaLaskennallinenAnturiVO> haeKaikkiLaskennallisetAnturit() {
        log.info("haeKaikkiLaskennallisetAnturit isStateAfterChange: " + isStateAfterChange());
        if (isStateAfterChange()) {
            return getAfterChangeLaskennallisetAnturis();
        }
        return getInitialLaskennallisetAnturis();
    }

    @Override
    public List<TiesaaAsemaLaskennallinenAnturiVO> haeTiesaaAsemanLaskennallistenAntureidenTilat(final Long asemaId) {
        throw new NotImplementedException("haeTiesaaAsemanLaskennallistenAntureidenTilat");
    }

    @Override
    public AnturiSanomaVO haeAnturisanoma(final Long id) {
        throw new NotImplementedException("haeAnturisanoma");
    }
}