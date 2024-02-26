package fi.livi.digitraffic.tie.service.lotju;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.annotation.NotTransactionalServiceMethod;
import fi.livi.digitraffic.tie.conf.properties.LotjuMetadataProperties;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAnturiVakio;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAnturiVakioArvot;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAnturiVakioArvotResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAnturiVakioResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakio;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakioArvot;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakioArvotResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeAsemanAnturiVakioResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiAnturiVakioArvot;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiAnturiVakioArvotResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMAsemat;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMAsematResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeKaikkiLAMLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsema;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsemaResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsemanLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMLaskennallinenAnturi;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.HaeLAMLaskennallinenAnturiResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioArvoVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAnturiVakioVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.LamLaskennallinenAnturiVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.lam.ObjectFactory;
import jakarta.xml.bind.JAXBElement;

@ConditionalOnNotWebApplication
@Service
public class LotjuTmsStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuTmsStationMetadataClient.class);
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuTmsStationMetadataClient(@Qualifier("lamMetadataJaxb2Marshaller")
                                         final Jaxb2Marshaller lamMetadataJaxb2Marshaller,
                                         final LotjuMetadataProperties lotjuMetadataProperties) {
        super(lamMetadataJaxb2Marshaller, lotjuMetadataProperties, lotjuMetadataProperties.getPath().tms);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public List<LamAsemaVO> getLamAsemas() {
        final HaeKaikkiLAMAsemat request = new HaeKaikkiLAMAsemat();

        log.info("Fetching LamAsemas");
        final JAXBElement<HaeKaikkiLAMAsematResponse> response = (JAXBElement<HaeKaikkiLAMAsematResponse>)
                marshalSendAndReceive(objectFactory.createHaeKaikkiLAMAsemat(request));
        log.info("lamFetchedCount={} LamAsemas", response.getValue().getAsemat().size());
        return response.getValue().getAsemat();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public LamAsemaVO getLamAsema(final long id) {
        final HaeLAMAsema request = new HaeLAMAsema();
        request.setId(id);

        log.info("method=getLamAsema id={}", id);
        final JAXBElement<HaeLAMAsemaResponse> response = (JAXBElement<HaeLAMAsemaResponse>)
            marshalSendAndReceive(objectFactory.createHaeLAMAsema(request));
        return response.getValue().getAsema();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public LamLaskennallinenAnturiVO getLamLaskennallinenAnturi(final long lotjuId) {
        final HaeLAMLaskennallinenAnturi request = new HaeLAMLaskennallinenAnturi();
        request.setId(lotjuId);
        final JAXBElement<HaeLAMLaskennallinenAnturiResponse> response = (JAXBElement<HaeLAMLaskennallinenAnturiResponse>)
            marshalSendAndReceive(objectFactory.createHaeLAMLaskennallinenAnturi(request));
        return response.getValue().getLamlaskennallinenanturi();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public List<LamLaskennallinenAnturiVO> getLamAsemanLaskennallisetAnturit(final long lamAsemaLotjuId) {

        final HaeLAMAsemanLaskennallisetAnturit request = new HaeLAMAsemanLaskennallisetAnturit();
        request.setId(lamAsemaLotjuId);
        final JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse> response = (JAXBElement<HaeLAMAsemanLaskennallisetAnturitResponse>)
                marshalSendAndReceive(objectFactory.createHaeLAMAsemanLaskennallisetAnturit(request));
        return response.getValue().getLamlaskennallisetanturit();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public List<LamLaskennallinenAnturiVO> getAllLamLaskennallinenAnturis() {
        final HaeKaikkiLAMLaskennallisetAnturit request = new HaeKaikkiLAMLaskennallisetAnturit();
        log.info("Fetching LAMLaskennallisetAnturis");
        final JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLAMLaskennallisetAnturitResponse>)
                marshalSendAndReceive(objectFactory.createHaeKaikkiLAMLaskennallisetAnturit(request));
        log.info("lamFetchedCount={} LAMLaskennallisetAnturis", response.getValue().getLaskennallinenAnturi().size());
        return response.getValue().getLaskennallinenAnturi();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public LamAnturiVakioVO getLamAnturiVakio(final long anturiVakiolotjuId) {
        final HaeAnturiVakio haeAnturiVakioRequest = new HaeAnturiVakio();
        haeAnturiVakioRequest.setAnturiVakioId(anturiVakiolotjuId);

        final JAXBElement<HaeAnturiVakioResponse> haeAsemanAnturiVakioResponse =
            (JAXBElement< HaeAnturiVakioResponse>)
                marshalSendAndReceive(objectFactory.createHaeAnturiVakio(haeAnturiVakioRequest));
        return haeAsemanAnturiVakioResponse.getValue().getLamanturivakio();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public List<LamAnturiVakioVO> getAsemanAnturiVakios(final Long lotjuId) {
        final HaeAsemanAnturiVakio haeAsemanAnturiVakioRequest =
            new HaeAsemanAnturiVakio();
        haeAsemanAnturiVakioRequest.setAsemaId(lotjuId);

        final JAXBElement<HaeAsemanAnturiVakioResponse> haeAsemanAnturiVakioResponse =
            (JAXBElement< HaeAsemanAnturiVakioResponse>)
                marshalSendAndReceive(objectFactory.createHaeAsemanAnturiVakio(haeAsemanAnturiVakioRequest));
        return haeAsemanAnturiVakioResponse.getValue().getLamanturivakiot();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public List<LamAnturiVakioArvoVO> getAllAnturiVakioArvos(final int month, final int dayOfMonth) {
        final HaeKaikkiAnturiVakioArvot haeKaikkiAnturiVakioArvotRequest =
            new HaeKaikkiAnturiVakioArvot();
        haeKaikkiAnturiVakioArvotRequest.setKuukausi(month);
        haeKaikkiAnturiVakioArvotRequest.setPaiva(dayOfMonth);

        final JAXBElement<HaeKaikkiAnturiVakioArvotResponse> haeKaikkiAnturiVakioArvotResponse =
            (JAXBElement<HaeKaikkiAnturiVakioArvotResponse>)
                marshalSendAndReceive(objectFactory.createHaeKaikkiAnturiVakioArvot(haeKaikkiAnturiVakioArvotRequest));
        return haeKaikkiAnturiVakioArvotResponse.getValue().getLamanturivakiot();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public List<LamAnturiVakioArvoVO> getAsemanAnturiVakioArvos(final long roadStationLotjuId, final int month, final int dayOfMonth) {
        final HaeAsemanAnturiVakioArvot haeAsemanAnturiVakioArvotRequest =
            new HaeAsemanAnturiVakioArvot();
        haeAsemanAnturiVakioArvotRequest.setAsemaId(roadStationLotjuId);
        haeAsemanAnturiVakioArvotRequest.setKuukausi(month);
        haeAsemanAnturiVakioArvotRequest.setPaiva(dayOfMonth);

        final JAXBElement<HaeAsemanAnturiVakioArvotResponse> haeAsemanAnturiVakioArvotResponse =
            (JAXBElement<HaeAsemanAnturiVakioArvotResponse>)
                marshalSendAndReceive(objectFactory.createHaeAsemanAnturiVakioArvot(haeAsemanAnturiVakioArvotRequest));
        return haeAsemanAnturiVakioArvotResponse.getValue().getLamanturivakiot();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    @NotTransactionalServiceMethod
    public LamAnturiVakioArvoVO getAnturiVakioArvot(final long anturiVakioLotjuId, final int month, final int dayOfMonth) {
        final HaeAnturiVakioArvot haeAnturiVakioArvot =
            new HaeAnturiVakioArvot();
        haeAnturiVakioArvot.setAnturiVakioId(anturiVakioLotjuId);
        haeAnturiVakioArvot.setKuukausi(month);
        haeAnturiVakioArvot.setPaiva(dayOfMonth);

        final JAXBElement<HaeAnturiVakioArvotResponse> haeAnturiVakioArvoResponse =
            (JAXBElement<HaeAnturiVakioArvotResponse>)
                marshalSendAndReceive(objectFactory.createHaeAnturiVakioArvot(haeAnturiVakioArvot));
        return haeAnturiVakioArvoResponse.getValue().getLamanturivakio();
    }
}
