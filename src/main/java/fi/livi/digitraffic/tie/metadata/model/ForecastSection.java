package fi.livi.digitraffic.tie.metadata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;

@Entity
@DynamicUpdate
public class ForecastSection {

    @Id
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
    private int startDistance;

    @ApiModelProperty(value = "Forecast section road number")
    @Column(insertable = false, updatable = false)
    private int startSectionNumber;

    @ApiModelProperty(value = "Forecast section end number")
    @Column(insertable = false, updatable = false)
    private int endSectionNumber;

    @ApiModelProperty(value = "Forecast section end distance")
    private int endDistance;

    @ApiModelProperty(value = "Forecast section length")
    private int length;

    @JsonIgnore
    private long startRoadSectionId;

    @JsonIgnore
    private long endRoadSectionId;

    @OneToMany(mappedBy = "forecastSectionCoordinatesPK.forecastSectionId", cascade = CascadeType.ALL)
    private List<ForecastSectionCoordinates> forecastSectionCoordinates;

    public Long getId() {
        return id;
    }

    public String getNaturalId() {
        return naturalId;
    }

    public void setNaturalId(final String naturalId) {
        this.naturalId = naturalId;
    }

    public int getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(final int roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final int roadNumber) {
        this.roadNumber = roadNumber;
    }

    public int getRoadSectionVersionNumber() {
        return roadSectionVersionNumber;
    }

    public void setRoadSectionVersionNumber(int roadSectionVersionNumber) {
        this.roadSectionVersionNumber = roadSectionVersionNumber;
    }

    public int getStartSectionNumber() {
        return startSectionNumber;
    }

    public void setStartSectionNumber(final int startSectionNumber) {
        this.startSectionNumber = startSectionNumber;
    }

    public int getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(final int startDistance) {
        this.startDistance = startDistance;
    }

    public int getEndSectionNumber() {
        return endSectionNumber;
    }

    public void setEndSectionNumber(final int endSectionNumber) {
        this.endSectionNumber = endSectionNumber;
    }

    public int getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(final int endDistance) {
        this.endDistance = endDistance;
    }

    public int getLength() {
        return length;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    public long getStartRoadSectionId() {
        return startRoadSectionId;
    }

    public void setStartRoadSectionId(long startRoadSectionId) {
        this.startRoadSectionId = startRoadSectionId;
    }

    public long getEndRoadSectionId() {
        return endRoadSectionId;
    }

    public void setEndRoadSectionId(long endRoadSectionId) {
        this.endRoadSectionId = endRoadSectionId;
    }

    public List<ForecastSectionCoordinates> getForecastSectionCoordinates() {
        return forecastSectionCoordinates;
    }

    @ApiModelProperty(value = "Road section version number")
    public int getRoadSectionVersion() {
        return Integer.parseInt(naturalId.substring(10, 13));
    }
}
