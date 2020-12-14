package fi.livi.digitraffic.tie.service.v1.datex2;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.datex2.D2LogicalModel;

@Component
public class Datex2XmlStringToObjectMarshaller {
    private final Jaxb2Marshaller datex2Jaxb2Marshaller;

    public Datex2XmlStringToObjectMarshaller(@Qualifier("datex2Jaxb2Marshaller")
                                    final Jaxb2Marshaller datex2Jaxb2Marshaller) {
        this.datex2Jaxb2Marshaller = datex2Jaxb2Marshaller;
    }

    public D2LogicalModel convertToObject(final String xmlSting) {
        // Trim empty control before and after xml-declaration as they are not allowed
        final Object object = datex2Jaxb2Marshaller.unmarshal(new StringSource(StringUtils.trim(xmlSting)));
        if (object instanceof JAXBElement) {
            return ((JAXBElement<D2LogicalModel>) object).getValue();
        }
        return (D2LogicalModel)object;
    }

    public String convertToString(final D2LogicalModel object) {
        final StringResult result = new StringResult();

        datex2Jaxb2Marshaller.marshal(object, result);

        return result.toString();
    }
}
