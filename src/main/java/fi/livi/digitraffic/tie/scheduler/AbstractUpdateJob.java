package fi.livi.digitraffic.tie.scheduler;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;

import fi.livi.digitraffic.tie.service.DataStatusService;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@DisallowConcurrentExecution
@ConditionalOnNotWebApplication
public abstract class AbstractUpdateJob implements Job {

    // AutowiringSpringBeanJobFactory takes care of autowiring
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    protected DataStatusService dataStatusService;

}
