package fi.livi.digitraffic.tie.model.roadstation;

import java.time.ZonedDateTime;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;

@Entity
@DynamicUpdate
public class SensorValue extends ReadOnlyCreatedAndModifiedFields {

    @Id
    @SequenceGenerator(name = "SEQ_SENSOR_VALUE", sequenceName = "SEQ_SENSOR_VALUE", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_SENSOR_VALUE")
    private Long id;

    private Double value;

    @Column(name = "MEASURED")
    private ZonedDateTime sensorValueMeasured;

    @OneToOne
    @JoinColumn(name="ROAD_STATION_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStation roadStation;

    @OneToOne
    @JoinColumn(name="ROAD_STATION_SENSOR_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStationSensor roadStationSensor;

    @Column(name = "TIME_WINDOW_START")
    private ZonedDateTime timeWindowStart;

    @Column(name = "TIME_WINDOW_END")
    private ZonedDateTime timeWindowEnd;

    @Enumerated(EnumType.STRING)
    private SensorValueReliability reliability;

    /**
     * Default constructor fo Hibernate
     */
    public SensorValue() {
    }

    public SensorValue(final RoadStation roadStation, final RoadStationSensor roadStationSensor, final double value,
                       final ZonedDateTime sensorValueMeasured, final SensorValueReliability reliability) {
        this.roadStation = roadStation;
        this.roadStationSensor = roadStationSensor;
        this.value = value;
        this.sensorValueMeasured = sensorValueMeasured;
        this.reliability = reliability;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(final Double value) {
        this.value = value;
    }

    public ZonedDateTime getSensorValueMeasured() {
        return sensorValueMeasured;
    }

    public void setSensorValueMeasured(final ZonedDateTime sensorValueMeasured) {
        this.sensorValueMeasured = sensorValueMeasured;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    public RoadStationSensor getRoadStationSensor() {
        return roadStationSensor;
    }

    public void setRoadStationSensor(final RoadStationSensor roadStationSensor) {
        this.roadStationSensor = roadStationSensor;
    }

    public ZonedDateTime getTimeWindowStart() {
        return timeWindowStart;
    }

    public ZonedDateTime getTimeWindowEnd() {
        return timeWindowEnd;
    }

    public void setReliability(final SensorValueReliability reliability) {
        this.reliability = reliability;
    }

    public SensorValueReliability getReliability() {
        return reliability;
    }

    @Override
    public String toString() {
        return new ToStringHelper(this)
                .appendField("id", getId())
                .appendField("value", this.getValue())
                .appendField("measured", getSensorValueMeasured())
                .appendField("sensor", getRoadStationSensor())
                .appendField("reliability", getReliability())
                .toString();
    }
}
