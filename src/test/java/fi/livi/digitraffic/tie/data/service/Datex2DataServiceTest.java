package fi.livi.digitraffic.tie.data.service;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.service.datex2.Datex2MessageDto;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.D2LogicalModel;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.SituationPublication;
import fi.livi.digitraffic.tie.lotju.xsd.datex2.response.TrafficDisordersDatex2Response;

@Import({Datex2DataService.class, Datex2UpdateService.class})
public class Datex2DataServiceTest extends AbstractServiceTest {
    @Autowired
    private Datex2DataService datex2DataService;

    @Autowired
    private Datex2UpdateService datex2UpdateService;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    private static Instant versionTime = Instant.now();

    private String disorder1;
    private String disorder2;
    private String disorder3;
    private static final String DISORDER1_GUID = "GUID50005166";
    private static final String DISORDER2_GUID = "GUID50006936";
    private static final String DISORDER3_GUID = "GUID50013339";

    private String roadwork1;
    private static final String ROADWORK1_GUID = "GUID50350441";

    private String weightRestriction1;
    private static final String WR1_GUID = "GUID50354262";

    private static final String NOT_FOUND_GUID = "NOT_FOUND";
    private static final String VERSION_TIME_REXP = "<situationRecordVersionTime>\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5][0-9]\\.\\d[0-9]\\d([+-]\\d[0-9]:\\d[09])<\\/situationRecordVersionTime>";

    @Before
    public void init() throws IOException {
        disorder1 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-09-12-20-51-24-602.xml");
        disorder2 = readResourceContent("classpath:lotju/datex2/InfoXML_2016-11-17-18-34-36-299.xml");
        disorder3 = readResourceContent("classpath:lotju/datex2/Datex2_2017-08-10-15-59-34-896.xml");
        roadwork1 = readResourceContent("classpath:lotju/roadwork/roadwork1.xml");
        weightRestriction1 = readResourceContent("classpath:lotju/weight_restrictions/wr1.xml");
    }

    private static Instant getNextVersionTime() {
        versionTime = versionTime.plusSeconds(1);
        return versionTime;
    }

    private void deleteAllDatex2() {
        datex2Repository.deleteAll();
        assertTrue(datex2Repository.findAll().isEmpty());
    }



    @Test(expected = ObjectNotFoundException.class)
    public void getAllTrafficDisordersBySituationIdNotFound() {
        deleteAllDatex2();

        datex2DataService.getAllTrafficDisordersBySituationId(NOT_FOUND_GUID);
    }

    @Test
    public void endedShouldNotFound() {
        deleteAllDatex2();
        updateTrafficAlerts(disorder3);
        // Not ended yet
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, true, 0);

        // Set situation Endtime to 1 min ago
        final String disorder3Ended = addEndTime(disorder3, Instant.now().minus(10, ChronoUnit.MINUTES));

        updateTrafficAlerts(disorder3Ended);

        // Disorder should not be found as active
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, false, 0);
    }

    @Test
    public void findActiveInPast() {
        deleteAllDatex2();
        // Set situation Endtime to 2h 1 min ago
        final String disorder3Ended = addEndTime(disorder3, Instant.now().minus(121, ChronoUnit.MINUTES));
        updateTrafficAlerts(disorder3Ended);

        // Disorder is ended > 2h in past. With parameter value > 3 it should found, but not with < 3
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, false, 2);
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, true, 3);
    }

    @Test
    public void activeAndActiveInPast() {
        deleteAllDatex2();
        updateTrafficAlerts(disorder2);
        updateTrafficAlerts(disorder3);
        // Both active
        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, true, 0);
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, true, 0);

        // After ending disorder3 it  not not be found
        final String disorder3Ended = addEndTime(disorder3, Instant.now().minus(10, ChronoUnit.MINUTES));
        updateTrafficAlerts(disorder3Ended);

        findActiveTrafficAlertsAndAssert(DISORDER2_GUID, true, 0);
        findActiveTrafficAlertsAndAssert(DISORDER3_GUID, false, 0);
    }

    @Test
    public void updateTrafficAlerts() {
        deleteAllDatex2();

        updateTrafficAlerts(disorder1);
        findTrafficAlertsAndAssert(DISORDER1_GUID, true);
        findTrafficAlertsAndAssert(DISORDER2_GUID, false);
        updateTrafficAlerts(disorder2);

        assertCollectionSize(2, datex2Repository.findAll());

        findTrafficAlertsAndAssert(DISORDER1_GUID, true);
        findTrafficAlertsAndAssert(DISORDER2_GUID, true);

        final TrafficDisordersDatex2Response allActive = datex2DataService.findActiveTrafficDisorders(0);
        assertCollectionSize(1, allActive.getDisorders());

        final SituationPublication active = getSituationPublication(allActive);
        assertCollectionSize(1, active.getSituations());
        assertTrue(active.getSituations().get(0).getId().equals(DISORDER2_GUID));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getAllRoadworksBySituationIdNotFound() {
        deleteAllDatex2();

        datex2DataService.getAllRoadworksBySituationId(NOT_FOUND_GUID);
    }

    @Test
    public void findActiveRoadworks() {
        deleteAllDatex2();

        updateRoadworks(roadwork1);

        assertCollectionSize(1, datex2DataService.findActiveRoadworks(0).getRoadworks());

        assertNotNull(datex2DataService.getAllRoadworksBySituationId(ROADWORK1_GUID));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getAllWeightRestrictionsBySituationIdNotFound() {
        deleteAllDatex2();

        datex2DataService.getAllWeightRestrictionsBySituationId(NOT_FOUND_GUID);
    }

    @Test
    public void findActiveWeightRestrictions() {
        deleteAllDatex2();

        updateWeightRestrictions(weightRestriction1);

        assertCollectionSize(1, datex2DataService.findActiveWeightRestrictions(0).getRestrictions());

        assertNotNull(datex2DataService.getAllWeightRestrictionsBySituationId(WR1_GUID));
    }

    private static String addEndTime(final String disorder, final Instant endTime) {
        return StringUtils.replace(disorder,
            "</overallStartTime>",
            "</overallStartTime>\n                        " +
                "<overallEndTime>" + endTime.toString() + "</overallEndTime>");
    }

    private void findActiveTrafficAlertsAndAssert(final String situationId, final boolean found, final int inactiveHours) {
        final TrafficDisordersDatex2Response allActive = datex2DataService.findActiveTrafficDisorders(inactiveHours);
        assertEquals(found,
                            allActive.getDisorders().stream()
                                .filter(d ->
                                    ((SituationPublication) d.getD2LogicalModel().getPayloadPublication()).getSituations().stream().filter(s -> s.getId().equals(situationId)).findFirst().isPresent()
                                ).findFirst().isPresent());
    }

    private TrafficDisordersDatex2Response findTrafficAlertsAndAssert(final String situationId, final boolean found) {
        try {
            final TrafficDisordersDatex2Response response = datex2DataService.getAllTrafficDisordersBySituationId(situationId);
            assertTrue(found);

            final SituationPublication s = getSituationPublication(response);
            assertEquals(situationId, s.getSituations().get(0).getId());
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
        assertTrue(response.getDisorders().size() == 1);
        return ((SituationPublication) response.getDisorders().get(0).getD2LogicalModel().getPayloadPublication());
    }

    private D2LogicalModel createModel(final String datex2Content) {
        final Object object = jaxb2Marshaller.unmarshal(new StringSource(datex2Content));

        if (object instanceof JAXBElement) {
            return (D2LogicalModel) ((JAXBElement) object).getValue();
        }

        return (D2LogicalModel)object;
    }

    private List<Datex2MessageDto> createDtoList(final String datex2Content) {
        final D2LogicalModel d2LogicalModel = createModel(datex2Content);

        return Collections.singletonList(new Datex2MessageDto(datex2Content, null, d2LogicalModel));
    }

    private void updateTrafficAlerts(final String datex2Content) {
        final String updated = replaceVersionTimes(datex2Content, getNextVersionTime());
        datex2UpdateService.updateTrafficAlerts(createDtoList(updated));
    }

    private static String replaceVersionTimes(final String xml, final Instant replacement) {
        return RegExUtils.replacePattern(xml, VERSION_TIME_REXP,
            "<situationRecordVersionTime>" + replacement.toString() + "</situationRecordVersionTime>");
    }

    private void updateRoadworks(final String datex2Content) {
        datex2UpdateService.updateRoadworks(createDtoList(datex2Content));
    }

    private void updateWeightRestrictions(final String datex2Content) {
        datex2UpdateService.updateWeightRestrictions(createDtoList(datex2Content));
    }

}
