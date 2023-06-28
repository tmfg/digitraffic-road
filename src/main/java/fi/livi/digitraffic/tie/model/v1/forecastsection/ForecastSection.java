package fi.livi.digitraffic.tie.model.v1.forecastsection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicUpdate;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.model.ReadOnlyCreatedAndModifiedFields;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionNaturalIdHelper;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@DynamicUpdate
public class ForecastSection extends ReadOnlyCreatedAndModifiedFields {

    @Id
    @SequenceGenerator(name = "SEQ_FORECAST_SECTION", sequenceName = "SEQ_FORECAST_SECTION", allocationSize = 1)
    @GeneratedValue(generator = "SEQ_FORECAST_SECTION")
    @JsonIgnore
    private Long id;

    /**
     * Road section identifier 15 characters ie. 00004_112_000_0
     * 1. Road number 5 characters ie. 00004
     * 2. Road section 3 characters ie. 112
     * 3. Road section version 3 characters ie. 000
     * 4. Reserver for future needs 1 characters default 0
     * Delimiter is underscore "_"
     */

    @Schema(description =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("roadId")
    private String naturalId;

    @JsonIgnore
    private Integer version;

    @Schema(description = "Forecast section description")
    private String description;

    @Schema(description = "Road section number")
    private int roadSectionNumber;

    @Schema(description = "Forecast section road number")
    private int roadNumber;

    @Schema(description = "Road section version number")
    private int roadSectionVersionNumber;

    @Schema(description = "Forecast section start distance")
    private Integer startDistance;

    @Schema(description = "Forecast section end distance")
    private Integer endDistance;

    @Schema(description = "Forecast section length")
    private Integer length;

    @Schema(description = "Forecast section obsolete date")
    private Date obsoleteDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "forecastSectionWeatherPK.forecastSectionId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("time")
    private List<ForecastSectionWeather> forecastSectionWeatherList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "roadSegmentPK.forecastSectionId", cascade = CascadeType.ALL)
    @OrderBy("roadSegmentPK.orderNumber")
    private List<RoadSegment> roadSegments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "linkIdPK.forecastSectionId", cascade = CascadeType.ALL)
    @OrderBy("linkIdPK.orderNumber")
    private List<LinkId> linkIds = new ArrayList<>();

    private Geometry geometry;

    private Geometry geometrySimplified;

    public ForecastSection() {
    }

    public ForecastSection(final String naturalId, final int version, final String description) {
        this.naturalId = naturalId;
        this.roadNumber = ForecastSectionNaturalIdHelper.getRoadNumber(naturalId);
        this.roadSectionNumber = ForecastSectionNaturalIdHelper.getRoadSectionNumber(naturalId);
        this.version = version;
        if (version == 1) {
            this.roadSectionVersionNumber = ForecastSectionNaturalIdHelper.getRoadSectionVersionNumber(naturalId);
        } else {
            this.roadSectionVersionNumber = 0;
        }
        this.description = description;
        this.forecastSectionWeatherList = new ArrayList<>();
        this.obsoleteDate = null;
    }

    public Long getId() {
        return id;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(String naturalId) {
        this.naturalId = naturalId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(int roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getRoadSectionVersionNumber() {
        return roadSectionVersionNumber;
    }

    public void setRoadSectionVersionNumber(int roadSectionVersionNumber) {
        this.roadSectionVersionNumber = roadSectionVersionNumber;
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public List<ForecastSectionWeather> getForecastSectionWeatherList() {
        return forecastSectionWeatherList;
    }

    public List<RoadSegment> getRoadSegments() {
        return roadSegments;
    }

    public List<LinkId> getLinkIds() {
        return linkIds;
    }

    @Override
    public String toString() {
        return "ForecastSection{" +
               "id=" + id +
               ", naturalId='" + naturalId + '\'' +
               ", description='" + description + '\'' +
               ", roadSectionNumber=" + roadSectionNumber +
               ", roadNumber=" + roadNumber +
               ", roadSectionVersionNumber=" + roadSectionVersionNumber +
               ", startDistance=" + startDistance +
               ", endDistance=" + endDistance +
               ", length=" + length +
               ", obsoleteDate=" + obsoleteDate +
               '}';
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }

    public Geometry getGeometrySimplified() {
        return geometrySimplified;
    }

    public void setGeometrySimplified(final Geometry geometrySimplified) {
        this.geometrySimplified = geometrySimplified;
    }
}
