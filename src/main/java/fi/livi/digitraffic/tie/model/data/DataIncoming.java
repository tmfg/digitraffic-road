package fi.livi.digitraffic.tie.model.data;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.DynamicUpdate;

import static fi.livi.digitraffic.tie.model.data.IncomingDataTypes.IMS_122;

@Entity
@DynamicUpdate
public class DataIncoming {
    /**
     * Upper bound for the stored reason. Database errors run to several hundred characters and
     * parse errors include a part of the message itself, so the limit is only here to stop an
     * unbounded error text from being stored.
     */
    private static final int FAILURE_REASON_MAX_LENGTH = 4000;

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

    /// Why processing failed. Null while the status is NEW or PROCESSED.
    private String failureReason;

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

    /**
     * Marks the message as failed and records why, so the row can be found and understood
     * without digging through application logs.
     *
     * @param failureReason human readable reason, truncated to fit the column
     */
    public void markFailed(final String failureReason) {
        this.status = IncomingDataTypes.DataStatus.FAILED;
        this.failureReason = StringUtils.abbreviate(failureReason, FAILURE_REASON_MAX_LENGTH);
    }

    /**
     * Marks the message as handled. Any reason left over from an earlier failed attempt is
     * cleared, so a processed row never carries a stale explanation.
     */
    public void markProcessed() {
        this.status = IncomingDataTypes.DataStatus.PROCESSED;
        this.failureReason = null;
    }

    public IncomingDataTypes.DataStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getMessageId() {
        return messageId;
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
