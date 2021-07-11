package fi.livi.digitraffic.tie.scheduler;

import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.TestPropertySource;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutLocalStack;

@TestPropertySource(properties = { "dt.scheduled.annotation.enabled=true" })
public class ScheduledAnnotationThreadTest extends AbstractDaemonTestWithoutLocalStack {

    private static final Logger log = getLogger(ScheduledAnnotationThreadTest.class);

    private int count1 = 0;
    private int count2 = 0;
    private boolean scheduledJob1Error = false;
    private boolean scheduledJob2Error = false;

    @Test
    public void checkScheduledJobsRunEvenAfterError() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Assert that scheduledServices has been running even when there has been errors
        Assert.assertTrue(scheduledJob1Error);
        Assert.assertTrue(scheduledJob2Error);
        Assert.assertTrue(count1 > 10);
        Assert.assertTrue(count2 > 20);
    }

    @Scheduled(fixedRate = 10)
    public void scheduledJob1() {
        count1++;
        // Ensure errors of pool size
        if (count1 >= 10 && count1 <= 15) {
            scheduledJob1Error = true;
            throw new RuntimeException("scheduledJob1 expected error at run " + count1);
        }
        log.warn("scheduledJob1: {}", count1);
    }

    @Scheduled(fixedRate = 10)
    public void scheduledJob2() {
        count2++;
        // Ensure errors of pool size
        if (count1 >= 15 && count1 <= 20) {
            scheduledJob2Error = true;
            assertTrue(false);
        }
        log.warn("scheduledJob2: {}", count2);
    }
}
