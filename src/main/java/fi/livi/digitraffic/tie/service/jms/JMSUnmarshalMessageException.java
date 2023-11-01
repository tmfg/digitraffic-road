package fi.livi.digitraffic.tie.service.jms;

public class JMSUnmarshalMessageException extends RuntimeException {

    public JMSUnmarshalMessageException(final String message, final Exception e) {
        super(message, e);
    }

    public JMSUnmarshalMessageException(final String message) {
        super(message);
    }
}
