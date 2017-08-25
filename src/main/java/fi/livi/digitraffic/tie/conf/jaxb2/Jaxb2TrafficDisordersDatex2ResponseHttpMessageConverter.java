package fi.livi.digitraffic.tie.conf.jaxb2;

import java.util.Set;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;

import com.google.common.collect.Sets;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.RoadworksDatex2Response;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

public class Jaxb2TrafficDisordersDatex2ResponseHttpMessageConverter extends Jaxb2RootElementHttpMessageConverter {
    private static final Logger log = LoggerFactory.getLogger(Jaxb2TrafficDisordersDatex2ResponseHttpMessageConverter.class);

    private static final Set<Class<?>> SUPPORTED = Sets.newHashSet(TrafficDisordersDatex2Response.class, RoadworksDatex2Response.class);

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
        return SUPPORTED.contains(clazz);
    }

    @Override
    protected void customizeMarshaller(final Marshaller marshaller) {
        super.customizeMarshaller(marshaller);
        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                    "http://tie.digitraffic.fi/schemas " +
                    "https://raw.githubusercontent.com/finnishtransportagency/metadata/master/schema/DATEXIIResponseSchema_1_0.xsd " +
                    "http://datex2.eu/schema/2/2_0 " +
                    "https://raw.githubusercontent.com/finnishtransportagency/metadata/master/schema/DATEXIISchema_2_2_3_with_definitions_FI.xsd");
        } catch (final PropertyException e) {
            log.error("setProperty failed", e);
        }
    }
}
