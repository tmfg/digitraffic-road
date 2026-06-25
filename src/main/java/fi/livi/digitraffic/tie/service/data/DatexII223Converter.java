package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.model.data.MessageAndModified;
import fi.livi.digitraffic.tie.service.trafficmessage.DatexII223XmlMarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatexII223Converter {
    private static final Logger log = LoggerFactory.getLogger(DatexII223Converter.class);

    private final DatexII223XmlMarshaller datexII223XmlMarshaller;

    public DatexII223Converter(final DatexII223XmlMarshaller datexII223XmlMarshaller) {
        this.datexII223XmlMarshaller = datexII223XmlMarshaller;
    }

    public D2LogicalModel createD2LogicalModel(final List<MessageAndModified> messages) {
        final var model = new D2LogicalModel();
        final var publication = new SituationPublication();

        model.setPayloadPublication(publication);

        messages.forEach(m -> {
            try {
                final var d2Model = datexII223XmlMarshaller.convertToObject(m.getMessage());
                final var situationPublication = (SituationPublication) d2Model.getPayloadPublication();
                publication.getSituations().addAll(situationPublication.getSituations());
            } catch (final Exception e) {
                log.error("Failed to convert Datex II 2.2.3 message id={}", m.getMessageId(), e);
            }
        });

        return model;
    }
}
