package fi.livi.digitraffic.tie.data.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.TrafficDisordersDatex2Response;

public class Datex2DataServiceTest extends AbstractTest {
    @Autowired
    private Datex2DataService datex2DataService;

    @Autowired
    private Datex2UpdateService datex2UpdateService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

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
        assertTrue(datex2Repository.findAll().isEmpty());

        try {
            datex2DataService.findAllTrafficDisordersBySituationId(situationId1);
            Assert.fail("ObjectNotFoundException should be raised");
        } catch (final ObjectNotFoundException onfe) {
            // OK
        }

        updateDatex2Data(datex2Content1);
        findDatex2AndAssert(situationId1, true);
        findDatex2AndAssert(situationId2, false);
        updateDatex2Data(datex2Content2);

        assertCollectionSize(2, datex2Repository.findAll());

        findDatex2AndAssert(situationId1, true);
        findDatex2AndAssert(situationId2, true);

        TrafficDisordersDatex2Response allActive = datex2DataService.findActiveTrafficDisorders();
        assertCollectionSize(1, allActive.getDisorder());
        SituationPublication active = getSituationPublication(allActive);
        assertCollectionSize(1, active.getSituation());
        assertTrue(active.getSituation().get(0).getId().equals(situationId2));
    }

    private TrafficDisordersDatex2Response findDatex2AndAssert(final String situationId, final boolean found) {
        try {
            final TrafficDisordersDatex2Response response = datex2DataService.findAllTrafficDisordersBySituationId(situationId);
            assertTrue(found);

            final SituationPublication s = getSituationPublication(response);
            assertEquals(situationId, s.getSituation().get(0).getId());
            return response;
        } catch (final ObjectNotFoundException onfe) {
            // OK
            if (found) {
                Assert.fail("Situation " + situationId + " should have found");
            }
            return null;
        }
    }

    private static SituationPublication getSituationPublication(final TrafficDisordersDatex2Response response) {
        assertTrue(response.getDisorder().size() == 1);
        return ((SituationPublication) response.getDisorder().get(0).getD2LogicalModel().getPayloadPublication());
    }

    private void updateDatex2Data(final String datex2Content) {
        Object object = jaxb2Marshaller.unmarshal(new StringSource(datex2Content));
        if (object instanceof JAXBElement) {
            object = ((JAXBElement) object).getValue();
        }
        final D2LogicalModel d2LogicalModel = (D2LogicalModel)object;
        datex2UpdateService.updateTrafficAlerts(Collections.singletonList(new Datex2MessageDto(datex2Content, null, d2LogicalModel)));
    }
}
