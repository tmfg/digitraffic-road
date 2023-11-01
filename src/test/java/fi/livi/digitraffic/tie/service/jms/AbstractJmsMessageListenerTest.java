package fi.livi.digitraffic.tie.service.jms;

import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.transaction.TestTransaction;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationSensor;
import fi.livi.digitraffic.tie.model.roadstation.RoadStationType;
import fi.livi.digitraffic.tie.service.RoadStationSensorService;
import fi.livi.digitraffic.tie.service.SensorDataTestUpdateService;
import fi.livi.digitraffic.tie.service.lotju.LotjuCameraStationMetadataClient;
import fi.livi.digitraffic.tie.service.weathercam.CameraImageUpdateHandler;

public abstract class AbstractJmsMessageListenerTest extends AbstractDaemonTest {

    @Autowired
    protected RoadStationSensorService roadStationSensorService;

    @Autowired
    protected SensorDataTestUpdateService sensorDataUpdateService;

    @MockBean
    protected LotjuCameraStationMetadataClient lotjuCameraStationMetadataClient;

    @SpyBean
    protected CameraImageUpdateHandler cameraImageUpdateHandler;

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

    public static TextMessage createTextMessage(final String content, final String filename) {
        return new TextMessage() {
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
}
