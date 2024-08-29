package fi.livi.digitraffic.tie.conf.kca.artemis.jms.listener;

import static fi.livi.digitraffic.tie.service.jms.JMSMessageHandler.JMSMessageType.TMS_DATA_REALTIME;

import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;

import fi.ely.lotju.lam.proto.LAMRealtimeProtos;
import fi.livi.digitraffic.common.annotation.ConditionalOnPropertyNotBlank;
import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.conf.kca.artemis.jms.ArtemisJMSConfiguration;
import fi.livi.digitraffic.tie.service.jms.marshaller.TmsDataJMSMessageMarshaller;
import fi.livi.digitraffic.tie.service.roadstation.SensorDataUpdateService;
import jakarta.jms.JMSException;

@ConditionalOnPropertyNotBlank("kca.artemis.jms.tms.data.realtime.topic")
@ConditionalOnBean(ArtemisJMSConfiguration.class)
@ConditionalOnNotWebApplication
@Configuration
public class TmsDataRealtimeJMSTopicListenerConfiguration extends JMSListenerConfiguration<LAMRealtimeProtos.Lam> {

    @Autowired
    public TmsDataRealtimeJMSTopicListenerConfiguration(final SensorDataUpdateService sensorDataUpdateService,
                                                        final LockingService lockingService) {
        super(TMS_DATA_REALTIME, sensorDataUpdateService::updateLamValueBuffer, new TmsDataJMSMessageMarshaller(),
                lockingService.getInstanceId());
    }

    @JmsListener(destination = "${kca.artemis.jms.tms.data.realtime.topic:}",
                 containerFactory = ArtemisJMSConfiguration.JMS_LISTENER_CONTAINER_FACTORY_FOR_TOPIC)
    public void onJmsMessage(final ActiveMQBytesMessage message) throws JMSException {
        super.onMessage(message);
    }

}
