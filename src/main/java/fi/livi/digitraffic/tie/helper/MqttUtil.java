package fi.livi.digitraffic.tie.helper;

public abstract class MqttUtil {

    public static String getTopicForMessage(final String topicString, final Object...topicParams) {
        return String.format(topicString, topicParams);
    }
}
