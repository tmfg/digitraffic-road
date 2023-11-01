package fi.livi.digitraffic.tie.helper;

import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.TestUtils;

public class ThreadUtilTest extends AbstractTest {

    @Test
    public void delayMs() {
        IntStream.range(0,4).parallel().forEach(i -> {
            final StopWatch duration = StopWatch.createStarted();
            final int delayMs = TestUtils.getRandom(500, 1500);
            ThreadUtils.delayMs(delayMs);
            AssertHelper.assertGe(duration.getTime(), delayMs, 200);
        });
    }
}
