package fi.livi.digitraffic.tie.data.service.datex2;

import java.util.List;

import javax.jms.JMSException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.jms.marshaller.Datex2MessageMarshaller;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Service
public class Datex2JMSMessageListener {

    private static final Logger log = LoggerFactory.getLogger(Datex2JMSMessageListener.class);

    private final Datex2DataService datex2DataService;
    private final Jaxb2Marshaller jaxb2Marshaller;
    private JMSMessageListener<Datex2> datex2MessageListener;

    @Autowired
    public Datex2JMSMessageListener(final Datex2DataService datex2DataService, final Jaxb2Marshaller jaxb2Marshaller) throws JMSException {

        this.datex2DataService = datex2DataService;
        this.jaxb2Marshaller = jaxb2Marshaller;

        final JMSMessageListener.JMSDataUpdater<Pair<D2LogicalModel, String>> handleData = datex2DataService::updateDatex2Data;
        final Datex2MessageMarshaller messageMarshaller = new Datex2MessageMarshaller(jaxb2Marshaller);

        datex2MessageListener = new JMSMessageListener(messageMarshaller, handleData,
                                                       false,
                                                       log);
    }

    public void handleMessages(final List<String> messages) {
        for (String message : messages) {
            try {
                datex2MessageListener.onMessage(new Datex2Message(message));
            } catch (JMSException e) {
                log.error("Datex2Message creation failed");
            }
        }
    }

}
