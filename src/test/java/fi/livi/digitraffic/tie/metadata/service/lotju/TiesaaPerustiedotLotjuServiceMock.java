package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.wsdl.tiesaa.AnturiSanoma;
import fi.livi.digitraffic.tie.wsdl.tiesaa.ArvoVastaavuus;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.HaeTiesaaAsemanAnturitResponse;
import fi.livi.digitraffic.tie.wsdl.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAnturi;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsemaHakuparametrit;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsemaLaskennallinenAnturi;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaException;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaLaskennallinenAnturi;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaPerustiedot;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaPerustiedotService;

@Service
public class TiesaaPerustiedotLotjuServiceMock extends LotjuServiceMock implements TiesaaPerustiedot {

    private static final Logger log = Logger.getLogger(TiesaaPerustiedotLotjuServiceMock.class);

    private List<TiesaaAsema> initialTiesaaAsemas;
    private List<TiesaaAsema> afterChangeTiesaaAsemas;
    private Map<Long, List<TiesaaAnturi>> initialTiesaaAnturisMap = new HashMap<>();

    @Autowired
    public TiesaaPerustiedotLotjuServiceMock(@Value("${metadata.server.address.weather}")
                                             final String metadataServerAddressWeather,
                                             final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressWeather, TiesaaPerustiedot.class, TiesaaPerustiedotService.SERVICE);

        setInitialTiesaaAsemas(readTiesaaAsemas("lotju/tiesaa/HaeKaikkiTiesaaAsematResponseInitial.xml"));
        setAfterChangeTiesaaAsemas(readTiesaaAsemas("lotju/tiesaa/HaeKaikkiTiesaaAsematResponseChanged.xml"));
        appendTiesaaAnturis(readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse33.xml"), initialTiesaaAnturisMap);
        appendTiesaaAnturis(readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse34.xml"), initialTiesaaAnturisMap);
        appendTiesaaAnturis(readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse35.xml"), initialTiesaaAnturisMap);
        appendTiesaaAnturis(readTiesaaAnturis("lotju/tiesaa/HaeTiesaaAsemanAnturitResponse36.xml"), initialTiesaaAnturisMap);
    }

    private void appendTiesaaAnturis(List<TiesaaAnturi> tiesaaAnturis, Map<Long, List<TiesaaAnturi>> tiesaaAnturisMap) {
        for (TiesaaAnturi tsa : tiesaaAnturis) {
            long tsaId = tsa.getTiesaaAsemaId();
            List<TiesaaAnturi> eas = tiesaaAnturisMap.get(Long.valueOf(tsaId));
            if (eas == null) {
                eas = new ArrayList<>();
                tiesaaAnturisMap.put(tsaId, eas);
            }
            eas.add(tsa);
        }
    }

    private List<TiesaaAsema> readTiesaaAsemas(String filePath) {
        HaeKaikkiTiesaaAsematResponse responseValue = (HaeKaikkiTiesaaAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        for ( TiesaaAsema k : responseValue.getTiesaaAsema() ) {
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

    private List<TiesaaAnturi> readTiesaaAnturis(String filePath) {
        HaeTiesaaAsemanAnturitResponse responseValue = (HaeTiesaaAsemanAnturitResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
        return responseValue.getTiesaaAnturi();
    }


    /* TiesaaPerustiedot Service methods */

    public List<TiesaaAsema> getInitialTiesaaAsemas() {
        return initialTiesaaAsemas;
    }

    public void setInitialTiesaaAsemas(List<TiesaaAsema> initialTiesaaAsemas) {
        this.initialTiesaaAsemas = initialTiesaaAsemas;
    }

    public void setAfterChangeTiesaaAsemas(List<TiesaaAsema> afterChangeTiesaaAsemas) {
        this.afterChangeTiesaaAsemas = afterChangeTiesaaAsemas;
    }

    public List<TiesaaAsema> getAfterChangeTiesaaAsemas() {
        return afterChangeTiesaaAsemas;
    }

    public void setInitialTiesaaAnturisMap(Map<Long, List<TiesaaAnturi>> initialTiesaaAnturisMap) {
        this.initialTiesaaAnturisMap = initialTiesaaAnturisMap;
    }

    public Map<Long, List<TiesaaAnturi>> getInitialTiesaaAnturisMap() {
        return initialTiesaaAnturisMap;
    }

    @Override
    public List<TiesaaLaskennallinenAnturi> haeTiesaaAsemanLaskennallisetAnturit(Long id) throws TiesaaException {
        throw new NotImplementedException("haeTiesaaAsemanLaskennallisetAnturit");
    }

    @Override
    public List<ArvoVastaavuus> haeKaikkiArvovastaavuudet() throws TiesaaException {
        throw new NotImplementedException("haeKaikkiArvovastaavuudet");
    }

    @Override
    public List<ArvoVastaavuus> haeLaskennallisenAnturinArvovastaavuudet(Long arg0) throws TiesaaException {
        throw new NotImplementedException("haeLaskennallisenAnturinArvovastaavuudet");
    }

    @Override
    public ArvoVastaavuus haeArvovastaavuus(Long id) throws TiesaaException {
        throw new NotImplementedException("haeArvovastaavuus");
    }

    @Override
    public List<TiesaaAnturi> haeTiesaaAsemanAnturit(Long id) throws TiesaaException {
        return initialTiesaaAnturisMap.get(Long.valueOf(id));
    }

    @Override
    public List<TiesaaAsema> haeKaikkiTiesaaAsemat() throws TiesaaException {
        if (isStateAfterChange()) {
            return getAfterChangeTiesaaAsemas();
        }
        return getInitialTiesaaAsemas();
    }

    @Override
    public TiesaaAsema haeTiesaaAsema(Long id) throws TiesaaException {
        throw new NotImplementedException("haeTiesaaAsema");
    }

    @Override
    public TiesaaLaskennallinenAnturi haeLaskennallinenAnturi(Long id) throws TiesaaException {
        throw new NotImplementedException("haeLaskennallinenAnturi");
    }

    @Override
    public List<TiesaaAsema> haeTiesaaAsemat(TiesaaAsemaHakuparametrit parametrit) {
        throw new NotImplementedException("haeTiesaaAsemat");
    }

    @Override
    public TiesaaAnturi haeAnturi(Long id) throws TiesaaException {
        throw new NotImplementedException("haeAnturi");
    }

    @Override
    public List<AnturiSanoma> haeKaikkiAnturisanomat() throws TiesaaException {
        throw new NotImplementedException("haeKaikkiAnturisanomat");
    }

    @Override
    public List<TiesaaLaskennallinenAnturi> haeKaikkiLaskennallisetAnturit() throws TiesaaException {
        throw new NotImplementedException("haeKaikkiLaskennallisetAnturit");
    }

    @Override
    public List<TiesaaAsemaLaskennallinenAnturi> haeTiesaaAsemanLaskennallistenAntureidenTilat(Long asemaId) throws TiesaaException {
        throw new NotImplementedException("haeTiesaaAsemanLaskennallistenAntureidenTilat");
    }

    @Override
    public AnturiSanoma haeAnturisanoma(Long id) throws TiesaaException {
        throw new NotImplementedException("haeAnturisanoma");
    }
}
