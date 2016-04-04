package fi.livi.digitraffic.tie.metadata.service;


public interface ScheduledJob extends Runnable {

    Integer getPriority();
}
