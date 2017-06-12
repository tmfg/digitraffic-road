package fi.livi.digitraffic.tie.conf.jms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import com.google.protobuf.InvalidProtocolBufferException;
import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.tie.data.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.data.service.SensorDataUpdateService;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.tms.enabled")
@Configuration
public class TmsJMSListenerConfiguration extends AbstractJMSListenerConfiguration<LAMRealtimeProtos.Lam> {
    private static final Logger log = LoggerFactory.getLogger(TmsJMSListenerConfiguration.class);

    private final JMSParameters jmsParameters;
    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public TmsJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory")
                                       QueueConnectionFactory connectionFactory,
                                       @Value("${jms.userId}")
                                       final String jmsUserId,
                                       @Value("${jms.password}")
                                       final String jmsPassword,
                                       @Value("${jms.tms.inQueue}")
                                       final String jmsQueueKey,
                                       final SensorDataUpdateService sensorDataUpdateService,
                                       final LockingService lockingService) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;

        jmsParameters = new JMSParameters(jmsQueueKey, jmsUserId, jmsPassword,
                                          TmsJMSListenerConfiguration.class.getSimpleName(),
                                          UUID.randomUUID().toString());
    }

    @Override
    public JMSParameters getJmsParameters() {
        return jmsParameters;
    }

    @Override
    public JMSMessageListener<LAMRealtimeProtos.Lam> createJMSMessageListener() throws JAXBException {
        JMSMessageListener.JMSDataUpdater<LAMRealtimeProtos.Lam> handleData = (data) -> {
            final List<LAMRealtimeProtos.Lam> lamData = data.stream().map(Pair::getLeft).collect(Collectors.toList());

            //sensorDataUpdateService.updateLamData(lamData);
        };

        return new TmsJMSMessageListener(
                                        handleData,
                                        isQueueTopic(jmsParameters.getJmsQueueKey()),
                                        log);
    }

    private static class TmsJMSMessageListener extends JMSMessageListener<LAMRealtimeProtos.Lam> {
        public TmsJMSMessageListener(final JMSDataUpdater<LAMRealtimeProtos.Lam> handleData, final boolean queueTopic, final Logger log) throws JAXBException {
            super(LAMRealtimeProtos.Lam.class, handleData, queueTopic, log);
        }

        @Override
        protected List<LAMRealtimeProtos.Lam> getObjectFromBytes(final byte[] body) {
            try {
                final List<LAMRealtimeProtos.Lam> lamList = new ArrayList<>();
                final ByteArrayInputStream bais = new ByteArrayInputStream(body);

                while(bais.available() > 0) {
                    final LAMRealtimeProtos.Lam lam = LAMRealtimeProtos.Lam.parseDelimitedFrom(bais);

                    debug(lam);

                    lamList.add(lam);
                }

                return lamList;
            } catch (final InvalidProtocolBufferException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static void debug(final LAMRealtimeProtos.Lam lam) {
        System.out.println(String.format("%d : %d anturia", lam.getAsemaId(), lam.getAnturiCount()));
    }
}
