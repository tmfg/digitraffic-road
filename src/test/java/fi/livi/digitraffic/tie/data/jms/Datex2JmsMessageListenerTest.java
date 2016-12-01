package fi.livi.digitraffic.tie.data.jms;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.dto.datex2.Datex2RootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.Datex2DataService;
import fi.livi.digitraffic.tie.data.service.LockingService;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;

@Transactional
public class Datex2JmsMessageListenerTest extends AbstractJmsMessageListenerTest {
    
    private static final Logger log = LoggerFactory.getLogger(Datex2JmsMessageListenerTest.class);

    @Autowired
    private Datex2DataService datex2DataService;

    @Autowired
    LockingService lockingService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    ResourceLoader resourceLoader;

    @Test
    public void testDatex2ReceiveMessages() throws JAXBException, DatatypeConfigurationException, IOException {

        log.info("Delete all Datex2 messages");
        datex2Repository.deleteAll();

        // Create listener
        JMSMessageListener.JMSDataUpdater<D2LogicalModel> dataUpdater = (data) -> {
            datex2DataService.updateDatex2Data(data);
        };
        JMSMessageListener<D2LogicalModel> datexJmsMessageListener =
                new JMSMessageListener<D2LogicalModel>(D2LogicalModel.class, dataUpdater, false, log);

        Resource[] datex2Resources = loadResources("classpath:lotju/datex2/InfoXML_*.xml");
        readAndSendMessages(datex2Resources, datexJmsMessageListener, false);

        Datex2RootDataObjectDto dto = datex2DataService.findActiveDatex2Data(false);
        List<Datex2> datex2s = dto.getDatex2s();

        Assert.assertTrue(datex2s.size() == 1);
        Assert.assertTrue(datex2s.get(0).getSituations().get(0).getSituationId().equals("GUID50006936"));

        Datex2RootDataObjectDto bySituation1 = datex2DataService.findAllDatex2DataBySituationId("GUID50006936");
        List<Datex2> bySituationDatex2s = bySituation1.getDatex2s();
        Assert.assertTrue(bySituationDatex2s.size() == 1);
        Assert.assertTrue(bySituationDatex2s.get(0).getSituations().get(0).getSituationId().equals("GUID50006936"));

        Datex2RootDataObjectDto bySituation2 = datex2DataService.findAllDatex2DataBySituationId("GUID50006401");
        List<Datex2> bySituation2Datex2s = bySituation2.getDatex2s();
        Assert.assertTrue(bySituation2Datex2s.size() == 3);
        for (Datex2 datex2 : bySituation2Datex2s) {
            datex2.getSituations().get(0).getSituationId().equals("GUID50006401");
        }

        Datex2RootDataObjectDto byTimeSituation2 = datex2DataService.findDatex2Data(null, 2016, 10);
        List<Datex2> byTimeSituation22Datex2s = byTimeSituation2.getDatex2s();
        Assert.assertTrue(byTimeSituation22Datex2s.size() == 6);
    }

    // Just for data importing for testing
    @Ignore
    @Test
    public void testImportData() throws JAXBException, DatatypeConfigurationException, IOException {

        log.info("Delete old messages");
        datex2Repository.deleteAll();

        JMSMessageListener.JMSDataUpdater<D2LogicalModel> dataUpdater = (data) -> {
            datex2DataService.updateDatex2Data(data);
        };

        JMSMessageListener<D2LogicalModel> datexJmsMessageListener =
                new JMSMessageListener<D2LogicalModel>(D2LogicalModel.class, dataUpdater, false, log);

        log.info("Read Datex2 messages from filesystem");
        Resource[] datex2Resources = loadResources("classpath:lotju/datex2/InfoXML_*.xml");
//        Resource[] datex2Resources = loadResources("file:/Users/jouniso/tyo/digitraffic/Data/datex2/formatted/ftp.tiehallinto.fi/incidents/datex2/InfoXML*.xml");

        readAndSendMessages(datex2Resources, datexJmsMessageListener, true);

        log.info("Persist changes");
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    Resource[] loadResources(String pattern) throws IOException {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(pattern);
    }

    private void readAndSendMessages(Resource[] datex2Resources, JMSMessageListener<D2LogicalModel> lamJmsMessageListener, boolean autoFix) throws IOException {
        log.info("Read and send " + datex2Resources.length + " Datex2 messages...");
        for (Resource datex2Resource : datex2Resources) {
            File datex2file = datex2Resource.getFile();
            String content = FileUtils.readFileToString(datex2file, StandardCharsets.UTF_8);
            try {
                lamJmsMessageListener.onMessage(createTextMessage(autoFix ?
                                                                        content.replace("Both", "both")
                                                                                .replace("<alertCPoint/>", "") :
                                                                        content,
                                                                  datex2file.getName()));
            } catch (Exception e) {
                log.error("Error with file " + datex2file.getName());
                throw e;
            }
        }
    }
}
