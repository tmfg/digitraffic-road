package fi.livi.digitraffic.tie.data.jms.marshaller;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

public class Datex2MessageMarshaller extends TextMessageMarshaller<Pair<D2LogicalModel, String>> {
    public Datex2MessageMarshaller(final Jaxb2Marshaller jaxb2Marshaller) {
        super(jaxb2Marshaller);
    }

    @Override
    protected List<Pair<D2LogicalModel, String>> transform(final Object object, final String text) {
        return Collections.singletonList(Pair.of((D2LogicalModel)object, text));
    }
}
