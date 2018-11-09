package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;

import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@DisallowConcurrentExecution
@ConditionalOnNotWebApplication
public abstract class AbstractUpdateJob implements Job {

    @Autowired
    protected DataStatusService dataStatusService;

}
