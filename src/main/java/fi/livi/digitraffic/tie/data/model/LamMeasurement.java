package fi.livi.digitraffic.tie.data.model;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
public class LamMeasurement {
    @Id
    private long lamId;

    private long trafficVolume1;

    private long trafficVolume2;

    private long averageSpeed1;

    private long averageSpeed2;

    private Date measured;

    public long getLamId() {
        return lamId;
    }

    public void setLamId(final long lamId) {
        this.lamId = lamId;
    }

    public long getTrafficVolume1() {
        return trafficVolume1;
    }

    public void setTrafficVolume1(final long trafficVolume1) {
        this.trafficVolume1 = trafficVolume1;
    }

    public long getTrafficVolume2() {
        return trafficVolume2;
    }

    public void setTrafficVolume2(final long trafficVolume2) {
        this.trafficVolume2 = trafficVolume2;
    }

    public long getAverageSpeed1() {
        return averageSpeed1;
    }

    public void setAverageSpeed1(final long averageSpeed1) {
        this.averageSpeed1 = averageSpeed1;
    }

    public long getAverageSpeed2() {
        return averageSpeed2;
    }

    public void setAverageSpeed2(final long averageSpeed2) {
        this.averageSpeed2 = averageSpeed2;
    }

    public Date getMeasured() {
        return measured;
    }

    public void setMeasured(final Date measured) {
        this.measured = measured;
    }
}
