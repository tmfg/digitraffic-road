package fi.livi.digitraffic.tie.conf.jms;

/**
 * Interface that different versions of ImsMessages should implement.
 *
 * @see fi.livi.digitraffic.tie.external.tloik.ims.v1_2_0.ImsMessage
 * @see fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1.ImsMessage
 */
public interface ExternalIMSMessage {
    ExternalImsMessageContent getMessageContent();
}