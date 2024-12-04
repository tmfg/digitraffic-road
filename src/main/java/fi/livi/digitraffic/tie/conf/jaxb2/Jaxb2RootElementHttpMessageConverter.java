package fi.livi.digitraffic.tie.conf.jaxb2;

import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.MarshalException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;

public class Jaxb2RootElementHttpMessageConverter<T>
        extends org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter {
    private static final Logger log = LoggerFactory.getLogger(Jaxb2RootElementHttpMessageConverter.class);

    private final Class<? extends T> childClazz;
    private final Class<T> parentClass;
    private final String parentName;

    private String jaxbSchemaLocations;
    private NamespacePrefixMapper namespacePrefixMapper;
    private String namespaceURI;

    /**
     * Used to produce xml from instance of type clazz.
     *
     * @param clazz the class to be produced by converter.
     */
    public Jaxb2RootElementHttpMessageConverter(final Class<T> clazz) {
        this(null, clazz, null);
    }

    /**
     * Used to produce xml from instance of childClazz with parentClazz as root xml element
     * and child type as xsi:type attribute in xml.
     * @param childClazz type of child class
     * @param parentClazz type of parent class
     * @param parentName name for parent to be used in xml
     */
    public Jaxb2RootElementHttpMessageConverter(final Class<? extends T> childClazz,
                                                final Class<T> parentClazz,
                                                final String parentName) {
        this.childClazz = childClazz;
        this.parentClass = parentClazz;
        this.parentName = parentName;
    }

    public Jaxb2RootElementHttpMessageConverter<T> withJaxbSchemaLocations(final String...jaxbSchemaLocations) {
        this.jaxbSchemaLocations = StringUtils.join(jaxbSchemaLocations, " ");
        return this;
    }

    public Jaxb2RootElementHttpMessageConverter<T> withNamespacePrefixMapper(final NamespacePrefixMapper namespacePrefixMapper) {
        this.namespacePrefixMapper = namespacePrefixMapper;
        return this;
    }

    public Jaxb2RootElementHttpMessageConverter<T> withNamespaceURI(final String namespaceURI) {
        this.namespaceURI = namespaceURI;
        return this;
    }

    @Override
    public boolean canRead(@NonNull final Class<?> clazz, final MediaType mediaType) {
        return supports(clazz) && super.canRead(clazz, mediaType);
    }

    @Override
    public boolean canWrite(@NonNull final Class<?> clazz, final MediaType mediaType) {
        return supports(clazz) && super.canWrite(clazz, mediaType);
    }

    @Override
    protected boolean supports(@NonNull final Class<?> clazz) {
        if (childClazz != null) {
            return Objects.equals(childClazz, clazz);
        }
        return Objects.equals(parentClass, clazz);
    }

    @Override
    protected void customizeMarshaller(@NonNull final Marshaller marshaller) {
        super.customizeMarshaller(marshaller);
        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (jaxbSchemaLocations != null) {
                marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, jaxbSchemaLocations);
            }
            if (namespacePrefixMapper != null) {
                marshaller.setProperty("org.glassfish.jaxb.namespacePrefixMapper", namespacePrefixMapper);
            }
        } catch (final PropertyException e) {
            log.error("method=customizeMarshaller setProperty failed", e);
        }
    }

    /**
     * Base implementation copied from parent class
     * @param o the object to write to the output message
     * @param headers the HTTP output headers
     * @param result the HTTP output body
     * @throws Exception thrown if there is an error
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeToResult(@NonNull final Object o, @NonNull final HttpHeaders headers, @NonNull final Result result) throws Exception {
        try {
            final Class<?> clazz = ClassUtils.getUserClass(o);
            // If child and parent classes are defined, then use parent class as target and generate
            // xsi:type attribute to tell what type the child is.
            if (childClazz != null) {
                    final Marshaller marshaller = createMarshaller(clazz);
                    setCharset(headers.getContentType(), marshaller);

                    final JAXBElement<T> jaxbElement =
                            new JAXBElement<>(
                                    new QName(namespaceURI, parentName),
                                    parentClass,
                                    (T)o);
                    marshaller.marshal(jaxbElement, result);
            } else {
                super.writeToResult(o, headers, result);
            }
        }
        catch (final MarshalException ex) {
            throw ex;
        }
        catch (final JAXBException ex) {
            throw new HttpMessageConversionException("Invalid JAXB setup: " + ex.getMessage(), ex);
        }
    }

    private void setCharset(@Nullable final MediaType contentType, final Marshaller marshaller) throws PropertyException {
        if (contentType != null && contentType.getCharset() != null) {
            marshaller.setProperty(Marshaller.JAXB_ENCODING, contentType.getCharset().name());
        }
    }
}
