package fi.livi.digitraffic.tie.helper;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.wsdl.kamera.Kamera;
import fi.livi.digitraffic.tie.wsdl.lam.LamAsema;
import fi.livi.digitraffic.tie.wsdl.tiesaa.TiesaaAsema;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class ToStringHelpperTest {

    @Test
    public void testKameraToString() {
        Kamera kamera = new Kamera();
        kamera.setId(741L);
        kamera.setVanhaId(1501);
        kamera.setNimi("OLD_vt6_Lapinjärvi");
        Assert.assertEquals("Kamera: {\"lotjuId\":741,\"vanhaId\":1501,\"nimi\":\"OLD_vt6_Lapinjärvi\"}", ToStringHelpper.toString(kamera));
    }


    @Test
    public void testLamasemaToString() {
        LamAsema lam = new LamAsema();
        lam.setId(607L);
        lam.setVanhaId(23210);
        lam.setNimi("L_vt12_Vammala");
        Assert.assertEquals("LamAsema: {\"lotjuId\":607,\"vanhaId\":23210,\"nimi\":\"L_vt12_Vammala\"}", ToStringHelpper.toString(lam));
    }

    @Test
    public void testTiesaaasemaToString() {
        TiesaaAsema tsa = new TiesaaAsema();
        tsa.setId(607L);
        tsa.setVanhaId(23210);
        tsa.setNimi("L_vt12_Vammala");
        Assert.assertEquals("TiesaaAsema: {\"lotjuId\":607,\"vanhaId\":23210,\"nimi\":\"L_vt12_Vammala\"}", ToStringHelpper.toString(tsa));
    }

    @Test
    public void testToStringFull() {
        TiesaaAsema tsa = new TiesaaAsema();
        tsa.setId(607L);
        tsa.setVanhaId(23210);
        tsa.setNimi("L_vt12_Vammala");
        System.out.println(ToStringHelpper.toStringFull(tsa));
        Assert.assertEquals(
                "TiesaaAsema: {\"id\":607,\"aliasemaId\":null,\"antureillaKaapelikaivo\":null,\"antureillaPutkitukset\":null," +
                "\"anturiliitantaHuoltotarranMerkinnat\":null,\"anturiliitantaSarjanumero\":null,\"anturiliitantaValmistusviikko\":null," +
                "\"anturiliitantaValmistusvuosi\":null,\"anturiliitantayksikko\":null,\"ip\":null,\"julkinen\":null,\"kaapelikaivonKunto\":null," +
                "\"kehikko\":null,\"kehikonHuoltotarranMerkinnat\":null,\"kehikonSarjanumero\":null,\"kehikonValmistusviikko\":null," +
                "\"kehikonValmistusvuosi\":null,\"lisakuvaus\":null,\"master\":null,\"merkittavyys\":null,\"portti\":null,\"putkienMateriaali\":null," +
                "\"tyyppi\":null,\"ymparistoKuvaus\":null,\"aikakatkaisu\":null,\"aikavyohyke\":null,\"aliverkonPeite\":null,\"alkamisPaiva\":null," +
                "\"asemanSijainti\":null,\"asemanTila\":null,\"huoltoPuutteet\":null,\"huoltolevikkeenEtaisyysAsemasta\":null,\"keruuVali\":null," +
                "\"keruunTila\":null,\"korjaushuolto\":null,\"kunta\":null,\"kuntaKoodi\":null,\"laitekaappiId\":null,\"lisatieto\":null,\"liviId\":null," +
                "\"maa\":null,\"maakunta\":null,\"maakuntaKoodi\":null,\"nimiEn\":null,\"nimiFi\":null,\"nimiSe\":null,\"ohjelmistoversio\":null," +
                "\"paattymisPaiva\":null,\"riittavanKokoinenHuoltolevike\":null,\"vanhaId\":23210,\"verkkolaiteId\":null,\"vuosihuolto\":null," +
                "\"yhdyskaytava\":null,\"yhteysTapa\":null,\"korkeus\":null,\"latitudi\":null,\"longitudi\":null,\"tieosoite\":null,\"tieosoiteId\":null," +
                "\"kuvaus\":null,\"nimi\":\"L_vt12_Vammala\",\"takuunPaattymisPvm\":null}",
                ToStringHelpper.toStringFull(tsa));
    }

}
