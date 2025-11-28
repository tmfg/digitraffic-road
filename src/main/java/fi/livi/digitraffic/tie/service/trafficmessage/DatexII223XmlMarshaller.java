package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import jakarta.xml.bind.JAXBElement;

@Component
public class DatexII223XmlMarshaller {
    private final Jaxb2Marshaller marshaller;

    public DatexII223XmlMarshaller(@Qualifier("datexII_2_2_3_fiJaxb2Marshaller")
                                             final Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public D2LogicalModel convertToObject(final String xmlString) {
        // Trim empty control before and after xml-declaration as they are not allowed
        final Object object = marshaller.unmarshal(new StringSource(StringUtils.trim(xmlString)));
        if (object instanceof JAXBElement) {
            return ((JAXBElement<D2LogicalModel>) object).getValue();
        }
        return (D2LogicalModel)object;
    }

    public String convertToString(final D2LogicalModel object) {
        final StringResult result = new StringResult();

        marshaller.marshal(object, result);

        return result.toString();
    }
}
