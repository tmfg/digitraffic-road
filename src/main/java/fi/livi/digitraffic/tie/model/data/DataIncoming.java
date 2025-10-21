package fi.livi.digitraffic.tie.model.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
public class DataIncoming {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    private String messageId;

    private String source;

    private String version;

    private String type;

    private String data;

    private String status;

    public DataIncoming(final String messageId, final String version, final String type, final String data) {
        this.messageId = messageId;
        this.version = version;
        this.type = type;
        this.data = data;
        this.source = "JMS";
        this.status = "NEW";
    }

    protected DataIncoming() {
    }

    public void setFailed() {
        this.status = "FAILED";
    }

    public void setProcessed() {
        this.status = "PROCESSED";
    }

    public String getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
