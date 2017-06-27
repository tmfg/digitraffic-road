package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@DisallowConcurrentExecution
public abstract class AbstractUpdateJob implements Job {

    @Autowired
    protected DataStatusService dataStatusService;

}
