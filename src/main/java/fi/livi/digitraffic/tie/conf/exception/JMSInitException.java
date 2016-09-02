package fi.livi.digitraffic.tie.conf.exception;

public class JMSInitException extends RuntimeException {

    public JMSInitException(String message, Exception e) {
        super(message, e);
    }

    public JMSInitException(String message) {
        super(message);
    }
}
