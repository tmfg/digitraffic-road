package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TimestampedTrafficDisorderDatex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

public class Datex2DataServiceTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(Datex2DataServiceTest.class);

    @Autowired
    Datex2DataService datex2DataService;

    @Autowired
    Datex2Repository datex2Repository;

    @Autowired
    Jaxb2Marshaller jaxb2Marshaller;

    private static final String situationId1 = "GUID50005166";
    private static final String situationId2 = "GUID50006936";
    private String datex2Content1;
    private String datex2Content2;

    @Before
    public void init() throws JAXBException, IOException {
        datex2Content1 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-09-12-20-51-24-602.xml");
        datex2Content2 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-11-17-18-34-36-299.xml");
    }

    @Test
    public void testUpdateDatex2Data() throws JAXBException, IOException {
        final String situationId1 = "GUID50005166";
        final String situationId2 = "GUID50006936";

        datex2Repository.deleteAll();
        Assert.assertTrue(datex2Repository.findAll().isEmpty());

        try {
            datex2DataService.findAllDatex2ResponsesBySituationId(situationId1);
            Assert.fail("ObjectNotFoundException should be raised");
        } catch (ObjectNotFoundException onfe) {
            // OK
        }

        updateDatex2Data(datex2Content1);
        findDatex2AndAssert(situationId1, true);
        findDatex2AndAssert(situationId2, false);
        updateDatex2Data(datex2Content2);

        Assert.assertTrue(datex2Repository.findAll().size() == 2);

        findDatex2AndAssert(situationId1, true);
        findDatex2AndAssert(situationId2, true);

        TrafficDisordersDatex2Response allActive = datex2DataService.findActiveDatex2Response();
        Assert.assertTrue(allActive.getDisorder().size() == 1);
        SituationPublication active = getSituationPublication(allActive);
        Assert.assertTrue(active.getSituation().size() == 1);
        Assert.assertTrue(active.getSituation().get(0).getId().equals(situationId2));
    }

    @Test
    public void testMultiThreadUnmarshall() throws InterruptedException {
        Datex2Thread first = new Datex2Thread("datex2Content1", datex2Content1);
        Datex2Thread second = new Datex2Thread("datex2Content2", datex2Content2);
        while (first.isRunning() || second.isRunning()) {
            log.info("Sleep");
            Thread.sleep(100);
        }
        Assert.assertTrue(first.isSuccess());
        Assert.assertTrue(second.isSuccess());
    }

    private TrafficDisordersDatex2Response findDatex2AndAssert(String situationId, boolean found) {
        try {
            TrafficDisordersDatex2Response response = datex2DataService.findAllDatex2ResponsesBySituationId(situationId);
            Assert.assertTrue(found);
            SituationPublication s = getSituationPublication(response);
            Assert.assertTrue(s.getSituation().get(0).getId().equals(situationId));
            return response;
        } catch (ObjectNotFoundException onfe) {
            // OK
            if (found) {
                Assert.fail("Situation " + situationId + " should have found");
            }
            return null;
        }
    }

    private void saveDatex2(String datex2Content) {
        Datex2 d2 = new Datex2();
        d2.setMessage(datex2Content);
        d2.setImportTime(ZonedDateTime.now());
        datex2Repository.save(d2);
    }

    private static SituationPublication getSituationPublication(TrafficDisordersDatex2Response response) {
        Assert.assertTrue(response.getDisorder().size() == 1);
        return ((SituationPublication) response.getDisorder().get(0).getD2LogicalModel().getPayloadPublication());
    }

    private void updateDatex2Data(final String datex2Content) throws IOException, JAXBException {
        Object object = jaxb2Marshaller.unmarshal(new StringSource(datex2Content));
        if (object instanceof JAXBElement) {
            object = ((JAXBElement) object).getValue();
        }
        D2LogicalModel d2LogicalModel = (D2LogicalModel)object;
        datex2DataService.updateDatex2Data(Collections.singletonList(new Datex2MessageDto(datex2Content, null, d2LogicalModel)));
    }

    private class Datex2Thread implements Runnable {

        Thread t;
        private final String name;
        private final String datex2;
        private boolean running = false;
        private boolean success = false;

        Datex2Thread(final String name, final String datex2) {
            this.name = name;
            this.datex2 = datex2;
            t = new Thread(this, name);
            log.info("Start new thread: {}", t);
            t.start(); // Start the thread
            running = true;
        }

        public void run() {
            try {
                for (int i = 10; i > 0; i--) {
                    TimestampedTrafficDisorderDatex2 d2 =
                        datex2DataService.unMarshallDatex2Message(datex2, ZonedDateTime.now());
                    if ( d2 == null ) {
                        throw new RuntimeException("Datex2 response can't be null!");
                    }
                    log.info(name + "{}: {}", name, i);
                }
                success = true;
            } finally {
                running = false;
                log.info("{} exiting with success status {}", name, success);
            }
        }

        public boolean isRunning() {
            return running;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}
