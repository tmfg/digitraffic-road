package fi.livi.digitraffic.tie.conf;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoComponent;
import org.springframework.integration.mqtt.outbound.AbstractMqttMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.*;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@ConditionalOnProperty("mqtt.enabled")
@ConditionalOnNotWebApplication
@Configuration
@EnableIntegration
@IntegrationComponentScan
public class MqttConfiguration {
    private final String clientId = "road_updater_" + MqttClient.generateClientId();

    @Bean
    public MqttPahoClientFactory mqttClientFactory(
        @Value("${mqtt.server.url}") final String serverUrl,
        @Value("${mqtt.server.username}") final String username,
        @Value("${mqtt.server.password}") final String password,
        @Value("${mqtt.server.maxInflight}") final int maxInflight,
        @Value("${mqtt.server.connectionTimeout}") final int connectionTimeout,
        @Value("${mqtt.server.keepAlive}") final int keepAlive) {

        final DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.getConnectionOptions().setServerURIs(serverUrl.split(","));
        factory.getConnectionOptions().setUserName(username);
        factory.getConnectionOptions().setPassword(password.toCharArray());
        factory.getConnectionOptions().setMaxInflight(maxInflight);
        factory.getConnectionOptions().setConnectionTimeout(connectionTimeout);
        factory.getConnectionOptions().setCleanSession(true);
        factory.getConnectionOptions().setKeepAliveInterval(keepAlive);

        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel", async = "true")
    public MessageHandler mqttOutbound(final MqttPahoClientFactory mqttClientFactory) {
        return new SingleThreadMessageHandler(clientId, mqttClientFactory);
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel", defaultRequestTimeout = "2000", defaultReplyTimeout = "2000")
    public interface MqttGateway {
        // Paho does not support concurrency, all calls to this must be synchronized!
        void sendToMqtt(@Header(MqttHeaders.TOPIC) final String topic, @Payload final String data);
    }

    /**
     * Own simplified implemenation of message handler, for sending messages only.  Simplified and without synchronization, so
     * must be called synhcronized or from a single thread!
     *
     */
    private class SingleThreadMessageHandler extends AbstractMqttMessageHandler implements MqttCallback, MqttPahoComponent {
        private final MqttPahoClientFactory clientFactory;
        private volatile IMqttAsyncClient client;

        public SingleThreadMessageHandler(final String clientId, final MqttPahoClientFactory clientFactory) {
            super(null, clientId);
            this.clientFactory = clientFactory;
        }

        @Override
        protected void onInit() {
            super.onInit();

            final DefaultPahoMessageConverter defaultConverter = new DefaultPahoMessageConverter(getDefaultQos(),
                getQosProcessor(), getDefaultRetained(), getRetainedProcessor());

            setConverter(defaultConverter);
        }

        @Override
        protected void doStart() {
        }

        @Override
        protected void doStop() {
            try {
                if (this.client != null) {
                    this.client.disconnect().waitForCompletion(getDisconnectCompletionTimeout());
                    closeClient();
                }
            }
            catch (final MqttException me) {
                logger.error(me, "Disconnect failed");
            }
        }

        @Override
        protected void publish(final String topic, final Object mqttMessage, final Message<?> message) {
            try {
                getConnection().publish(topic, (MqttMessage) mqttMessage);
            }
            catch (final MqttException me) {
                throw new MessageHandlingException(message, "Publish failed", me);
            }
        }

        private IMqttAsyncClient getConnection() throws MqttException {
            if (this.client != null && !this.client.isConnected()) {
                closeClient();
            }

            if (this.client == null) {
                try {
                    final MqttConnectOptions connectionOptions = this.clientFactory.getConnectionOptions();

                    this.client = this.clientFactory.getAsyncClientInstance(this.getUrl(), this.getClientId());
                    incrementClientInstance();
                    this.client.setCallback(this);
                    this.client.connect(connectionOptions).waitForCompletion(getCompletionTimeout());
                }
                catch (final MqttException me) {
                    closeClient();

                    throw new MessagingException("Failed to connect", me);
                }
            }

            return this.client;
        }

        @Override
        public synchronized void connectionLost(final Throwable cause) {
            logger.error("Connection lost");

            closeClient();
        }

        private void closeClient() {
            if (this.client != null) {
                this.client.setCallback(null);

                try {
                    this.client.close();
                } catch (final MqttException me) {
                    logger.error(me, "Exception when closing");
                }

                this.client = null;
            }
        }

        @Override
        public void messageArrived(final String topic, final MqttMessage message) {
        }

        @Override
        public void deliveryComplete(final IMqttDeliveryToken token) {
        }

        @Override
        public MqttConnectOptions getConnectionInfo() {
            return this.clientFactory.getConnectionOptions();
        }
    }
}
