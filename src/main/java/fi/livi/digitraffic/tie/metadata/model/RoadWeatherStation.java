package fi.livi.digitraffic.tie.metadata.model;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import fi.livi.digitraffic.tie.metadata.converter.RoadWeatherStationTypeConverter;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@DynamicUpdate
@NamedEntityGraph(name = "roadWeatherStation", attributeNodes = @NamedAttributeNode("roadStation"))
public class RoadWeatherStation implements Stringifiable {

    @Id
    @SequenceGenerator(name = "RWS_SEQ", sequenceName = "SEQ_ROAD_WEATHER_STATION")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RWS_SEQ")
    private long id;

    private Long lotjuId;

    @Convert(converter = RoadWeatherStationTypeConverter.class)
    private RoadWeatherStationType roadWeatherStationType;

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

    public RoadWeatherStationType getRoadWeatherStationType() {
        return roadWeatherStationType;
    }

    public void setRoadWeatherStationType(final RoadWeatherStationType roadWeatherStationType) {
        this.roadWeatherStationType = roadWeatherStationType;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("id", getId())
                .appendField("lotjuId", this.getLotjuId())
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