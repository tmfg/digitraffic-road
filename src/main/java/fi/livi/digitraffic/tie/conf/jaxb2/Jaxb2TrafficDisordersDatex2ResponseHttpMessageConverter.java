package fi.livi.digitraffic.tie.conf.jaxb2;

import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

public class Jaxb2TrafficDisordersDatex2ResponseHttpMessageConverter extends Jaxb2RootElementHttpMessageConverter {

    private static final Logger log = LoggerFactory.getLogger(Jaxb2TrafficDisordersDatex2ResponseHttpMessageConverter.class);

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        boolean val = supports(clazz) && super.canRead(clazz, mediaType);
        return val;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        boolean val = supports(clazz) && super.canWrite(clazz, mediaType);
        return val;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return clazz.equals(TrafficDisordersDatex2Response.class);
    }

    @Override
    protected void customizeMarshaller(Marshaller marshaller) {
        super.customizeMarshaller(marshaller);
        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,
                    "http://tie.digitraffic.fi/schemas " +
                    "https://raw.githubusercontent.com/finnishtransportagency/metadata/master/schema/DATEXIIResponseSchema_1_0.xsd " +
                    "http://datex2.eu/schema/2/2_0 " +
                    "https://raw.githubusercontent.com/finnishtransportagency/metadata/master/schema/DATEXIISchema_2_2_3_with_definitions_FI.xsd");
        } catch (PropertyException e) {
            log.error("setProperty failed", e);
        }
    }
}
