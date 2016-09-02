package fi.livi.digitraffic.tie.metadata.model;

import javax.persistence.Convert;
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

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.converter.WeatherStationTypeConverter;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "weatherStation", attributeNodes = @NamedAttributeNode("roadStation"))
public class WeatherStation {

    @Id
    @GenericGenerator(name = "SEQ_WEATHER_STATION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_ROAD_WEATHER_STATION"))
    @GeneratedValue(generator = "SEQ_WEATHER_STATION")
    private long id;

    private Long lotjuId;

    @Convert(converter = WeatherStationTypeConverter.class)
    private WeatherStationType weatherStationType;

    private boolean master;

    @OneToOne
    @JoinColumn(name="road_station_id", nullable = false)
    @Fetch(FetchMode.JOIN)
    private RoadStation roadStation;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Long getLotjuId() {
        return lotjuId;
    }

    public void setLotjuId(final Long lotjuId) {
        this.lotjuId = lotjuId;
    }

    public RoadStation getRoadStation() {
        return roadStation;
    }

    public void setRoadStation(final RoadStation roadStation) {
        this.roadStation = roadStation;
    }

    public boolean obsolete() {
        return roadStation.obsolete();
    }

    public WeatherStationType getWeatherStationType() {
        return weatherStationType;
    }

    public void setWeatherStationType(final WeatherStationType weatherStationType) {
        this.weatherStationType = weatherStationType;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(final boolean master) {
        this.master = master;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("lotjuId", getLotjuId())
                .appendField("roadStationId", getRoadStationId())
                .appendField("roadStationNaturalId", getRoadStationNaturalId())
                .toString();
    }

    public Long getRoadStationId() {
        return roadStation != null ? roadStation.getId() : null;
    }

    public Long getRoadStationNaturalId() {
        return roadStation != null ? roadStation.getNaturalId() : null;
    }

}
