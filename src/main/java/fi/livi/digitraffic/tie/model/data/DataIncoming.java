package fi.livi.digitraffic.tie.model.data;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

import org.hibernate.annotations.DynamicUpdate;

import static fi.livi.digitraffic.tie.model.data.IncomingDataTypes.IMS_122;

@Entity
@DynamicUpdate
public class DataIncoming {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    private String messageId;

    @Enumerated(EnumType.STRING)
    private IncomingDataTypes.DataSource source;

    /// version of incoming data, for IMS it's 1.2.2
    private String version;

    @Enumerated(EnumType.STRING)
    private IncomingDataTypes.DataType type;

    private String data;

    @Enumerated(EnumType.STRING)
    private IncomingDataTypes.DataStatus status;

    protected DataIncoming(final String messageId, final String version, final IncomingDataTypes.DataType type, final String data) {
        this.messageId = messageId;
        this.version = version;
        this.type = type;
        this.data = data;
        this.source = IncomingDataTypes.DataSource.JMS;
        this.status = IncomingDataTypes.DataStatus.NEW;
    }

    public static DataIncoming ims122(final String messageId, final String data) {
        return new DataIncoming(messageId, IMS_122, IncomingDataTypes.DataType.IMS, data);
    }

    protected DataIncoming() {
    }

    public void setFailed() {
        this.status = IncomingDataTypes.DataStatus.FAILED;
    }

    public void setProcessed() {
        this.status = IncomingDataTypes.DataStatus.PROCESSED;
    }

    public IncomingDataTypes.DataStatus getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public IncomingDataTypes.DataType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public Long getDataId() {
        return dataId;
    }
}
