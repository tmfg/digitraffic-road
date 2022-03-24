package fi.livi.digitraffic.tie.conf.jms;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import progress.message.jclient.QueueConnectionFactory;

@Configuration
@ConditionalOnNotWebApplication
public class JMSConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JMSConfiguration.class);

    @ConditionalOnProperty("jms.connectionUrls")
    @Bean(name = "sonjaJMSConnectionFactory")
    public QueueConnectionFactory queueConnectionFactoryForJMS(@Value("${jms.connectionUrls}")
                                                               final String jmsConnectionUrls,
                                                               final @Value("${jms.userId}") String jmsUserId,
                                                               final @Value("${jms.password}") String jmsPassword) throws JMSException {
        return createQueueConnectionFactory(jmsConnectionUrls, jmsUserId, jmsPassword);
    }

    public static QueueConnectionFactory createQueueConnectionFactory(final String jmsConnectionUrls, final String jmsUserId, final String jmsPassword) throws JMSException {
        QueueConnectionFactory connectionFactory = new QueueConnectionFactory();
        connectionFactory.setSequential(true);
        connectionFactory.setFaultTolerant(true);
        // How often to check idle connection status
        connectionFactory.setPingInterval(10);
        // How soon to try next broker
        connectionFactory.setFaultTolerantReconnectTimeout(10);
        // Maximum time to try establish socket connection
        connectionFactory.setSocketConnectTimeout(10000);
        // Maximum total time to try connection to different brokers
        connectionFactory.setInitialConnectTimeout(60);
        connectionFactory.setConnectionURLs(jmsConnectionUrls);
        connectionFactory.setDefaultUser(jmsUserId);
        connectionFactory.setDefaultPassword(jmsPassword);
        log.info("method=createQueueConnectionFactory Create JMS QueueConnectionFactory {}", connectionFactory);
        return connectionFactory;
    }
}
