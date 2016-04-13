package fi.livi.digitraffic.tie.metadata.service.lotju;

import java.util.List;

import org.apache.log4j.Logger;
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
    private List<LamAsema> lamAsemasChanged;

    @Autowired
    public LamMetatiedotLotjuServiceMock(@Value("${metadata.server.address.lam}")
                                         final String metadataServerAddressCamera,
                                         final ResourceLoader resourceLoader) {
        super(resourceLoader, metadataServerAddressCamera, LAMMetatiedot.class, LAMMetatiedotService.SERVICE);

        setLamAsemasInitial(readLamAsemas("lotju/HaeKaikkiLAMAsematResponseInitial.xml"));
        setLamAsemasChanged(readLamAsemas("lotju/HaeKaikkiLAMAsematResponseChanged.xml"));
    }

    private List<LamAsema> readLamAsemas(String filePath) {
            HaeKaikkiLAMAsematResponse responseValue = (HaeKaikkiLAMAsematResponse)readLotjuMetadataXml(filePath, ObjectFactory.class);
            return responseValue.getAsemat();
    }

    public List<LamAsema> getLamAsemasInitial() {
        return lamAsemasInitial;
    }

    public void setLamAsemasInitial(List<LamAsema> lamAsemasInitial) {
        this.lamAsemasInitial = lamAsemasInitial;
    }

    public List<LamAsema> getLamAsemasChanged() {
        return lamAsemasChanged;
    }

    public void setLamAsemasChanged(List<LamAsema> lamAsemasChanged) {
        this.lamAsemasChanged = lamAsemasChanged;
    }

    /* LAMMetatiedot Service methods */

    @Override
    public LamAnturiVakioArvo haeAnturiVakioArvot(Long anturiVakioId, Integer paiva, Integer kuukausi) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public LamAsema haeLAMAsema(Long id) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAnturiVakioArvo> haeKaikkiAnturiVakioArvot(Integer paiva, Integer kuukausi) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public LamArvoVastaavuus haeArvovastaavuus(Long id) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAnturiVakioArvo> haeAsemanAnturiVakioArvot(Long asemaId, Integer paiva, Integer kuukausi) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public LamAnturi haeLAMAnturi(Long id) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamLaskennallinenAnturi> haeKaikkiLAMLaskennallisetAnturit() throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamArvoVastaavuus> haeLaskennallisenAnturinArvovastaavuudet(Long arg0) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamArvoVastaavuus> haeKaikkiArvovastaavuudet() throws LAMMetatiedotException {
        return null;
    }

    @Override
    public LamAnturiVakio haeAnturiVakio(Long anturiVakioId) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAnturi> haeKaikkiLAMAnturit() throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamLaskennallinenAnturi> haeLAMAsemanLaskennallisetAnturit(Long id) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAsemaLaskennallinenAnturi> haeLAMAsemanLaskennallistenAntureidenTilat(Long asemaId) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAnturi> haeLAMAsemanAnturit(Long id) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAsema> haeKaikkiLAMAsemat() throws LAMMetatiedotException {
        if (isStateAfterChange()) {
            return getLamAsemasChanged();
        }
        return getLamAsemasInitial();
    }

    @Override
    public LamLaskennallinenAnturi haeLAMLaskennallinenAnturi(Long id) throws LAMMetatiedotException {
        return null;
    }

    @Override
    public List<LamAnturiVakio> haeAsemanAnturiVakio(Long asemaId) throws LAMMetatiedotException {
        return null;
    }

}
