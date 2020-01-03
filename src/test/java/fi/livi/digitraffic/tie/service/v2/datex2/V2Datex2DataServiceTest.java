package fi.livi.digitraffic.tie.service.v2.datex2;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.annotation.Rollback;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.conf.jaxb2.XmlMarshallerConfiguration;
import fi.livi.digitraffic.tie.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.datex2.Situation;
import fi.livi.digitraffic.tie.datex2.SituationPublication;
import fi.livi.digitraffic.tie.external.tloik.ims.ImsMessage;
import fi.livi.digitraffic.tie.external.tloik.ims.jmessage.JsonMessage;
import fi.livi.digitraffic.tie.helper.AssertHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.Datex2MessageType;

@Import({ V2Datex2DataService.class, V2Datex2UpdateService.class, XmlMarshallerConfiguration.class, JacksonAutoConfiguration.class })
public class V2Datex2DataServiceTest extends AbstractServiceTest {

    private final static String GUID = "GUID50001238";

    @Autowired
    private V2Datex2DataService v2Datex2DataService;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private V2Datex2UpdateService v2Datex2UpdateService;

    @Test
    public void activeIncidentsDatex2AndJsonEquals() throws IOException {
        // One active
        initData();

        final D2LogicalModel d2 = v2Datex2DataService.findActive(0, Datex2MessageType.TRAFFIC_INCIDENT);
        final List<Situation> activeSituations = ((SituationPublication) d2.getPayloadPublication()).getSituations();
        final List<JsonMessage> activeJsons = v2Datex2DataService.findActiveJson(0, Datex2MessageType.TRAFFIC_INCIDENT);

        AssertHelper.assertCollectionSize(1, activeSituations);
        AssertHelper.assertCollectionSize(1, activeJsons);
        final Situation situation = activeSituations.get(0);
        final JsonMessage situationJson = activeJsons.get(0);

        Assert.assertEquals(GUID, situation.getId());
        Assert.assertEquals(GUID, situationJson.getSituationId());

        final Instant start = ZonedDateTime.parse("2019-12-13T14:43:18.388+02:00").toInstant();

        final Instant situationStart = situation.getSituationRecords().get(0).getValidity().getValidityTimeSpecification().getOverallStartTime();
        final ZonedDateTime situationJsonStart = situationJson.getReleaseTime();
        Assert.assertEquals(start, situationStart);
        Assert.assertEquals(start, situationJsonStart.toInstant());

        final String commentXml = situation.getSituationRecords().get(0).getGeneralPublicComments().get(0).getComment().getValues().getValues().stream()
            .filter(c -> c.getLang().equals("fi")).findFirst().orElseThrow().getValue();
        final String descJson = situationJson.getAnnouncements().get(0).getLocation().getDescription();
        final String titleJson = situationJson.getAnnouncements().get(0).getTitle();

        Assert.assertTrue(commentXml.contains(titleJson.trim()));
        Assert.assertTrue(commentXml.contains(descJson.trim()));
    }

    @Rollback(false)
    @Test
    public void findBySituationId() throws IOException {
        // One active
        initData();


        final D2LogicalModel d2 = v2Datex2DataService.findAllBySituationId(GUID, Datex2MessageType.TRAFFIC_INCIDENT);
        final List<JsonMessage> jsons = v2Datex2DataService.findAllBySituationIdJson(GUID, Datex2MessageType.TRAFFIC_INCIDENT);

        final List<Situation> situations = ((SituationPublication) d2.getPayloadPublication()).getSituations();

        AssertHelper.assertCollectionSize(1, situations);
        AssertHelper.assertCollectionSize(1, jsons);
        final Situation situation = situations.get(0);
        final JsonMessage situationJson = jsons.get(0);

        Assert.assertEquals(GUID, situation.getId());
        Assert.assertEquals(GUID, situationJson.getSituationId());
    }

    private void initData() throws IOException {
        final ArrayList<String> xmlImsMessages = readResourceContents("classpath:tloik/ims/TrafficIncidentImsMessage.xml");
        final ArrayList<ImsMessage> imsMessages = new ArrayList<>();
        for (String xmlMessage : xmlImsMessages) {
            final ImsMessage ims = (ImsMessage) jaxb2Marshaller.unmarshal(new StringSource(xmlMessage));
            imsMessages.add(ims);
        }
        v2Datex2UpdateService.updateTrafficIncidentImsMessages(imsMessages);
    }
}
