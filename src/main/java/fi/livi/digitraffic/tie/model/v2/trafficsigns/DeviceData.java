package fi.livi.digitraffic.tie.model.v2.trafficsigns;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.ZonedDateTime;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
public class DeviceData {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private long id;

    private ZonedDateTime createdDate;

    private String deviceId; // this is a foreign key

    private String displayValue;
    private String additionalInformation;

    private ZonedDateTime effectDate;

    private String cause;
    private String reliability;

    @OneToMany(targetEntity = DeviceDataRow.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "device_data_id", nullable = false)
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

    public ZonedDateTime getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(final ZonedDateTime effectDate) {
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

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final ZonedDateTime createdDate) {
        this.createdDate = createdDate;
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

    public void setRows(List<DeviceDataRow> rows) {
        this.rows = rows;
    }
}
