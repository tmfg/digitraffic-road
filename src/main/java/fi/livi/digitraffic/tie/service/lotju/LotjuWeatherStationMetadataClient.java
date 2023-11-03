package fi.livi.digitraffic.tie.service.lotju;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.common.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.conf.properties.LotjuMetadataProperties;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsemat;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeKaikkiTiesaaAsematResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeLaskennallinenAnturi;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeLaskennallinenAnturiResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsema;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsemaResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsemanLaskennallisetAnturit;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.HaeTiesaaAsemanLaskennallisetAnturitResponse;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.ObjectFactory;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaAsemaVO;
import fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa.TiesaaLaskennallinenAnturiVO;
import jakarta.xml.bind.JAXBElement;

@ConditionalOnNotWebApplication
@Component
public class LotjuWeatherStationMetadataClient extends AbstractLotjuMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(LotjuWeatherStationMetadataClient.class);
    final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public LotjuWeatherStationMetadataClient(@Qualifier("tiesaaMetadataJaxb2Marshaller")
                                             final Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller,
                                             final LotjuMetadataProperties lotjuMetadataProperties) {
        super(tiesaaMetadataJaxb2Marshaller, lotjuMetadataProperties, lotjuMetadataProperties.getPath().weather);
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    public List<TiesaaAsemaVO> getTiesaaAsemas() {
        log.info("Fetching TiesaaAsemas from " + getDefaultUri());

        final HaeKaikkiTiesaaAsemat request = new HaeKaikkiTiesaaAsemat();
        final JAXBElement<HaeKaikkiTiesaaAsematResponse> response = (JAXBElement<HaeKaikkiTiesaaAsematResponse>)
                marshalSendAndReceive(objectFactory.createHaeKaikkiTiesaaAsemat(request));

        log.info("roadStationFetchedCount={} TiesaaAsemas", response.getValue().getTiesaaAsema().size());
        return response.getValue().getTiesaaAsema();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 10000)
    @Retryable(maxAttempts = 5)
    public List<TiesaaLaskennallinenAnturiVO> getAllTiesaaLaskennallinenAnturis() {
        log.info("Fetching all LaskennallisetAnturit from " + getDefaultUri());

        final HaeKaikkiLaskennallisetAnturit request = new HaeKaikkiLaskennallisetAnturit();
        final JAXBElement<HaeKaikkiLaskennallisetAnturitResponse> response = (JAXBElement<HaeKaikkiLaskennallisetAnturitResponse>)
                marshalSendAndReceive(objectFactory.createHaeKaikkiLaskennallisetAnturit(request));

        return response.getValue().getLaskennallinenAnturi();
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Retryable(maxAttempts = 5)
    public List<TiesaaLaskennallinenAnturiVO> getTiesaaAsemanLaskennallisetAnturit(final Long tiesaaAsemaLotjuId) {
        final HaeTiesaaAsemanLaskennallisetAnturit request = new HaeTiesaaAsemanLaskennallisetAnturit();
        request.setId(tiesaaAsemaLotjuId);

        final JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse> response =
                (JAXBElement<HaeTiesaaAsemanLaskennallisetAnturitResponse>)
                        marshalSendAndReceive(objectFactory.createHaeTiesaaAsemanLaskennallisetAnturit(request));

        return response.getValue().getLaskennallinenAnturi();
    }

    @Retryable(maxAttempts = 5)
    public TiesaaAsemaVO getTiesaaAsema(final long lotjuId) {
        final HaeTiesaaAsema request = new HaeTiesaaAsema();
        request.setId(lotjuId);

        final JAXBElement<HaeTiesaaAsemaResponse> response =
            (JAXBElement<HaeTiesaaAsemaResponse>)
                marshalSendAndReceive(objectFactory.createHaeTiesaaAsema(request));

        return response.getValue().getTiesaaAsema();
    }

    public TiesaaLaskennallinenAnturiVO getTiesaaLaskennallinenAnturi(final Long lotjuId) {
        final HaeLaskennallinenAnturi request = new HaeLaskennallinenAnturi();
        request.setId(lotjuId);

        final JAXBElement<HaeLaskennallinenAnturiResponse> response =
            (JAXBElement<HaeLaskennallinenAnturiResponse>)
                marshalSendAndReceive(objectFactory.createHaeLaskennallinenAnturi(request));

        return response.getValue().getLaskennallinenAnturi();
    }
}
