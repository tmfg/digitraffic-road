package fi.livi.digitraffic.tie.conf.jms.listener;

import java.util.Collections;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

public class Datex2JMSMessageListener extends JMSMessageListener<Pair<D2LogicalModel, String>> {
    public Datex2JMSMessageListener(final Jaxb2Marshaller jaxb2Marshaller, final JMSDataUpdater<Pair<D2LogicalModel, String>> dataUpdater, final boolean drainScheduled, final Logger log) {
        super(jaxb2Marshaller, dataUpdater, drainScheduled, log);
    }

    @Override
    protected List<Pair<D2LogicalModel, String>> getObjectFromBytes(final byte[] body) {
        throw new IllegalStateException();
    }

    protected List<Pair<D2LogicalModel, String>> unmarshallText(final TextMessage message) throws JMSException, JAXBException {
        final String text = parseTextMessageText(message);

        Object object = jaxb2Marshaller.unmarshal(new StringSource(text));
        if (object instanceof JAXBElement) {
            // For Datex2 messages extra stuff
            object = ((JAXBElement) object).getValue();
        }
        return Collections.singletonList(Pair.of((D2LogicalModel)object, text));
    }

}
