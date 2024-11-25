package fi.livi.digitraffic.tie.conf.kca.artemis.jms.message;

/**
 * Interface that different versions of ImsMessages content should implement.
 *
 * @see fi.livi.digitraffic.tie.external.tloik.ims.v1_2_0.ImsMessage.MessageContent
 * @see fi.livi.digitraffic.tie.external.tloik.ims.v1_2_1.ImsMessage.MessageContent
 */
public interface ExternalImsMessageContent {
    String getD2Message();
    String getJMessage();
}
