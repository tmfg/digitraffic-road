package fi.livi.digitraffic.tie.model.roadstation;

import java.io.Serializable;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

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
