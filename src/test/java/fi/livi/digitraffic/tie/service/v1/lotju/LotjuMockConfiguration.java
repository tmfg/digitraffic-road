package fi.livi.digitraffic.tie.service.v1.lotju;

import fi.livi.digitraffic.tie.conf.properties.LotjuMetadataProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@ConditionalOnNotWebApplication
@Configuration
public class LotjuMockConfiguration {

    @Autowired
    LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @Autowired
    LotjuTmsStationMetadataClient lotjuTmsStationMetadataClient;

    @Autowired
    LotjuWeatherStationMetadataClient lotjuWeatherStationMetadataClient;

    @Bean
    public LotjuKameraPerustiedotServiceEndpointMock lotjuKameraPerustiedotServiceEndpoint(
        final ResourceLoader resourceLoader,
        @Qualifier("kameraMetadataJaxb2Marshaller")
            final Jaxb2Marshaller kameraMetadataJaxb2Marshaller,
        final LotjuMetadataProperties lmp) {
        final String srv = lmp.getAddresses()[0];
        return LotjuKameraPerustiedotServiceEndpointMock.getInstance(srv + lmp.getPath().camera, resourceLoader, kameraMetadataJaxb2Marshaller);
    }

    @Bean
    public LotjuLAMMetatiedotServiceEndpointMock lotjuLAMMetatiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("lamMetadataJaxb2Marshaller")
            final Jaxb2Marshaller lamMetadataJaxb2Marshaller,
            final LotjuMetadataProperties lmp) {
        final String srv = lmp.getAddresses()[0];
        return LotjuLAMMetatiedotServiceEndpointMock.getInstance(srv + lmp.getPath().tms, resourceLoader, lamMetadataJaxb2Marshaller);
    }

    @Bean
    public LotjuTiesaaPerustiedotServiceEndpointMock lotjuTiesaaPerustiedotServiceEndpoint(
            final ResourceLoader resourceLoader,
            @Qualifier("tiesaaMetadataJaxb2Marshaller")
            final Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller,
            final LotjuMetadataProperties lmp) {
        final String srv = lmp.getAddresses()[0];
        return LotjuTiesaaPerustiedotServiceEndpointMock.getInstance(srv + lmp.getPath().weather, resourceLoader, tiesaaMetadataJaxb2Marshaller);
    }

}
