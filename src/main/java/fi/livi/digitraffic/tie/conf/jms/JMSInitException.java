package fi.livi.digitraffic.tie.conf.jms;

public class JMSInitException extends RuntimeException {

    public JMSInitException(final String message, final Exception e) {
        super(message, e);
    }

    public JMSInitException(final String message) {
        super(message);
    }
}
