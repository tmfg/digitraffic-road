package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.datex2.v3_5.SituationPublication;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII35XmlMarshaller;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatexII35Converter {
    private final DatexII35XmlMarshaller datex35XmlMarshaller;

    public DatexII35Converter(final DatexII35XmlMarshaller datex35XmlMarshaller) {
        this.datex35XmlMarshaller = datex35XmlMarshaller;
    }

    public SituationPublication createPublication(final List<String> datex2Messages) {
        final var publications = datex2Messages.stream()
                .map(datex35XmlMarshaller::convertToObject)
                .sorted(Comparator.comparing((SituationPublication p) -> p.getPublicationTime()).reversed())
                .collect(Collectors.toList());

        if(publications.isEmpty()) {
            // this is missing all metadata...
            return new SituationPublication();
        }

        final var publication = publications.removeFirst();

        // append all older situations to the newest and return the combined
        publications.forEach(p -> publication.getSituations().addAll(p.getSituations()));

        return publication;
    }
}
