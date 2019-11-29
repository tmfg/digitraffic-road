package fi.livi.digitraffic.tie.conf.jaxb2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class MetadataMarshallerConfiguration {
    @Bean
    public Jaxb2Marshaller marshaller() {
        final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setContextPaths(
                // from WSDLs generate classes. Only higest
                "fi.livi.ws.wsdl.lotju.kamerametatiedot._2018._06._15",
                "fi.livi.ws.wsdl.lotju.lammetatiedot._2018._03._12",
                "fi.livi.ws.wsdl.lotju.tiesaa._2017._05._02",
                // From xsds generated classes
                "fi.livi.digitraffic.tie.lotju.xsd.datex2",
                "fi.livi.digitraffic.tie.lotju.xsd.datex2.response",
                "fi.livi.digitraffic.tie.lotju.xsd.metatietomuutos.kameratietovirta");
        return marshaller;
    }
}