package fi.livi.digitraffic.tie.metadata.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@DisallowConcurrentExecution
public abstract class AbstractUpdateJob implements Job {

    @Autowired
    protected StaticDataStatusService staticDataStatusService;

}
