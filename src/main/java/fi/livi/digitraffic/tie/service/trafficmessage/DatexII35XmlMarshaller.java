package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import jakarta.xml.bind.JAXBElement;

@Component
public class DatexII35XmlMarshaller {
    private final Jaxb2Marshaller marshaller;
    private static final Logger log = LoggerFactory.getLogger(DatexII35XmlMarshaller.class);

    public DatexII35XmlMarshaller(@Qualifier("datexII_3_5_jaxb2Marshaller") final Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public SituationPublication convertToObject(final String xmlSting) {
        try {
            // Trim empty control before and after xml-declaration as they are not allowed
            final Object object = marshaller.unmarshal(new StringSource(StringUtils.trim(xmlSting)));
            if (object instanceof JAXBElement) {
                return ((JAXBElement<SituationPublication>) object).getValue();
            }
            return (SituationPublication) object;
        } catch (final Exception e) {
            safeDebugMessage(xmlSting);

            log.error("Could not unmarshal datex2 message", e);
            return null;
        }
    }

    public String convertToString(final SituationPublication object) {
        final StringResult result = new StringResult();

        marshaller.marshal(object, result);

        return result.toString();
    }

    private void safeDebugMessage(final String message) {
        final var afterSituation = StringUtils.substringAfter(message, "situation id");

        if(afterSituation.equals(StringUtils.EMPTY)) {
            log.error("no situation text found!");
            log.error(StringUtils.substring(message, 1100, 1400));
        } else {
            log.error(StringUtils.substring(afterSituation, 0, 400));
        }
    }
}
