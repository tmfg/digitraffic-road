package fi.livi.digitraffic.tie.conf.kca.artemis.jms.message;

/**
 * Interface that V122 ImsMessage implements
 *
 * @see fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.ImsMessage
 */
public interface ExternalIMSMessage {
    ExternalImsMessageContent getMessageContent();
    long getMessageId();
}
