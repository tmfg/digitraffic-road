package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;

import fi.livi.digitraffic.tie.service.trafficmessage.DatexII223XmlMarshaller;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatexII223Converter {
    private final DatexII223XmlMarshaller datexII223XmlMarshaller;

    public DatexII223Converter(final DatexII223XmlMarshaller datexII223XmlMarshaller) {
        this.datexII223XmlMarshaller = datexII223XmlMarshaller;
    }

    public D2LogicalModel createD2LogicalModel(final List<DataDatex2SituationMessage> messages) {
        final var model = new D2LogicalModel();
        final var publication = new SituationPublication();

        model.setPayloadPublication(publication);

        messages.forEach(message -> {
            final var d2Model = datexII223XmlMarshaller.convertToObject(message.getMessage());
            final var situationPublication = (SituationPublication)d2Model.getPayloadPublication();

            publication.getSituations().addAll(situationPublication.getSituations());
        });

        return model;
    }

}
