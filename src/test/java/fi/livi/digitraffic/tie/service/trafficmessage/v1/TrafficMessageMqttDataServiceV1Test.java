package fi.livi.digitraffic.tie.service.trafficmessage.v1;

import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.ROAD_WORK;
import static fi.livi.digitraffic.tie.dto.trafficmessage.v1.SituationType.TRAFFIC_ANNOUNCEMENT;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.TrafficAnnouncementFeature;
import fi.livi.digitraffic.common.util.TimeUtil;
import fi.livi.digitraffic.tie.model.trafficmessage.datex2.Datex2;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;

public class TrafficMessageMqttDataServiceV1Test extends AbstractDaemonTest {

    @Autowired
    private TrafficMessageMqttDataServiceV1 trafficMessageMqttDataServiceV1;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    private Instant lastUpdated;

    @BeforeEach
    public void initDb() throws IOException {
        trafficMessageTestHelper.cleanDb();
        final ZonedDateTime start = TimeUtil.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
        final ZonedDateTime end = start.plusHours(2);
        lastUpdated = TimeUtil.roundInstantSeconds(getTransactionTimestamp());
        trafficMessageTestHelper.initDataFromStaticImsResourceContent(
            TrafficMessageTestHelper.ImsXmlVersion.getLatestVersion(),
            TRAFFIC_ANNOUNCEMENT.name(),
            TrafficMessageTestHelper.ImsJsonVersion.getLatestVersion(),
            start, end);
        trafficMessageTestHelper.initDataFromStaticImsResourceContent(
            TrafficMessageTestHelper.ImsXmlVersion.getLatestVersion(),
            ROAD_WORK.name(),
            TrafficMessageTestHelper.ImsJsonVersion.getLatestVersion(),
            start, end);
    }

    @AfterEach
    public void cleanDb() {
        trafficMessageTestHelper.cleanDb();
    }

    @Test
    public void findSimpleTrafficMessagesForMqttCreatedAfter() {
        final Pair<Instant, List<TrafficAnnouncementFeature>> result1 =
            trafficMessageMqttDataServiceV1.findSimpleTrafficMessagesForMqttCreatedAfter(lastUpdated.minusSeconds(1));
        Assertions.assertEquals(2, result1.getRight().size());

        final Pair<Instant, List<TrafficAnnouncementFeature>> result2 =
            trafficMessageMqttDataServiceV1.findSimpleTrafficMessagesForMqttCreatedAfter(result1.getLeft());
        Assertions.assertEquals(0, result2.getRight().size());
        Assertions.assertNull(result2.getLeft());
    }

    @Test
    public void findDatex2TrafficMessagesForMqttCreatedAfter() {
        final Pair<Instant, List<Datex2>> result1 =
            trafficMessageMqttDataServiceV1.findDatex2TrafficMessagesForMqttCreatedAfter(lastUpdated.minusSeconds(1));
        Assertions.assertEquals(2, result1.getRight().size());

        final Pair<Instant, List<Datex2>> result2 =
            trafficMessageMqttDataServiceV1.findDatex2TrafficMessagesForMqttCreatedAfter(result1.getLeft());
        Assertions.assertEquals(0, result2.getRight().size());
        Assertions.assertNull(result2.getLeft());
    }
}
