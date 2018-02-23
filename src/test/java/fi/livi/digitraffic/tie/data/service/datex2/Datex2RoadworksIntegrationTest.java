package fi.livi.digitraffic.tie.data.service.datex2;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;
import fi.livi.digitraffic.tie.data.service.Datex2UpdateService;

public class Datex2RoadworksIntegrationTest extends AbstractTest {
    @Autowired
    private Datex2SimpleMessageUpdater messageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    @Autowired
    private Datex2UpdateService datex2UpdateService;

    @Autowired
    private StringToObjectMarshaller stringToObjectMarshaller;

    @MockBean
    private Datex2RoadworksHttpClient datex2RoadworksHttpClient;

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE_TIME)
        .optionalStart()
        .appendOffsetId()
        .optionalStart()
        .toFormatter();

    @Before
    public void before() {
        messageUpdater = new Datex2SimpleMessageUpdater(null, datex2RoadworksHttpClient, datex2UpdateService, stringToObjectMarshaller);
        datex2Repository.deleteAll();
    }

    private void assertCountAndVersionTime(final int count, final ZonedDateTime versionTime) {
        final List<Datex2> roadWorks = datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name());

        assertCollectionSize(count, roadWorks);
        assertVersionTime(roadWorks.get(0), versionTime);
    }

    private static void assertVersionTime(final Datex2 datex2, final ZonedDateTime versionTime) {
        assertEquals(versionTime.withNano(0), datex2.getSituations().get(0).getSituationRecords().get(0).getVersionTime());
    }

    private String getRoadworks(final ZonedDateTime versionTime) throws IOException {
        final String xml = readResourceContent("classpath:roadworks/roadworks_GUID50013753.xml");
        final ZonedDateTime endTime = ZonedDateTime.now().plusDays(1);

        return xml
            .replace("%ENDTIME%", endTime.format(FORMATTER))
            .replace("%VERSIONTIME%", versionTime.format(FORMATTER));
    }

    @Test
    @Ignore
    public void updateMessagesWithRealData() {
        //assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()));

        messageUpdater.updateDatex2RoadworksMessages();

        Assert.assertTrue(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()).size() > 1);
    }

    @Test
    @Rollback
    public void updateWithChangedTimestamp() throws IOException, InterruptedException {
        assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()));

        ZonedDateTime versionTime = ZonedDateTime.now();
        when(datex2RoadworksHttpClient.getRoadWorksMessage()).thenReturn(getRoadworks(versionTime));
        messageUpdater.updateDatex2RoadworksMessages();
        assertCountAndVersionTime(1, versionTime);

        Thread.sleep(1000);

        // this one has newer situationRecordVersion time on one of the situations

        versionTime = versionTime.plusHours(1);
        when(datex2RoadworksHttpClient.getRoadWorksMessage()).thenReturn(getRoadworks(versionTime));
        messageUpdater.updateDatex2RoadworksMessages();
        assertCountAndVersionTime(1, versionTime);
    }
}
