package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.datex2.v3_7.InternationalIdentifier;
import fi.livi.digitraffic.tie.datex2.v3_7.SituationPublication;
import fi.livi.digitraffic.tie.model.data.MessageAndModified;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII37XmlMarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatexII37Converter {
    private static final Logger log = LoggerFactory.getLogger(DatexII37Converter.class);

    private final DatexII37XmlMarshaller datex37XmlMarshaller;

    public DatexII37Converter(final DatexII37XmlMarshaller datex37XmlMarshaller) {
        this.datex37XmlMarshaller = datex37XmlMarshaller;
    }

    public SituationPublication createPublication(final List<MessageAndModified> messages) {
        final var publications = messages.stream()
                .map(m -> {
                    try {
                        return datex37XmlMarshaller.convertToObject(m.getMessageId(), m.getMessage());
                    } catch (final Exception e) {
                        log.error("Failed to convert Datex II 3.7 message id={}", m.getMessageId(), e);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing((SituationPublication p) -> p.getPublicationTime()).reversed())
                .collect(Collectors.toList());

        if (publications.isEmpty()) {
            return new SituationPublication()
                    .withPublicationTime(Instant.now())
                    .withPublicationCreator(new InternationalIdentifier("FI", "FTA", null));
        }

        final var publication = publications.removeFirst();

        // append all older situations to the newest and return the combined
        publications.forEach(p -> publication.getSituations().addAll(p.getSituations()));

        return publication;
    }
}
