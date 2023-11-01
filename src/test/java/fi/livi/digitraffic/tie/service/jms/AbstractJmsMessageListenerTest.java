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

    protected void flushSensorBuffer(boolean tms) {
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
            public void setText(String s) {

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
            public void setJMSMessageID(String s) {

            }

            @Override
            public long getJMSTimestamp() {
                return 0;
            }

            @Override
            public void setJMSTimestamp(long l) {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() {
                return new byte[0];
            }

            @Override
            public void setJMSCorrelationIDAsBytes(byte[] bytes) {

            }

            @Override
            public void setJMSCorrelationID(String s) {

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
            public void setJMSReplyTo(Destination destination) {

            }

            @Override
            public Destination getJMSDestination() {
                return null;
            }

            @Override
            public void setJMSDestination(Destination destination) {

            }

            @Override
            public int getJMSDeliveryMode() {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(int i) {

            }

            @Override
            public boolean getJMSRedelivered() {
                return false;
            }

            @Override
            public void setJMSRedelivered(boolean b) {

            }

            @Override
            public String getJMSType() {
                return null;
            }

            @Override
            public void setJMSType(String s) {

            }

            @Override
            public long getJMSExpiration() {
                return 0;
            }

            @Override
            public void setJMSExpiration(long l) {

            }

            @Override
            public int getJMSPriority() {
                return 0;
            }

            @Override
            public void setJMSPriority(int i) {

            }

            @Override
            public void clearProperties() {

            }

            @Override
            public boolean propertyExists(String s) {
                return false;
            }

            @Override
            public boolean getBooleanProperty(String s) {
                return false;
            }

            @Override
            public byte getByteProperty(String s) {
                return 0;
            }

            @Override
            public short getShortProperty(String s) {
                return 0;
            }

            @Override
            public int getIntProperty(String s) {
                return 0;
            }

            @Override
            public long getLongProperty(String s) {
                return 0;
            }

            @Override
            public float getFloatProperty(String s) {
                return 0;
            }

            @Override
            public double getDoubleProperty(String s) {
                return 0;
            }

            @Override
            public String getStringProperty(String s) {
                return null;
            }

            @Override
            public Object getObjectProperty(String s) {
                return null;
            }

            @Override
            public Enumeration<String> getPropertyNames() {
                return null;
            }

            @Override
            public void setBooleanProperty(String s, boolean b) {

            }

            @Override
            public void setByteProperty(String s, byte b) {

            }

            @Override
            public void setShortProperty(String s, short i) {

            }

            @Override
            public void setIntProperty(String s, int i) {

            }

            @Override
            public void setLongProperty(String s, long l) {

            }

            @Override
            public void setFloatProperty(String s, float v) {

            }

            @Override
            public void setDoubleProperty(String s, double v) {

            }

            @Override
            public void setStringProperty(String s, String s1) {

            }

            @Override
            public void setObjectProperty(String s, Object o) {

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
