package fi.livi.digitraffic.tie.conf.jms;

public class JMSInitException extends RuntimeException {

    public JMSInitException(String message, Exception e) {
        super(message, e);
    }

    public JMSInitException(String message) {
        super(message);
    }
}
