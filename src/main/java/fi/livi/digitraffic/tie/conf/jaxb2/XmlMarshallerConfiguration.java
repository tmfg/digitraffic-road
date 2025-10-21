package fi.livi.digitraffic.tie.conf.jaxb2;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.xml.bind.Marshaller;

/**
 * Jaxb Marshallers used to convert incoming xml data to POJO
 */
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
        return createMarshaller("fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2");
    }

    @Bean
    public Jaxb2Marshaller datex2v2_3_5_jaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.datex2.v3_5");
    }

    @Bean
    public Jaxb2Marshaller datex2v2_2_3_fiJaxb2Marshaller() {
        return createMarshaller("fi.livi.digitraffic.tie.datex2.v2_2_3_fi");
    }

    private static Jaxb2Marshaller createMarshaller(final String...contextPaths) {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths(contextPaths);
        marshaller.setMarshallerProperties(Map.of(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE));
        return marshaller;
    }
}
