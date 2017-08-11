package fi.livi.digitraffic.tie.data.service.datex2;

import javax.xml.bind.JAXBElement;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringSource;

@Component
public class StringToObjectMarshaller {
    private final Jaxb2Marshaller jaxb2Marshaller;

    public StringToObjectMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    public <T> T convertToObject(final String xmlSting) {
        Object object = jaxb2Marshaller.unmarshal(new StringSource(xmlSting));
        if (object instanceof JAXBElement) {
            object = ((JAXBElement) object).getValue();
        }

        return (T)object;
    }
}
