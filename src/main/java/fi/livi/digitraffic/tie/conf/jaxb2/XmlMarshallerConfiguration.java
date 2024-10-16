package fi.livi.digitraffic.tie.conf.jaxb2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class XmlMarshallerConfiguration {

    @Bean
    public Jaxb2Marshaller kameraMetadataJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.lotju.metadata.kamera");
    }

    @Bean
    public Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.lotju.metatietomuutos.kamera.tietovirta");
    }

    @Bean
    public Jaxb2Marshaller lamMetadataChangeJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.lotju.metatietomuutos.lam.tietovirta");
    }

    @Bean
    public Jaxb2Marshaller tiesaaMetadataChangeJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.lotju.metatietomuutos.tiesaa.tietovirta");
    }

    @Bean
    public Jaxb2Marshaller lamMetadataJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.lotju.metadata.lam");
    }

    @Bean
    public Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa");
    }

    @Bean
    public Jaxb2Marshaller imsJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.tloik.ims.v1_2_0",
                                "fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1");
    }

    @Bean
    public Jaxb2Marshaller datex2v2_2_3_fiJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.datex2.v2_2_3_fi");
    }

    @Bean
    public Jaxb2Marshaller datex2v3_5Jaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.external.datex2.v3_5");
    }

    private static Jaxb2Marshaller createMarshaller(final String...contextPaths) {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths(contextPaths);
        return marshaller;
    }
}
