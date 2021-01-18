package fi.livi.digitraffic.tie.service.v3.datex2;

import static fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsXmlVersion;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.model.v1.datex2.SituationType;
import fi.livi.digitraffic.tie.service.AbstractDatex2DataServiceTest;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper;
import fi.livi.digitraffic.tie.service.TrafficMessageTestHelper.ImsJsonVersion;

public class V3Datex2DataInternalTest extends AbstractDatex2DataServiceTest {
    private static final Logger log = getLogger(V3Datex2DataInternalTest.class);

    @Autowired
    private V3Datex2DataService v3Datex2DataService;

    @Autowired
    private TrafficMessageTestHelper trafficMessageTestHelper;

    @Ignore("Just for internal testing")
    @Rollback(value = false)
    @Test
    public void findActiveTrafficMessagesDatex2AndJsonEqualsForEveryVersionOfImsAndJson() throws IOException {
        IntStream.range(0,100).forEach(i -> {
            for (final ImsXmlVersion imsXmlVersion : ImsXmlVersion.values()) {
                for (final ImsJsonVersion imsJsonVersion : ImsJsonVersion.values()) {
                    for (final SituationType situationType : SituationType.values()) {
                        final ZonedDateTime start = DateHelper.getZonedDateTimeNowWithoutMillisAtUtc().minusHours(1);
                        final ZonedDateTime end = start.plusHours(2);
                        try {
                            trafficMessageTestHelper.initDataFromStaticImsResourceConent(imsXmlVersion, situationType, imsJsonVersion, start, end);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
