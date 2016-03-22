package fi.livi.digitraffic.tie.service;


public interface ScheduledJob extends Runnable {

    Integer getPriority();
}
