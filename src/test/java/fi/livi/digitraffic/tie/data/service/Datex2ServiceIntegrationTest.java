package fi.livi.digitraffic.tie.data.service;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.AbstractMetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

public class Datex2ServiceIntegrationTest extends AbstractMetadataIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(Datex2ServiceIntegrationTest.class);

    @Autowired
    Datex2DataService datex2DataService;

    @Autowired
    Datex2Repository datex2Repository;

    private Unmarshaller jaxbUnmarshaller;

    private static final String situationId1 = "GUID50005166";
    private static final String situationId2 = "GUID50006936";
    private String datex2Content1;
    private String datex2Content2;

    @Before
    public void init() throws JAXBException, IOException {
        jaxbUnmarshaller = JAXBContext.newInstance(D2LogicalModel.class).createUnmarshaller();
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
    public void testHandleUnhandledDatex2() throws JAXBException, IOException, InterruptedException {

        datex2Repository.deleteAll();
        Assert.assertTrue(datex2Repository.findAll().isEmpty());

        saveDatex2(datex2Content1);
        Thread.sleep(50); // delay 2nd save a bit
        saveDatex2(datex2Content2);

        Assert.assertTrue(datex2Repository.findAll().size() == 2);
        findDatex2AndAssert(situationId1, false);
        findDatex2AndAssert(situationId2, false);

        datex2DataService.handleUnhandledDatex2Messages();

        findDatex2AndAssert(situationId1, true);
        findDatex2AndAssert(situationId2, true);
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
        Object object = jaxbUnmarshaller.unmarshal(new StringReader(datex2Content));
        if (object instanceof JAXBElement) {
            object = ((JAXBElement) object).getValue();
        }
        D2LogicalModel d2LogicalModel = (D2LogicalModel)object;
        Pair<D2LogicalModel, String> pair = Pair.of(d2LogicalModel, datex2Content);
        datex2DataService.updateDatex2Data(Collections.singletonList(pair));
    }

}
