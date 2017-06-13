package fi.livi.digitraffic.tie.conf.jms.listener;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

public class Datex2JMSMessageListener extends JMSMessageListener<D2LogicalModel, Pair<D2LogicalModel, String>> {
    public Datex2JMSMessageListener(final JMSDataUpdater dataUpdater, final boolean drainScheduled, final Logger log) throws JAXBException {
        super(D2LogicalModel.class, dataUpdater, drainScheduled, log);
    }

    protected List<Pair<D2LogicalModel, String>> unmarshallText(final TextMessage message) throws JMSException, JAXBException {
        final String text = parseTextMessageText(message);

        final StringReader sr = new StringReader(text);
        Object object = jaxbUnmarshaller.unmarshal(sr);
        if (object instanceof JAXBElement) {
            // For Datex2 messages extra stuff
            object = ((JAXBElement) object).getValue();
        }
        return Collections.singletonList(Pair.of((D2LogicalModel)object, text));
    }

}
