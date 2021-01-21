package fi.livi.digitraffic.tie.model.v3.trafficannouncement.geojson;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

/**
 * CREATE TABLE area_location (
 *     id                      SERIAL NOT NULL, -- just for ordering from latest to oldest
 *     location_code           INTEGER NOT NULL, -- ie. 00003_Suomi.json -> 3
 *     type                    TEXT NOT NULL,
 *     effective_date          TIMESTAMP(0) WITH TIME ZONE NOT NULL,
 *     geometry                TEXT NOT NULL,
 *     version_date            TIMESTAMP(0) WITH TIME ZONE NOT NULL -- creation date from the version control
 * );
 */
@Entity
@Table(name = "AREA_LOCATION_REGION")
public class AreaLocationRegion {

    @Id
    @GenericGenerator(name = "SEQ_AREA_LOCATION_REGION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_AREA_LOCATION_REGION"))
    @GeneratedValue(generator = "SEQ_AREA_LOCATION_REGION")
    private Long id;

    private Integer locationCode;

    private String type;

    private Instant effectiveDate;

    private Geometry geometry;

    private Instant versionDate;

    public AreaLocationRegion() {
        // For Hibernate
    }

    public AreaLocationRegion(final Integer locationCode, final String type, final Instant effectiveDate,
                              final Geometry geometry, final Instant versionDate) {
        this.locationCode = locationCode;
        this.type = type;
        this.effectiveDate = effectiveDate;
        this.geometry = geometry;
        this.versionDate = versionDate;
    }

    public Long getId() {
        return id;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public String getType() {
        return type;
    }

    public Instant getEffectiveDate() {
        return effectiveDate;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Instant getVersionDate() {
        return versionDate;
    }
}
