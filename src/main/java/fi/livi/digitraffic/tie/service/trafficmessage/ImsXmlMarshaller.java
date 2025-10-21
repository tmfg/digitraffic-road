package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.ImsMessage;
import jakarta.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringSource;

@Component
public class ImsXmlMarshaller {
    private final Jaxb2Marshaller imsJaxb2Marshaller;

    public ImsXmlMarshaller(final Jaxb2Marshaller imsJaxb2Marshaller) {
        this.imsJaxb2Marshaller = imsJaxb2Marshaller;
    }

    public ImsMessage convertToObject(final String xmlString) {
        // Trim empty control before and after xml-declaration as they are not allowed
        final Object object = imsJaxb2Marshaller.unmarshal(new StringSource(StringUtils.trim(xmlString)));
        if (object instanceof JAXBElement<?>) {
            return ((JAXBElement<ImsMessage>) object).getValue();
        }
        return (ImsMessage) object;

    }
}
