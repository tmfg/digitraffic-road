package fi.livi.digitraffic.tie.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

@Configuration
public class MessageConverterConfig {
    @Bean
    public MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter(final Jackson2ObjectMapperBuilder builder) {
        final XmlMapper xmlMapper = builder.createXmlMapper(true).build();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

        return new MappingJackson2XmlHttpMessageConverter(xmlMapper);
    }

    /**
     * Try to speed up JSON serialization
     * @see <a href="https://github.com/FasterXML/jackson-modules-base/tree/master/afterburner">afterburner</a>
     */
    @Bean
    public AfterburnerModule afterburnerModule() {
        return new AfterburnerModule();
    }
}
