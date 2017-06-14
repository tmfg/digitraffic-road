package fi.livi.digitraffic.tie.conf.jms.listener;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;

public class NormalJMSMessageListener<T> extends JMSMessageListener<T,T> {
    public NormalJMSMessageListener(final Class<T> typeClass, final JMSDataUpdater dataUpdater, final boolean drainScheduled,
        final Logger log) throws JAXBException {
        super(typeClass, dataUpdater, drainScheduled, log);
    }

    protected List<T> unmarshallText(final TextMessage message) throws JMSException, JAXBException {
        final String text = parseTextMessageText(message);
        final StringReader sr = new StringReader(text);

        Object object = jaxbUnmarshaller.unmarshal(sr);
        if (object instanceof JAXBElement) {
            // For Datex2 messages extra stuff
            object = ((JAXBElement) object).getValue();
        }
        return Collections.singletonList((T) object);
    }

}
