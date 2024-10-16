package fi.livi.digitraffic.tie.conf.jaxb2;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;

public class Jaxb2RootElementHttpMessageConverter
        extends org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter {
    private static final Logger log = LoggerFactory.getLogger(Jaxb2RootElementHttpMessageConverter.class);

    private final Class<?> supportsClazz;

    private final String jaxbSchemaLocations;

    public Jaxb2RootElementHttpMessageConverter(final Class<?> supportsClazz, final String...jaxbSchemaLocations) {
        this.supportsClazz = supportsClazz;
        this.jaxbSchemaLocations = StringUtils.join(jaxbSchemaLocations, " ");
    }

    @Override
    public boolean canRead(final Class<?> clazz, final MediaType mediaType) {
        return supports(clazz) && super.canRead(clazz, mediaType);
    }

    @Override
    public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
        return supports(clazz) && super.canWrite(clazz, mediaType);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return Objects.equals(supportsClazz, clazz);
    }

    @Override
    protected void customizeMarshaller(final Marshaller marshaller) {
        super.customizeMarshaller(marshaller);
        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, jaxbSchemaLocations);
        } catch (final PropertyException e) {
            log.error("method=customizeMarshaller setProperty failed", e);
        }
    }
}
