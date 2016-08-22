package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "sensorValue", attributeNodes = @NamedAttributeNode("roadStation"))
public class SensorValue {

    @Id
    @GenericGenerator(name = "SEQ_SENSOR_VALUE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_SENSOR_VALUE"))
    @GeneratedValue(generator = "SEQ_SENSOR_VALUE")
    private long id;

    private Double value;

    @Column(name = "MEASURED")
    private LocalDateTime sensorValueMeasured;

    @OneToOne
    @JoinColumn(name="ROAD_STATION_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStation roadStation;

    @OneToOne
    @JoinColumn(name="ROAD_STATION_SENSOR_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStationSensor roadStationSensor;

    public SensorValue() {
        // default;
    }

    public SensorValue(RoadStation roadStation, RoadStationSensor roadStationSensor, double value, LocalDateTime sensorValueMeasured) {
        this.roadStation = roadStation;
        this.roadStationSensor = roadStationSensor;
        this.value = value;
        this.sensorValueMeasured = sensorValueMeasured;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(final Double value) {
        this.value = value;
    }

    public LocalDateTime getSensorValueMeasured() {
        return sensorValueMeasured;
    }

    public void setSensorValueMeasured(final LocalDateTime sensorValueMeasured) {
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
}
