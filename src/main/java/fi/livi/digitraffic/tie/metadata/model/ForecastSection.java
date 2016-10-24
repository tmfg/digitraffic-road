package fi.livi.digitraffic.tie.metadata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionNaturalIdHelper;
import fi.livi.digitraffic.tie.metadata.service.roadconditions.Coordinate;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@DynamicUpdate
public class ForecastSection {

    private static final Logger log = LoggerFactory.getLogger(ForecastSection.class);

    @Id
    @GenericGenerator(name = "SEQ_FORECAST_SECTION", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
                      parameters = @Parameter(name = "sequence_name", value = "SEQ_FORECAST_SECTION"))
    @GeneratedValue(generator = "SEQ_FORECAST_SECTION")
    private Long id;

    /**
     * Road section identifier 15 characters ie. 00004_112_000_0
     * 1. Road number 5 characters ie. 00004
     * 2. Road section 3 characters ie. 112
     * 3. Road section version 3 characters ie. 000
     * 4. Reserver for future needs 1 characters default 0
     * Delimiter is underscore "_"
     */

    @ApiModelProperty(value =
            "Forecast section identifier 15 characters ie. 00004_112_000_0: \n" +
            "1. Road number 5 characters ie. 00004, \n" +
            "2. Road section 3 characters ie. 112, \n" +
            "3. Road section version 3 characters ie. 000, \n" +
            "4. Reserved for future needs 1 characters default 0")
    @JsonProperty("id")
    private String naturalId;

    @ApiModelProperty(value = "Forecast section description")
    private String description;

    @ApiModelProperty(value = "Road section number")
    private int roadSectionNumber;

    @ApiModelProperty(value = "Forecast section road number")
    private int roadNumber;

    @ApiModelProperty(value = "Road section version number")
    private int roadSectionVersionNumber;

    @ApiModelProperty(value = "Forecast section start distance")
    private Integer startDistance;

    @ApiModelProperty(value = "Forecast section end distance")
    private Integer endDistance;

    @ApiModelProperty(value = "Forecast section length")
    private Integer length;

    @ApiModelProperty(value = "Forecast section obsolete date")
    private Date obsoleteDate;

    @JsonIgnore
    private Long startRoadSectionId;

    @JsonIgnore
    private Long endRoadSectionId;

    @OneToMany(mappedBy = "forecastSectionCoordinatesPK.forecastSectionId", cascade = CascadeType.ALL)
    private List<ForecastSectionCoordinates> forecastSectionCoordinates;

    public ForecastSection() {
    }

    public ForecastSection(String naturalId, String description) {
        this.naturalId = naturalId;
        this.roadNumber = ForecastSectionNaturalIdHelper.getRoadNumber(naturalId);
        this.roadSectionNumber = ForecastSectionNaturalIdHelper.getRoadSectionNumber(naturalId);
        this.roadSectionVersionNumber = ForecastSectionNaturalIdHelper.getRoadSectionVersionNumber(naturalId);
        this.description = description;
        this.forecastSectionCoordinates = new ArrayList<>();
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

    public Long getStartRoadSectionId() {
        return startRoadSectionId;
    }

    public void setStartRoadSectionId(Long startRoadSectionId) {
        this.startRoadSectionId = startRoadSectionId;
    }

    public Long getEndRoadSectionId() {
        return endRoadSectionId;
    }

    public void setEndRoadSectionId(Long endRoadSectionId) {
        this.endRoadSectionId = endRoadSectionId;
    }

    public List<ForecastSectionCoordinates> getForecastSectionCoordinates() {
        return forecastSectionCoordinates;
    }

    public void setForecastSectionCoordinates(List<ForecastSectionCoordinates> forecastSectionCoordinates) {
        this.forecastSectionCoordinates = forecastSectionCoordinates;
    }

    public void addCoordinates(List<Coordinate> coordinates) {
        this.forecastSectionCoordinates = new ArrayList<>();
        long orderNumber = 1;
        for (Coordinate coordinate : coordinates) {
            if (!coordinate.isValid()) {
                log.info("Invalid coordinates for forecast section " + getNaturalId() + ". Coordinates were: " + coordinate.toString());
            } else {
                getForecastSectionCoordinates().add(
                        new ForecastSectionCoordinates(this, new ForecastSectionCoordinatesPK(getId(), orderNumber),
                                                       coordinate.longitude, coordinate.latitude));
                orderNumber++;
            }
        }
    }
}
