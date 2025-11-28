package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import jakarta.xml.bind.JAXBElement;

@Component
public class DatexII35XmlMarshaller {
    private final Jaxb2Marshaller marshaller;

    public DatexII35XmlMarshaller(@Qualifier("datexII_3_5_jaxb2Marshaller") final Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public SituationPublication convertToObject(final String xmlSting) {
        // Trim empty control before and after xml-declaration as they are not allowed
        final Object object = marshaller.unmarshal(new StringSource(StringUtils.trim(xmlSting)));
        if (object instanceof JAXBElement) {
            return ((JAXBElement<SituationPublication>) object).getValue();
        }
        return (SituationPublication)object;
    }

    public String convertToString(final SituationPublication object) {
        final StringResult result = new StringResult();

        marshaller.marshal(object, result);

        return result.toString();
    }
}
