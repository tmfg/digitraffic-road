package fi.livi.digitraffic.tie.data.jms;

import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Datex2JmsMessageListenerTest extends MetadataIntegrationTest {
    
    private static final Logger log = LoggerFactory.getLogger(Datex2JmsMessageListenerTest.class);

    @Autowired
    private Datex2DataService datex2DataService;

    @Autowired
    LockingService lockingService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Before
    public void setUpTestData() {
        // TODO
    }

    @Test
    public void test1PerformanceForReceivedMessages() throws JAXBException, DatatypeConfigurationException {

        AbstractJMSMessageListener<D2LogicalModel> lamJmsMessageListener =
                new AbstractJMSMessageListener<D2LogicalModel>(D2LogicalModel.class, log) {
            @Override
            protected void handleData(List<Pair<D2LogicalModel, String>> data) {
                for (Pair<D2LogicalModel, String> pair : data) {
                    log.info("Message:\n" + ToStringHelpper.toStringFull(pair.getLeft()));
                }
                datex2DataService.updateDatex2Data(data);
            }
        };

        TextMessage txtMsg = getTextMessage();

        lamJmsMessageListener.onMessage(txtMsg);
        lamJmsMessageListener.drainQueue();

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    private static final String DATEX2 =
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<d2LogicalModel xmlns=\"http://datex2.eu/schema/2/2_0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" modelBaseVersion=\"2\" xsi:schemaLocation=\"http://datex2.eu/schema/2/2_0 https://raw.githubusercontent.com/finnishtransportagency/metadata/master/schema/DATEXIISchema_2_2_3_with_definitions_FI.xsd\">\n" +
                    "<exchange>\n" +
                    "<supplierIdentification>\n" +
                    "<country>fi</country>\n" +
                    "<nationalIdentifier>FTA</nationalIdentifier>\n" +
                    "</supplierIdentification>\n" +
                    "</exchange>\n" +
                    "<payloadPublication lang=\"fi\" xsi:type=\"SituationPublication\">\n" +
                    "<publicationTime>2016-11-02T08:35:52.240+02:00</publicationTime>\n" +
                    "<publicationCreator>\n" +
                    "<country>fi</country>\n" +
                    "<nationalIdentifier>FTA</nationalIdentifier>\n" +
                    "</publicationCreator>\n" +
                    "<situation id=\"GUID50000002\" version=\"1\">\n" +
                    "<headerInformation>\n" +
                    "<confidentiality>restrictedToAuthoritiesTrafficOperatorsAndPublishers</confidentiality>\n" +
                    "<informationStatus>real</informationStatus>\n" +
                    "</headerInformation>\n" +
                    "<situationRecord id=\"GUID5000000201\" version=\"1\" xsi:type=\"Accident\">\n" +
                    "<situationRecordCreationTime>2016-11-02T08:35:52.240+02:00</situationRecordCreationTime>\n" +
                    "<situationRecordVersionTime>2016-11-02T08:35:52.240+02:00</situationRecordVersionTime>\n" +
                    "<situationRecordFirstSupplierVersionTime>2016-11-02T08:35:52.240+02:00</situationRecordFirstSupplierVersionTime>\n" +
                    "<probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
                    "<validity>\n" +
                    "<validityStatus>active</validityStatus>\n" +
                    "<validityTimeSpecification>\n" +
                    "<overallStartTime>2016-11-02T08:35:52.240+02:00</overallStartTime>\n" +
                    "</validityTimeSpecification>\n" +
                    "</validity>\n" +
                    "<generalPublicComment>\n" +
                    "<comment>\n" +
                    "<values>\n" +
                    "<value lang=\"fi\">Savonlinna. Liikennetiedote onnettomuudesta. \n" +
                    "\n" +
                    "Savonlinna. \n" +
                    "\n" +
                    "Onnettomuus. \n" +
                    "Raskaan ajoneuvon nostotyö. \n" +
                    "\n" +
                    "Lisätieto: TESTIVIESTI 2\n" +
                    "\n" +
                    "Ajankohta: 02.11.2016 klo 08:35 toistaiseksi.\n" +
                    "\n" +
                    "Liikenne- ja kelitiedot verkossa: http://liikennetilanne.liikennevirasto.fi/\n" +
                    "\n" +
                    "Liikenneviraston tieliikennekeskus Tampere\n" +
                    "Puh: 0206373330\n" +
                    "Faksi: 0206373712\n" +
                    "Sähköposti: tampere.liikennekeskus@liikennevirasto.fi</value>\n" +
                    "</values>\n" +
                    "</comment>\n" +
                    "</generalPublicComment>\n" +
                    "<groupOfLocations xsi:type=\"Area\">\n" +
                    "<alertCArea>\n" +
                    "<alertCLocationCountryCode>6</alertCLocationCountryCode>\n" +
                    "<alertCLocationTableNumber>17</alertCLocationTableNumber>\n" +
                    "<alertCLocationTableVersion>1.11.01</alertCLocationTableVersion>\n" +
                    "<areaLocation>\n" +
                    "<specificLocation>740</specificLocation>\n" +
                    "</areaLocation>\n" +
                    "</alertCArea>\n" +
                    "</groupOfLocations>\n" +
                    "<accidentType>accident</accidentType>\n" +
                    "</situationRecord>\n" +
                    "<situationRecord id=\"GUID5000000202\" version=\"1\" xsi:type=\"GeneralObstruction\">\n" +
                    "<situationRecordCreationTime>2016-11-02T08:35:52.240+02:00</situationRecordCreationTime>\n" +
                    "<situationRecordVersionTime>2016-11-02T08:35:52.240+02:00</situationRecordVersionTime>\n" +
                    "<situationRecordFirstSupplierVersionTime>2016-11-02T08:35:52.240+02:00</situationRecordFirstSupplierVersionTime>\n" +
                    "<probabilityOfOccurrence>certain</probabilityOfOccurrence>\n" +
                    "<validity>\n" +
                    "<validityStatus>active</validityStatus>\n" +
                    "<validityTimeSpecification>\n" +
                    "<overallStartTime>2016-11-02T08:35:52.240+02:00</overallStartTime>\n" +
                    "</validityTimeSpecification>\n" +
                    "</validity>\n" +
                    "<groupOfLocations xsi:type=\"Point\"/>\n" +
                    "<obstructionType>craneOperating</obstructionType>\n" +
                    "</situationRecord>\n" +
                    "</situation>\n" +
                    "</payloadPublication>\n" +
                    "</d2LogicalModel>\n";

    public TextMessage getTextMessage() {
        return new TextMessage() {
            @Override
            public void setText(String s) throws JMSException {

            }

            @Override
            public String getText() throws JMSException {
                return DATEX2;
            }

            @Override
            public String getJMSMessageID() throws JMSException {
                return null;
            }

            @Override
            public void setJMSMessageID(String s) throws JMSException {

            }

            @Override
            public long getJMSTimestamp() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSTimestamp(long l) throws JMSException {

            }

            @Override
            public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
                return new byte[0];
            }

            @Override
            public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {

            }

            @Override
            public void setJMSCorrelationID(String s) throws JMSException {

            }

            @Override
            public String getJMSCorrelationID() throws JMSException {
                return null;
            }

            @Override
            public Destination getJMSReplyTo() throws JMSException {
                return null;
            }

            @Override
            public void setJMSReplyTo(Destination destination) throws JMSException {

            }

            @Override
            public Destination getJMSDestination() throws JMSException {
                return null;
            }

            @Override
            public void setJMSDestination(Destination destination) throws JMSException {

            }

            @Override
            public int getJMSDeliveryMode() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSDeliveryMode(int i) throws JMSException {

            }

            @Override
            public boolean getJMSRedelivered() throws JMSException {
                return false;
            }

            @Override
            public void setJMSRedelivered(boolean b) throws JMSException {

            }

            @Override
            public String getJMSType() throws JMSException {
                return null;
            }

            @Override
            public void setJMSType(String s) throws JMSException {

            }

            @Override
            public long getJMSExpiration() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSExpiration(long l) throws JMSException {

            }

            @Override
            public int getJMSPriority() throws JMSException {
                return 0;
            }

            @Override
            public void setJMSPriority(int i) throws JMSException {

            }

            @Override
            public void clearProperties() throws JMSException {

            }

            @Override
            public boolean propertyExists(String s) throws JMSException {
                return false;
            }

            @Override
            public boolean getBooleanProperty(String s) throws JMSException {
                return false;
            }

            @Override
            public byte getByteProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public short getShortProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public int getIntProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public long getLongProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public float getFloatProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public double getDoubleProperty(String s) throws JMSException {
                return 0;
            }

            @Override
            public String getStringProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Object getObjectProperty(String s) throws JMSException {
                return null;
            }

            @Override
            public Enumeration getPropertyNames() throws JMSException {
                return null;
            }

            @Override
            public void setBooleanProperty(String s, boolean b) throws JMSException {

            }

            @Override
            public void setByteProperty(String s, byte b) throws JMSException {

            }

            @Override
            public void setShortProperty(String s, short i) throws JMSException {

            }

            @Override
            public void setIntProperty(String s, int i) throws JMSException {

            }

            @Override
            public void setLongProperty(String s, long l) throws JMSException {

            }

            @Override
            public void setFloatProperty(String s, float v) throws JMSException {

            }

            @Override
            public void setDoubleProperty(String s, double v) throws JMSException {

            }

            @Override
            public void setStringProperty(String s, String s1) throws JMSException {

            }

            @Override
            public void setObjectProperty(String s, Object o) throws JMSException {

            }

            @Override
            public void acknowledge() throws JMSException {

            }

            @Override
            public void clearBody() throws JMSException {

            }
        };
    }
}
