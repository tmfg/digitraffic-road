package fi.livi.digitraffic.tie.conf.kca.artemis.jms.message;

import java.util.List;

public interface ExternalImsMessageContent {
    List<? extends ExternalMessage> getMessages();
}
