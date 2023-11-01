package fi.livi.digitraffic.tie.model.trafficmessage;

import java.time.Instant;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Immutable;
import org.locationtech.jts.geom.Geometry;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.AreaType;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Simple traffic messages area locations model.
 * @see @https://github.com/tmfg/metadata/tree/master/geometry/regions
 */
@Immutable
@Entity
@Table(name = "REGION_GEOMETRY")
public class RegionGeometry {

    @Id
    @SequenceGenerator(name = "SEQ_REGION_GEOMETRY", sequenceName = "SEQ_REGION_GEOMETRY", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_REGION_GEOMETRY")
    private Long id;

    private String name;
    private Integer locationCode;
    @Enumerated(EnumType.STRING)
    private AreaType type;
    private Instant effectiveDate;
    @ColumnTransformer(write = "ST_MakeValid(?)")
    private Geometry geometry;
    private Instant versionDate;
    private String gitId;
    private String gitPath;
    private String gitCommitId;

    public RegionGeometry() {
        // For Hibernate
    }

    public RegionGeometry(final String name, final Integer locationCode, final AreaType type, final Instant effectiveDate,
                          final Geometry geometry, final Instant versionDate, final String gitId, final String gitPath, final String gitCommitId) {
        this.name = name;
        this.locationCode = locationCode;
        this.type = type;
        this.effectiveDate = effectiveDate;
        this.geometry = geometry;
        this.versionDate = versionDate;
        this.gitId = gitId;
        this.gitPath = gitPath;
        this.gitCommitId = gitCommitId;
    }

    public Long getId() {
        return id;
    }

    public Integer getLocationCode() {
        return locationCode;
    }

    public String getName() {
        return name;
    }

    public AreaType getType() {
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

    public String getGitId() {
        return gitId;
    }

    public String getGitPath() {
        return gitPath;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public String toString() {
        return ToStringHelper.toStringFull(this, "geometry");
    }

    public boolean isValid() {
        return !AreaType.UNKNOWN.equals(type);
    }
}
