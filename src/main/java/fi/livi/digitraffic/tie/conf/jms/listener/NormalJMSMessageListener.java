package fi.livi.digitraffic.tie.conf.jms.listener;

import java.util.Collections;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;

public class NormalJMSMessageListener<T> extends JMSMessageListener<T> {
    public NormalJMSMessageListener(final Jaxb2Marshaller jaxb2Marshaller, final JMSDataUpdater dataUpdater, final boolean drainScheduled,
        final Logger log) {
        super(jaxb2Marshaller, dataUpdater, drainScheduled, log);
    }

    protected List<T> unmarshallText(final TextMessage message) throws JMSException, JAXBException {
        final String text = parseTextMessageText(message);

        Object object = jaxb2Marshaller.unmarshal(new StringSource(text));
        if (object instanceof JAXBElement) {
            // For Datex2 messages extra stuff
            object = ((JAXBElement) object).getValue();
        }
        return Collections.singletonList((T) object);
    }

}
