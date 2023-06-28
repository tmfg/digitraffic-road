package fi.livi.digitraffic.tie.model.v1;

import java.io.Serializable;

import jakarta.persistence.*;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.model.RoadStationType;

@Entity
@Immutable
public class AllowedRoadStationSensor implements Serializable {

    @Id
    @SequenceGenerator(name = "SEQ_ALLOWED_SENSOR", sequenceName = "SEQ_ALLOWED_SENSOR", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_ALLOWED_SENSOR")
    private Long id;

    @Column(name="NATURAL_ID")
    private long naturalId;

    @Enumerated(EnumType.STRING)
    @Column(name="ROAD_STATION_TYPE")
    private RoadStationType roadStationType;

}
