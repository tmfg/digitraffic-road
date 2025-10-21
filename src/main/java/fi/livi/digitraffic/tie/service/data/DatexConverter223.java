package fi.livi.digitraffic.tie.service.data;

import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.v2_2_3_fi.SituationPublication;
import fi.livi.digitraffic.tie.model.data.DataDatex2SituationMessage;

import fi.livi.digitraffic.tie.service.trafficmessage.Datex223XmlMarshaller;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatexConverter223 {
    private final Datex223XmlMarshaller datex223XmlMarshaller;

    public DatexConverter223(final Datex223XmlMarshaller datex223XmlMarshaller) {
        this.datex223XmlMarshaller = datex223XmlMarshaller;
    }

    public D2LogicalModel createD2LogicalModel(final List<DataDatex2SituationMessage> messages) {
        final var model = new D2LogicalModel();
        final var publication = new SituationPublication();

        model.setPayloadPublication(publication);

        messages.forEach(message -> {
            final var d2Model = datex223XmlMarshaller.convertToObject(message.getMessage());
            final var situationPublication = (SituationPublication)d2Model.getPayloadPublication();

            publication.getSituations().addAll(situationPublication.getSituations());
        });

        return model;
    }

}
