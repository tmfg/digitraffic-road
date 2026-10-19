package fi.livi.digitraffic.tie.model.data;

import fi.livi.digitraffic.tie.model.ModifiedAt;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Generated;

import java.time.Instant;

@Entity
@Table(name = "data_datex2_situation_message")
public class DataDatex2SituationMessage implements ModifiedAt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "datex2_id")
    private DataDatex2Situation situation;

    private String messageVersion;

    /**
     * The message type as TLOIK sends it: {@code "DATEX2"}, {@code "SIMPPELI"}, {@code "INFOXML"} or
     * {@code "OTHER"}.
     *
     * <p>Write and query this with
     * {@link fi.livi.digitraffic.tie.external.tloik.ims.v1_2_2.MessageTypeEnum}, which is generated
     * from the IMS schema. A binding in {@code ImsXmlMessage_1_2_2.xjb} names the constants after
     * the schema values, so {@code name()} and {@code value()} always agree.</p>
     */
    private String messageType;
    private String message;

    @Generated
    private Instant modifiedAt;
    @Generated
    private Instant createdAt;

    public DataDatex2SituationMessage(final String messageVersion,
                                      final String messageType,
                                      final String message) {
        this.messageVersion = messageVersion;
        this.messageType = messageType;
        this.message = message;
    }

    public DataDatex2SituationMessage() {
    }

    public Long getMessageId() {
        return messageId;
    }

    public String getMessageVersion() {
        return messageVersion;
    }

    public void setSituation(final DataDatex2Situation situation) {
        this.situation = situation;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public DataDatex2Situation getSituation() {
        return situation;
    }
}
