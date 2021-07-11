package fi.livi.digitraffic.tie.scheduler;

import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.TestPropertySource;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutLocalStack;

@TestPropertySource(properties = { "dt.scheduled.annotation.enabled=true" })
public class ScheduledAnnotationThreadTest extends AbstractDaemonTestWithoutLocalStack {

    private static final Logger log = getLogger(ScheduledAnnotationThreadTest.class);

    @Value("${dt.scheduled.pool.size}")
    int poolSize;

    private final int job1StartErrorsAt = 10;
    private final int job2StartErrorsAt = 20;
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
        Assert.assertTrue(count1 > job1StartErrorsAt + poolSize);
        Assert.assertTrue(count2 > job2StartErrorsAt + poolSize);
    }

    @Scheduled(fixedRate = 10)
    public void scheduledJob1() {
        count1++;
        // Ensure errors of pool size
        if (count1 >= job1StartErrorsAt && count1 <= job1StartErrorsAt +poolSize) {
            scheduledJob1Error = true;
            throw new RuntimeException("scheduledJob1 expected error at run " + count1);
        }
        log.warn("scheduledJob1: {}", count1);
    }

    @Scheduled(fixedRate = 10)
    public void scheduledJob2() {
        count2++;
        // Ensure errors of pool size
        if (count1 >= job2StartErrorsAt && count1 <= job2StartErrorsAt + poolSize) {
            scheduledJob2Error = true;
            assertTrue("scheduledJob2 expected error at run " + count2, false);
        }
        log.warn("scheduledJob2: {}", count2);
    }
}
