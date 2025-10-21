package fi.livi.digitraffic.tie.conf.kca.artemis.jms.message;

import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

/**
 * Interface that IMS V1.2.2 implements
 *
 * @see fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.ImsMessage
 */
public interface ExternalMessage {
    MessageTypeEnum getType();
    String getVersion();
    String getContent();
}

