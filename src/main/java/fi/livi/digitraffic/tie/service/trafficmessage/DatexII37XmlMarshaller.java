package fi.livi.digitraffic.tie.service.trafficmessage;

import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import jakarta.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DatexII37XmlMarshaller {
    private final Jaxb2Marshaller marshaller;
    private static final Logger log = LoggerFactory.getLogger(DatexII37XmlMarshaller.class);

    public DatexII37XmlMarshaller(
            @Qualifier("datexII_3_7_jaxb2Marshaller")
            final Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public SituationPublication convertToObject(final Long messageId, final String xmlString) {
        try {
            // Trim empty control before and after xml-declaration as they are not allowed
            final Object object = marshaller.unmarshal(new StringSource(StringUtils.trim(xmlString)));
            if (object instanceof JAXBElement) {
                @SuppressWarnings("unchecked")
                final JAXBElement<SituationPublication> element = (JAXBElement<SituationPublication>) object;
                return element.getValue();
            }
            return (SituationPublication) object;
        } catch (final Exception e) {
            safeDebugMessage(messageId, xmlString);
            throw new RuntimeException("Could not unmarshal Datex II 3.7 message id=" + messageId, e);
        }
    }

    public String convertToString(final SituationPublication object) {
        final StringResult result = new StringResult();
        marshaller.marshal(object, result);
        return result.toString();
    }

    private static final Pattern XSI_TYPE_PATTERN =
            Pattern.compile(
                    "<sit:situationRecord\\b[^>]*\\bxsi:type\\s*=\\s*\"([^\"]+)\"",
                    Pattern.DOTALL
            );

    private void safeDebugMessage(final Long messageId, final String message) {
        if (message == null) {
            log.error("Message id={} is null!", messageId);
            return;
        }
        final var afterSituation = StringUtils.substringAfter(message, "<sit:situation");

        final Set<String> uniqueSituationRecordTypes = new HashSet<>();
        final Matcher matcher = XSI_TYPE_PATTERN.matcher(message);
        while (matcher.find()) {
            uniqueSituationRecordTypes.add(matcher.group(1));
        }

        if (afterSituation.equals(StringUtils.EMPTY)) {
            log.error("No situation text found! Datex II message id={}: {} with situationRecordTypes: {}",
                    messageId, StringUtils.substring(message, 1100, 1400), String.join(", ", uniqueSituationRecordTypes));
        } else {
            log.error("Failed to convert Datex II message id={}: {} with situationRecordTypes: {}",
                    messageId, StringUtils.substring(afterSituation, 0, 400), String.join(", ", uniqueSituationRecordTypes));
        }
    }
}
