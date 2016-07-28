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

import fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29.TiesaaAsemaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.AnturiSanomaVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.ArvoVastaavuusVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeKaikkiTiesaaAsematResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.ObjectFactory;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaAsemaHakuparametrit;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaAsemaLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaLaskennallinenAnturiVO;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaPerustiedotEndpoint;
import fi.livi.ws.wsdl.lotju.tiesaa._2015._09._29.TiesaaPerustiedotV2;

@Service
public class TiesaaPerustiedotLotjuServiceMock extends LotjuServiceMock implements TiesaaPerustiedotEndpoint {

    private static final Logger log = LoggerFactory.getLogger(TiesaaPerustiedotLotjuServiceMock.class);

    private List<TiesaaAsemaVO> initialTiesaaAsemas;
    private List<TiesaaAsemaVO> afterChangeTiesaaAsemas;
    private Map<Long, List<TiesaaLaskennallinenAnturiVO>> initialTiesaaAnturisMap = new HashMap<>();
    private Map<Long, List<TiesaaLaskennallinenAnturiVO>> afterChangeTiesaaAnturisMap = new HashMap<>();
    private List<TiesaaLaskennallinenAnturiVO> initialLaskennallisetAnturis;
    private List<TiesaaLaskennallinenAnturiVO> afterChangeLaskennallisetAnturis;

    @Autowired
    public TiesaaPerustiedotLotjuServiceMock(@Value("${metadata.server.address.weather}")
                                             final String metadataServerAddressWeather,
                                             final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressWeather, TiesaaPerustiedotEndpoint.class, TiesaaPerustiedotV2.SERVICE);
    }

    @Override
    public void initDataAndService() {
        if (!isInited()) {
            initService();

            setInitialTiesaaAsemas(readTiesaaAsemas("lotju/tiesaa/HaeKaikkiTiesaaAsematResponseInitial.xml"));
            setAfterChangeTiesaaAsemas(readTiesaaAsemas("lotju/tiesaa/HaeKaikkiTiesaaAsematResponseChanged.xml"));

            setInitialLaskennallisetAnturis(readKaikkiLaskennallisetAnturit("lotju/tiesaa/HaeKaikkiLaskennallisetAnturitResponse.xml"));
            setAfterChangeLaskennallisetAnturis(readKaikkiLaskennallisetAnturit("lotju/tiesaa/HaeKaikkiLaskennallisetAnturitResponseChanged.xml"));

            appendTiesaaAnturis(33, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse33.xml"), initialTiesaaAnturisMap,
                    afterChangeTiesaaAnturisMap);
            appendTiesaaAnturis(34, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse34.xml"), initialTiesaaAnturisMap,
                    afterChangeTiesaaAnturisMap);
            appendTiesaaAnturis(35, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse35.xml"), initialTiesaaAnturisMap,
                    afterChangeTiesaaAnturisMap);
            appendTiesaaAnturis(36, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse36.xml"), initialTiesaaAnturisMap);
            appendTiesaaAnturis(36, readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse36Changed.xml"), afterChangeTiesaaAnturisMap);
        }
    }


    private void appendTiesaaAnturis(long tsaId, List<TiesaaLaskennallinenAnturiVO> tiesaaLaskennallinenAnturis, Map<Long, List<TiesaaLaskennallinenAnturiVO>>... tiesaaAnturisMaps) {
        for (TiesaaLaskennallinenAnturiVO tsa : tiesaaLaskennallinenAnturis) {

            for (Map<Long, List<TiesaaLaskennallinenAnturiVO>> tiesaaAnturisMap : tiesaaAnturisMaps) {
                List<TiesaaLaskennallinenAnturiVO> eas = tiesaaAnturisMap.get(Long.valueOf(tsaId));
                if (eas == null) {
                    eas = new ArrayList<>();
                    tiesaaAnturisMap.put(tsaId, eas);
                }
                eas.add(tsa);
            }
        }
    }

    private List<TiesaaAsemaVO> readTiesaaAsemas(String filePath) {
        HaeKaikkiTiesaaAsematResponse responseValue = (HaeKaikkiTiesaaAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( TiesaaAsemaVO k : responseValue.getTiesaaAsema() ) {
            Assert.assertNull(k.getAliasemaId());
            Assert.assertNull(k.getAnturiliitantaHuoltotarranMerkinnat());
            Assert.assertNull(k.getAnturiliitantaSarjanumero());
            Assert.assertNull(k.getAnturiliitantaValmistusviikko());
            Assert.assertNull(k.getAnturiliitantaValmistusvuosi());
            Assert.assertNull(k.getAnturiliitantayksikko());
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
        return responseValue.getTiesaaAsema();
    }

    private List<TiesaaLaskennallinenAnturiVO> readKaikkiLaskennallisetAnturit(final String filePath) {
        HaeKaikkiLaskennallisetAnturitResponse
                responseValue = (HaeKaikkiLaskennallisetAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getLaskennallinenAnturi();
    }

    private List<TiesaaLaskennallinenAnturiVO> readTiesaaAnturis(String filePath) {
        HaeTiesaaAsemanLaskennallisetAnturitResponse responseValue = (HaeTiesaaAsemanLaskennallisetAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getLaskennallinenAnturi();
    }


    /* TiesaaPerustiedot Service methods */

    public List<TiesaaAsemaVO> getInitialTiesaaAsemas() {
        return initialTiesaaAsemas;
    }

    public void setInitialTiesaaAsemas(List<TiesaaAsemaVO> initialTiesaaAsemas) {
        this.initialTiesaaAsemas = initialTiesaaAsemas;
    }

    public void setAfterChangeTiesaaAsemas(List<TiesaaAsemaVO> afterChangeTiesaaAsemas) {
        this.afterChangeTiesaaAsemas = afterChangeTiesaaAsemas;
    }

    public List<TiesaaAsemaVO> getAfterChangeTiesaaAsemas() {
        return afterChangeTiesaaAsemas;
    }

    public List<TiesaaLaskennallinenAnturiVO> getInitialLaskennallisetAnturis() {
        return initialLaskennallisetAnturis;
    }

    public void setInitialLaskennallisetAnturis(List<TiesaaLaskennallinenAnturiVO> initialLaskennallisetAnturis) {
        this.initialLaskennallisetAnturis = initialLaskennallisetAnturis;
    }

    public void setAfterChangeLaskennallisetAnturis(List<TiesaaLaskennallinenAnturiVO> afterChangeLaskennallisetAnturis) {
        this.afterChangeLaskennallisetAnturis = afterChangeLaskennallisetAnturis;
    }

    public List<TiesaaLaskennallinenAnturiVO> getAfterChangeLaskennallisetAnturis() {
        return afterChangeLaskennallisetAnturis;
    }

    public void setInitialTiesaaAnturisMap(Map<Long, List<TiesaaLaskennallinenAnturiVO>> initialTiesaaAnturisMap) {
        this.initialTiesaaAnturisMap = initialTiesaaAnturisMap;
    }

    public Map<Long, List<TiesaaLaskennallinenAnturiVO>> getInitialTiesaaAnturisMap() {
        return initialTiesaaAnturisMap;
    }

    @Override
    public List<TiesaaLaskennallinenAnturiVO> haeTiesaaAsemanLaskennallisetAnturit(Long id) {
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
    public List<ArvoVastaavuusVO> haeLaskennallisenAnturinArvovastaavuudet(Long arg0) {
        throw new NotImplementedException("haeLaskennallisenAnturinArvovastaavuudet");
    }

    @Override
    public ArvoVastaavuusVO haeArvovastaavuus(Long id) {
        throw new NotImplementedException("haeArvovastaavuus");
    }

    @Override
    public List<TiesaaAnturiVO> haeTiesaaAsemanAnturit(Long id) {
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
    public TiesaaAsemaVO haeTiesaaAsema(Long id) {
        throw new NotImplementedException("haeTiesaaAsema");
    }

    @Override
    public TiesaaLaskennallinenAnturiVO haeLaskennallinenAnturi(Long id) {
        throw new NotImplementedException("haeLaskennallinenAnturi");
    }

    @Override
    public List<TiesaaAsemaVO> haeTiesaaAsemat(TiesaaAsemaHakuparametrit parametrit) {
        throw new NotImplementedException("haeTiesaaAsemat");
    }

    @Override
    public TiesaaAnturiVO haeAnturi(Long id) {
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
    public List<TiesaaAsemaLaskennallinenAnturiVO> haeTiesaaAsemanLaskennallistenAntureidenTilat(Long asemaId) {
        throw new NotImplementedException("haeTiesaaAsemanLaskennallistenAntureidenTilat");
    }

    @Override
    public AnturiSanomaVO haeAnturisanoma(Long id) {
        throw new NotImplementedException("haeAnturisanoma");
    }
}
