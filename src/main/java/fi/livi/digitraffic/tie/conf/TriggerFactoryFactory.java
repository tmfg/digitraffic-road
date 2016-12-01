package fi.livi.digitraffic.tie.conf;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

public final class TriggerFactoryFactory {
    private TriggerFactoryFactory() {}

    public static SimpleTriggerFactoryBean createRepeatingTrigger(final JobDetail jobDetail, final long pollFrequencyMs) {
        final SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        // Delay first execution 5 seconds
        factoryBean.setStartDelay(5000L);
        factoryBean.setRepeatInterval(pollFrequencyMs);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        // In case of misfire: The first misfired execution is run immediately, remaining are discarded.
        // Next execution happens after desired interval. Effectively the first execution time is moved to current time.
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT);
        return factoryBean;
    }
}
