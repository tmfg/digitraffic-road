package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.common.annotation.ConditionalOnPropertyNotBlank;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.service.jms.JMSMessageHandler;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnBean(JMSConfiguration.class)
@ConditionalOnPropertyNotBlank("jms.tms.inQueue")
@Configuration
public class TmsJMSListenerConfiguration extends AbstractJMSListenerConfiguration<LAMRealtimeProtos.Lam> {
    private static final Logger log = LoggerFactory.getLogger(TmsJMSListenerConfiguration.class);

    private final SensorDataUpdateService sensorDataUpdateService;

    @Autowired
    public TmsJMSListenerConfiguration(final @Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                       final @Value("${jms.userId}") String jmsUserId,
                                       final @Value("${jms.password}") String jmsPassword,
                                       final @Value("#{'${jms.tms.inQueue}'.split(',')}") List<String> jmsQueueKeys,
                                       final SensorDataUpdateService sensorDataUpdateService,
                                       final LockingService lockingService) {

        super(connectionFactory,
              lockingService,
              log);
        this.sensorDataUpdateService = sensorDataUpdateService;
        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                TmsJMSListenerConfiguration.class.getSimpleName(),
                lockingService.getInstanceId()));
    }

    @Override
    public JMSMessageListener<LAMRealtimeProtos.Lam> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<LAMRealtimeProtos.Lam> handleData = sensorDataUpdateService::updateLamValueBuffer;
        final TmsDataJMSMessageMarshaller messageMarshaller = new TmsDataJMSMessageMarshaller();

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                        log);
    }

    @Override
    protected JMSMessageHandler.JMSMessageType getJMSMessageType() {
        return JMSMessageHandler.JMSMessageType.TMS_DATA_UP_TO_DATE;
    }
}
