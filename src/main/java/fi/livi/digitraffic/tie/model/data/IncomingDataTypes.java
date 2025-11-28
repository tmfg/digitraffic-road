package fi.livi.digitraffic.tie.model.data;

public abstract class IncomingDataTypes {
    public enum DataSource {
        JMS,
        API
    }

    public enum DataType {
        IMS,
        VARIABLE_MESSAGE_SIGN_DATEX_XML
    }

    public enum DataStatus {
        NEW, FAILED, PROCESSED
    }

    public static final String IMS_122 = "1.2.2";
}
