package fi.livi.digitraffic.tie.metadata.model;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "roadWeatherSensor", attributeNodes = @NamedAttributeNode("roadWeatherStation"))
public class RoadWeatherSensor {

    @Id
    @SequenceGenerator(name = "RW_SENSOR_SEQ", sequenceName = "SEQ_ROAD_WEATHER_SENSOR")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RW_SENSOR_SEQ")
    private long id;

    private long lotjuId;

    private long sensorTypeId;

    private Integer altitude;

    @ManyToOne
    @JoinColumn(name="road_weather_station_id", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadWeatherStation roadWeatherStation;

    private String description;

    private String name;

    private LocalDate obsoleteDate;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public long getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(long sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public Integer getAltitude() {
        return altitude;
    }

    public void setAltitude(Integer altitude) {
        this.altitude = altitude;
    }

    public RoadWeatherStation getRoadWeatherStation() {
        return roadWeatherStation;
    }

    public void setRoadWeatherStation(RoadWeatherStation roadWeatherStation) {
        this.roadWeatherStation = roadWeatherStation;
    }

    public Long getRoadWeatherStationId() {
        return roadWeatherStation != null ? roadWeatherStation.getId() : null;
    }

    public Long getRoadStationNaturalId() {
        return roadWeatherStation != null ? roadWeatherStation.getRoadStationNaturalId() : null;
    }

    public Long getRoadWeatherStationLotjuId() {
        return roadWeatherStation != null ? roadWeatherStation.getLotjuId() : null;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isObsolete() {
        return obsoleteDate != null;
    }

    /**
     *
     * @param obsolete
     * @return true if state is changed
     */
    public boolean setObsolete(boolean obsolete) {
        if ( obsolete ) {
            return obsolete();
        } else if ( isObsolete() ) {
            this.obsoleteDate = null;
            return true;
        }
        return false;
    }

    public boolean obsolete() {
        if (obsoleteDate == null) {
            obsoleteDate = LocalDate.now();
            return true;
        }
        return false;
    }



    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
                .appendField("roadWeatherStationId", getRoadWeatherStationId())
                .appendField("roadStationNaturalId", getRoadStationNaturalId())
                .toString();
    }

}
