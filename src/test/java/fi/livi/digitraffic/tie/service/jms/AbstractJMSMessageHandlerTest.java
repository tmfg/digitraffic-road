package fi.livi.digitraffic.tie.service.jms;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.activemq.artemis.api.core.QueueAttributes;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.FailoverEventListener;
import org.apache.activemq.artemis.api.core.client.SendAcknowledgementHandler;
import org.apache.activemq.artemis.api.core.client.SessionFailureListener;
import org.apache.activemq.artemis.jms.client.ActiveMQBytesMessage;
import org.apache.activemq.artemis.jms.client.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.transaction.TestTransaction;

import com.google.protobuf.AbstractMessage;

import fi.livi.digitraffic.common.service.locking.LockingService;
import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.SensorDataTestUpdateService;
import fi.livi.digitraffic.tie.service.lotju.LotjuCameraStationMetadataClient;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateHandler;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;

public abstract class AbstractJMSMessageHandlerTest extends AbstractDaemonTest {

    @Autowired
    protected RoadStationSensorService roadStationSensorService;

    @Autowired
    protected SensorDataTestUpdateService sensorDataUpdateService;

    @MockBean
    protected LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @SpyBean
    protected CameraImageUpdateHandler cameraImageUpdateHandler;

    @Autowired
    protected LockingService lockingService;

    protected List<RoadStationSensor> findPublishableRoadStationSensors(final RoadStationType roadStationType) {
        return roadStationSensorService
            .findAllPublishableRoadStationSensors(roadStationType);
    }

    protected void flushSensorBuffer(final boolean tms) {
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }

        TestTransaction.start();

        if (tms) {
            sensorDataUpdateService.flushTmsBuffer();
        } else {
            sensorDataUpdateService.flushWeatherBuffer();
        }

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    public static ActiveMQBytesMessage createBytesMessage(final AbstractMessage protoBuffMessage) throws JMSException, IOException {
        final org.apache.commons.io.output.ByteArrayOutputStream bous = new org.apache.commons.io.output.ByteArrayOutputStream(0);
        protoBuffMessage.writeDelimitedTo(bous);
        final byte[] protoBytes = bous.toByteArray();

        final ActiveMQBytesMessage bytesMessage = mock(ActiveMQBytesMessage.class);
        when(bytesMessage.getBody((byte[].class))).thenReturn(protoBytes);

        return bytesMessage;
    }

    public static ActiveMQTextMessage createTextMessage(final String content, final String filename) {
        final ClientSession session = createClientSession();
        return new ActiveMQTextMessage(session) {
            @Override
            public void setText(final String s) {

            }

            @Override
            public String getText() {
                return content;
            }

            @Override
            public String getJMSMessageID() {
                return filename;
            }

            @Override
            public void setJMSMessageID(final String s) {

            }

            @Override
            public long getJMSTimestamp() {
                return 0;
            }

            @Override
            public void setJMSTimestamp(final long l) {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() {
                return new byte[0];
            }

            @Override
            public void setJMSCorrelationIDAsBytes(final byte[] bytes) {

            }

            @Override
            public void setJMSCorrelationID(final String s) {

            }

            @Override
            public String getJMSCorrelationID() {
                return null;
            }

            @Override
            public Destination getJMSReplyTo() {
                return null;
            }

            @Override
            public void setJMSReplyTo(final Destination destination) {

            }

            @Override
            public Destination getJMSDestination() {
                return null;
            }

            @Override
            public void setJMSDestination(final Destination destination) {

            }

            @Override
            public int getJMSDeliveryMode() {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(final int i) {

            }

            @Override
            public boolean getJMSRedelivered() {
                return false;
            }

            @Override
            public void setJMSRedelivered(final boolean b) {

            }

            @Override
            public String getJMSType() {
                return null;
            }

            @Override
            public void setJMSType(final String s) {

            }

            @Override
            public long getJMSExpiration() {
                return 0;
            }

            @Override
            public void setJMSExpiration(final long l) {

            }

            @Override
            public int getJMSPriority() {
                return 0;
            }

            @Override
            public void setJMSPriority(final int i) {

            }

            @Override
            public void clearProperties() {

            }

            @Override
            public boolean propertyExists(final String s) {
                return false;
            }

            @Override
            public boolean getBooleanProperty(final String s) {
                return false;
            }

            @Override
            public byte getByteProperty(final String s) {
                return 0;
            }

            @Override
            public short getShortProperty(final String s) {
                return 0;
            }

            @Override
            public int getIntProperty(final String s) {
                return 0;
            }

            @Override
            public long getLongProperty(final String s) {
                return 0;
            }

            @Override
            public float getFloatProperty(final String s) {
                return 0;
            }

            @Override
            public double getDoubleProperty(final String s) {
                return 0;
            }

            @Override
            public String getStringProperty(final String s) {
                return null;
            }

            @Override
            public Object getObjectProperty(final String s) {
                return null;
            }

            @Override
            public Enumeration<String> getPropertyNames() {
                return null;
            }

            @Override
            public void setBooleanProperty(final String s, final boolean b) {

            }

            @Override
            public void setByteProperty(final String s, final byte b) {

            }

            @Override
            public void setShortProperty(final String s, final short i) {

            }

            @Override
            public void setIntProperty(final String s, final int i) {

            }

            @Override
            public void setLongProperty(final String s, final long l) {

            }

            @Override
            public void setFloatProperty(final String s, final float v) {

            }

            @Override
            public void setDoubleProperty(final String s, final double v) {

            }

            @Override
            public void setStringProperty(final String s, final String s1) {

            }

            @Override
            public void setObjectProperty(final String s, final Object o) {

            }

            @Override
            public void acknowledge() {

            }

            @Override
            public void clearBody() {

            }
        };
    }

    private static ClientSession createClientSession() {
        return new ClientSession() {
            @Override
            public ClientSession start() {
                return null;
            }

            @Override
            public void stop() {

            }

            @Override
            public void close() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void addFailureListener(final SessionFailureListener listener) {

            }

            @Override
            public boolean removeFailureListener(final SessionFailureListener listener) {
                return false;
            }

            @Override
            public void addFailoverListener(final FailoverEventListener listener) {

            }

            @Override
            public boolean removeFailoverListener(final FailoverEventListener listener) {
                return false;
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public void createAddress(final SimpleString address, final EnumSet<RoutingType> routingTypes, final boolean autoCreated)
                {

            }

            @Override
            public void createAddress(final SimpleString address, final Set<RoutingType> routingTypes, final boolean autoCreated)
                {

            }

            @Override
            public void createAddress(final SimpleString address, final RoutingType routingType, final boolean autoCreated) {

            }

            @Override
            public void createQueue(final QueueConfiguration queueConfiguration) {

            }

            @Override
            public void createSharedQueue(final QueueConfiguration queueConfiguration) {

            }

            @Override
            public void createQueue(final SimpleString address, final SimpleString queueName, final boolean durable) {

            }

            @Override
            public void createSharedQueue(final SimpleString address, final SimpleString queueName, final boolean durable) {

            }

            @Override
            public void createSharedQueue(final SimpleString address, final SimpleString queueName, final SimpleString filter, final boolean durable)
                {

            }

            @Override
            public void createQueue(final String address, final String queueName, final boolean durable) {

            }

            @Override
            public void createQueue(final String address, final String queueName) {

            }

            @Override
            public void createQueue(final SimpleString address, final SimpleString queueName) {

            }

            @Override
            public void createQueue(final SimpleString address, final SimpleString queueName, final SimpleString filter, final boolean durable)
                {

            }

            @Override
            public void createQueue(final String address, final String queueName, final String filter, final boolean durable)
                {

            }

            @Override
            public void createQueue(final SimpleString address, final SimpleString queueName, final SimpleString filter, final boolean durable,
                                    final boolean autoCreated)
                {

            }

            @Override
            public void createQueue(final String address, final String queueName, final String filter, final boolean durable,
                                    final boolean autoCreated) {

            }

            @Override
            public void createTemporaryQueue(final SimpleString address, final SimpleString queueName) {

            }

            @Override
            public void createTemporaryQueue(final String address, final String queueName) {

            }

            @Override
            public void createTemporaryQueue(final SimpleString address, final SimpleString queueName, final SimpleString filter)
                {

            }

            @Override
            public void createTemporaryQueue(final String address, final String queueName, final String filter) {

            }

            @Override
            public void createQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName, final boolean durable)
                {

            }

            @Override
            public void createSharedQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                          final boolean durable)
                {

            }

            @Override
            public void createSharedQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                          final SimpleString filter, final boolean durable)
                {

            }

            @Override
            public void createSharedQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                          final SimpleString filter, final boolean durable,
                                          final Integer maxConsumers, final Boolean purgeOnNoConsumers, final Boolean exclusive,
                                          final Boolean lastValue)
                {

            }

            @Override
            public void createSharedQueue(final SimpleString address, final SimpleString queueName, final QueueAttributes queueAttributes)
                {

            }

            @Override
            public void createQueue(final String address, final RoutingType routingType, final String queueName, final boolean durable)
                {

            }

            @Override
            public void createQueue(final String address, final RoutingType routingType, final String queueName) {

            }

            @Override
            public void createQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName)
                {

            }

            @Override
            public void createQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                    final SimpleString filter, final boolean durable)
                {

            }

            @Override
            public void createQueue(final String address, final RoutingType routingType, final String queueName, final String filter,
                                    final boolean durable)
                {

            }

            @Override
            public void createQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                    final SimpleString filter, final boolean durable,
                                    final boolean autoCreated) {

            }

            @Override
            public void createQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                    final SimpleString filter, final boolean durable,
                                    final boolean autoCreated, final int maxConsumers, final boolean purgeOnNoConsumers) {

            }

            @Override
            public void createQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                    final SimpleString filter, final boolean durable,
                                    final boolean autoCreated, final int maxConsumers, final boolean purgeOnNoConsumers, final Boolean exclusive,
                                    final Boolean lastValue)
                {

            }

            @Override
            public void createQueue(final SimpleString address, final SimpleString queueName, final boolean autoCreated,
                                    final QueueAttributes queueAttributes)
                {

            }

            @Override
            public void createQueue(final String address, final RoutingType routingType, final String queueName, final String filter,
                                    final boolean durable, final boolean autoCreated)
                {

            }

            @Override
            public void createQueue(final String address, final RoutingType routingType, final String queueName, final String filter,
                                    final boolean durable, final boolean autoCreated,
                                    final int maxConsumers, final boolean purgeOnNoConsumers) {

            }

            @Override
            public void createQueue(final String address, final RoutingType routingType, final String queueName, final String filter,
                                    final boolean durable, final boolean autoCreated,
                                    final int maxConsumers, final boolean purgeOnNoConsumers, final Boolean exclusive, final Boolean lastValue)
                {

            }

            @Override
            public void createTemporaryQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName)
                {

            }

            @Override
            public void createTemporaryQueue(final String address, final RoutingType routingType, final String queueName) {

            }

            @Override
            public void createTemporaryQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                             final SimpleString filter,
                                             final int maxConsumers, final boolean purgeOnNoConsumers, final Boolean exclusive,
                                             final Boolean lastValue)
                {

            }

            @Override
            public void createTemporaryQueue(final SimpleString address, final SimpleString queueName, final QueueAttributes queueAttributes)
                {

            }

            @Override
            public void createTemporaryQueue(final SimpleString address, final RoutingType routingType, final SimpleString queueName,
                                             final SimpleString filter)
                {

            }

            @Override
            public void createTemporaryQueue(final String address, final RoutingType routingType, final String queueName, final String filter)
                {

            }

            @Override
            public void deleteQueue(final SimpleString queueName) {

            }

            @Override
            public void deleteQueue(final String queueName) {

            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final String queueName) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName, final SimpleString filter) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final String queueName, final String filter) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName, final boolean browseOnly) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final String queueName, final boolean browseOnly) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final String queueName, final String filter, final boolean browseOnly) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName, final SimpleString filter, final boolean browseOnly)
                {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName, final SimpleString filter, final int priority,
                                                 final boolean browseOnly)
                {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName, final SimpleString filter, final int windowSize, final int maxRate,
                                                 final boolean browseOnly)
                {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final SimpleString queueName, final SimpleString filter, final int priority, final int windowSize,
                                                 final int maxRate,
                                                 final boolean browseOnly) {
                return null;
            }

            @Override
            public ClientConsumer createConsumer(final String queueName, final String filter, final int windowSize, final int maxRate,
                                                 final boolean browseOnly)
                {
                return null;
            }

            @Override
            public ClientProducer createProducer() {
                return null;
            }

            @Override
            public ClientProducer createProducer(final SimpleString address) {
                return null;
            }

            @Override
            public ClientProducer createProducer(final String address) {
                return null;
            }

            @Override
            public ClientProducer createProducer(final SimpleString address, final int rate) {
                return null;
            }

            @Override
            public ClientMessage createMessage(final boolean durable) {
                return null;
            }

            @Override
            public ClientMessage createMessage(final byte type, final boolean durable) {
                return null;
            }

            @Override
            public ClientMessage createMessage(final byte type, final boolean durable, final long expiration, final long timestamp,
                                               final byte priority) {
                return null;
            }

            @Override
            public QueueQuery queueQuery(final SimpleString queueName) {
                return null;
            }

            @Override
            public AddressQuery addressQuery(final SimpleString address) {
                return null;
            }

            @Override
            public XAResource getXAResource() {
                return null;
            }

            @Override
            public boolean isXA() {
                return false;
            }

            @Override
            public void commit() {

            }

            @Override
            public void commit(final boolean block) {

            }

            @Override
            public void rollback() {

            }

            @Override
            public void rollback(final boolean considerLastMessageAsDelivered) {

            }

            @Override
            public boolean isRollbackOnly() {
                return false;
            }

            @Override
            public boolean isAutoCommitSends() {
                return false;
            }

            @Override
            public boolean isAutoCommitAcks() {
                return false;
            }

            @Override
            public boolean isBlockOnAcknowledge() {
                return false;
            }

            @Override
            public ClientSession setSendAcknowledgementHandler(final SendAcknowledgementHandler handler) {
                return null;
            }

            @Override
            public void addMetaData(final String key, final String data) {

            }

            @Override
            public void addUniqueMetaData(final String key, final String data) {

            }

            @Override
            public ClientSessionFactory getSessionFactory() {
                return null;
            }

            @Override
            public void commit(final Xid xid, final boolean onePhase) {

            }

            @Override
            public void end(final Xid xid, final int flags) {

            }

            @Override
            public void forget(final Xid xid) {

            }

            @Override
            public int getTransactionTimeout() {
                return 0;
            }

            @Override
            public boolean isSameRM(final XAResource xares) {
                return false;
            }

            @Override
            public int prepare(final Xid xid) {
                return 0;
            }

            @Override
            public Xid[] recover(final int flag) {
                return new Xid[0];
            }

            @Override
            public void rollback(final Xid xid) {

            }

            @Override
            public boolean setTransactionTimeout(final int seconds) {
                return false;
            }

            @Override
            public void start(final Xid xid, final int flags) {

            }
        };
    }
}
