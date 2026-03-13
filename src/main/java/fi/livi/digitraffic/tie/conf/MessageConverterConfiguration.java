package fi.livi.digitraffic.tie.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;

import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;

@Configuration
public class MessageConverterConfiguration {
    @Bean
    public JacksonXmlHttpMessageConverter jacksonXmlHttpMessageConverter() {
        final XmlMapper xmlMapper = XmlMapper.builder()
                .enable(XmlWriteFeature.WRITE_XML_DECLARATION)
                .build();

        return new JacksonXmlHttpMessageConverter(xmlMapper);
    }
}
