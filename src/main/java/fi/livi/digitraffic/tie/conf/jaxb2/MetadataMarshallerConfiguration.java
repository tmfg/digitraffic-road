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
                "fi.livi.ws.wsdl.lotju.kamerametatiedot._2015._09._29",
                "fi.livi.ws.wsdl.lotju.kamerametatiedot._2016._10._06",
                "fi.livi.ws.wsdl.lotju.lammetatiedot._2014._03._06",
                "fi.livi.ws.wsdl.lotju.lammetatiedot._2015._09._29",
                "fi.livi.ws.wsdl.lotju.lammetatiedot._2016._10._06",
                "fi.livi.ws.wsdl.lotju.metatiedot._2014._03._06",
                "fi.livi.ws.wsdl.lotju.metatiedot._2015._09._29",
                "fi.livi.ws.wsdl.lotju.tiesaa._2016._10._06",
                // From xsds generated classes
                "fi.livi.digitraffic.tie.lotju.xsd.kamera",
                "fi.livi.digitraffic.tie.lotju.xsd.lam",
                "fi.livi.digitraffic.tie.lotju.xsd.tiesaa",
                "fi.livi.digitraffic.tie.lotju.xsd.datex2");
        return marshaller;
    }
}