package fi.livi.digitraffic.tie.conf.jaxb2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class XmlMarshallerConfiguration {

    @Bean
    public Jaxb2Marshaller kameraMetadataJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
                // from WSDLs generate classes. Only higest
                "fi.livi.digitraffic.tie.external.lotju.metadata.kamera");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller kameraMetadataChangeJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // From xsds generated classes
            "fi.livi.digitraffic.tie.lotju.xsd.metatietomuutos.kameratietovirta");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller lamMetadataJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // from WSDLs generate classes. Only higest
            "fi.livi.digitraffic.tie.external.lotju.metadata.lam");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller tiesaaMetadataJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // from WSDLs generate classes. Only higest
            "fi.livi.digitraffic.tie.external.lotju.metadata.tiesaa");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller imsJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // From xsds generated classes
            "fi.livi.digitraffic.tie.external.tloik.ims.v1_2_0",
            "fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller datex2Jaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // From xsds generated classes
            "fi.livi.digitraffic.tie.datex2");
        return marshaller;
    }

    @Bean
    public Jaxb2Marshaller datex2ResponseJaxb2Marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
            // From xsds generated classes
            "fi.livi.digitraffic.tie.datex2.response");
        return marshaller;
    }
}