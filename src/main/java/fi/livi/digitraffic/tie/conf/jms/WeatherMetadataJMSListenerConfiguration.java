package fi.livi.digitraffic.tie.conf.jms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import fi.livi.digitraffic.tie.service.ClusteredLocker;
import fi.livi.digitraffic.tie.service.jms.JMSMessageListener;
import fi.livi.digitraffic.tie.service.jms.marshaller.WeatherMetadataUpdatedMessageMarshaller;
import fi.livi.digitraffic.tie.service.jms.marshaller.dto.WeatherMetadataUpdatedMessageDto;
import fi.livi.digitraffic.tie.service.weather.WeatherMetadataUpdateMessageHandler;
import progress.message.jclient.QueueConnectionFactory;

@ConditionalOnProperty(name = "jms.weather.meta.inQueue")
@Configuration
public class WeatherMetadataJMSListenerConfiguration extends AbstractJMSListenerConfiguration<WeatherMetadataUpdatedMessageDto> {
    private static final Logger log = LoggerFactory.getLogger(WeatherMetadataJMSListenerConfiguration.class);
    private final WeatherMetadataUpdateMessageHandler weatherMetadataUpdateMessageHandler;
    private final Jaxb2Marshaller tiesaaMetadataChangeJaxb2Marshaller;

    @Autowired
    public WeatherMetadataJMSListenerConfiguration(@Qualifier("sonjaJMSConnectionFactory") QueueConnectionFactory connectionFactory,
                                                   @Value("${jms.userId}") final String jmsUserId,
                                                   @Value("${jms.password}") final String jmsPassword,
                                                   @Value("#{'${jms.weather.meta.inQueue}'.split(',')}")final List<String> jmsQueueKeys,
                                                   final WeatherMetadataUpdateMessageHandler weatherMetadataUpdateMessageHandler,
                                                   final ClusteredLocker clusteredLocker,
                                                   @Qualifier("tiesaaMetadataChangeJaxb2Marshaller")
                                                   final Jaxb2Marshaller tiesaaMetadataChangeJaxb2Marshaller) {
        super(connectionFactory, clusteredLocker, log);
        this.weatherMetadataUpdateMessageHandler = weatherMetadataUpdateMessageHandler;
        this.tiesaaMetadataChangeJaxb2Marshaller = tiesaaMetadataChangeJaxb2Marshaller;

        setJmsParameters(new JMSParameters(jmsQueueKeys, jmsUserId, jmsPassword,
                                           WeatherMetadataJMSListenerConfiguration.class.getSimpleName(),
                                           ClusteredLocker.generateInstanceId()));
    }

    @Override
    public JMSMessageListener<WeatherMetadataUpdatedMessageDto> createJMSMessageListener() {
        final JMSMessageListener.JMSDataUpdater<WeatherMetadataUpdatedMessageDto> handleData =
            weatherMetadataUpdateMessageHandler::updateMetadataFromJms;
        final WeatherMetadataUpdatedMessageMarshaller messageMarshaller =
            new WeatherMetadataUpdatedMessageMarshaller(tiesaaMetadataChangeJaxb2Marshaller);

        return new JMSMessageListener<>(messageMarshaller, handleData,
                                        isQueueTopic(getJmsParameters().getJmsQueueKeys()),
                                        log);
    }
}
