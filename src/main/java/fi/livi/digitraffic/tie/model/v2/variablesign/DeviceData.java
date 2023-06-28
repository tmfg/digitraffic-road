package fi.livi.digitraffic.tie.model.v2.variablesign;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;

@Entity
@Immutable
public class DeviceData extends ReadOnlyCreatedAndModifiedFields {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    private String deviceId; // this is a foreign key

    private String displayValue;
    private String additionalInformation;

    private Instant effectDate;

    private String cause;
    private String reliability;

    @OneToMany(targetEntity = DeviceDataRow.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_data_id", nullable = false)
    @OrderBy("screen,rowNumber")
    private List<DeviceDataRow> rows;

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(final String displayValue) {
        this.displayValue = displayValue;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(final String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public Instant getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(final Instant effectDate) {
        this.effectDate = effectDate;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(final String cause) {
        this.cause = cause;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    public String getReliability() {
        return reliability;
    }

    public void setReliability(final String reliability) {
        this.reliability = reliability;
    }

    public List<DeviceDataRow> getRows() {
        return rows;
    }

    public void setRows(final List<DeviceDataRow> rows) {
        this.rows = rows;
    }
}
