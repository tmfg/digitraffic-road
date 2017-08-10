package fi.livi.digitraffic.tie.data.jms.marshaller;

import java.util.Collections;
import java.util.List;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

public class Datex2MessageMarshaller extends TextMessageMarshaller<Datex2MessageDto> {
    public Datex2MessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        super(jaxb2Marshaller);
    }

    @Override
    protected List<Datex2MessageDto> transform(final Object object, final String text) {
        return Collections.singletonList(new Datex2MessageDto(text, null, (D2LogicalModel)object));
    }
}
