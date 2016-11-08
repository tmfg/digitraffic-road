package fi.livi.digitraffic.tie.metadata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionNaturalIdHelper;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.Coordinate;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionCoordinatesDto;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @ManyToOne
    @JoinColumn(name="road_id")
    @Fetch(FetchMode.JOIN)
    private Road road;

    @ManyToOne
    @JoinColumn(name="start_road_section_id")
    @Fetch(FetchMode.JOIN)
    private RoadSection startRoadSection;

    @ManyToOne
    @JoinColumn(name="end_road_section_id")
    @Fetch(FetchMode.JOIN)
    private RoadSection endRoadSection;

    @OneToMany(mappedBy = "forecastSectionCoordinatesPK.forecastSectionId", cascade = CascadeType.ALL)
    private List<ForecastSectionCoordinates> forecastSectionCoordinates;

    @OneToMany(mappedBy = "forecastSectionWeatherPK.forecastSectionId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForecastSectionWeather> forecastSectionWeatherList;

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

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    public RoadSection getStartRoadSection() {
        return startRoadSection;
    }

    public void setStartRoadSection(RoadSection startRoadSection) {
        this.startRoadSection = startRoadSection;
    }

    public RoadSection getEndRoadSection() {
        return endRoadSection;
    }

    public void setEndRoadSection(RoadSection endRoadSection) {
        this.endRoadSection = endRoadSection;
    }

    public List<ForecastSectionCoordinates> getForecastSectionCoordinates() {
        return forecastSectionCoordinates;
    }

    public void setForecastSectionCoordinates(List<ForecastSectionCoordinates> forecastSectionCoordinates) {
        this.forecastSectionCoordinates = forecastSectionCoordinates;
    }

    public List<ForecastSectionWeather> getForecastSectionWeatherList() {
        return forecastSectionWeatherList;
    }

    public void setForecastSectionWeatherList(List<ForecastSectionWeather> forecastSectionWeatherList) {
        this.forecastSectionWeatherList = forecastSectionWeatherList;
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

    public boolean corresponds(ForecastSectionCoordinatesDto value) {
        if (value.getName().equals(description) && coordinatesCorrespond(value.getCoordinates())) {
            return true;
        }
        return false;
    }

    private boolean coordinatesCorrespond(List<Coordinate> coordinates) {

        if (getForecastSectionCoordinates().size() != coordinates.size()) return false;

        List<Coordinate> sorted1 = this.forecastSectionCoordinates.stream().sorted((a, b) -> {
            if (a.getLongitude().equals(b.getLongitude())) {
                return a.getLatitude().compareTo(b.getLatitude());
            }
            return a.getLongitude().compareTo(b.getLongitude());
        }).map(c -> new Coordinate(Arrays.asList(c.getLongitude(), c.getLatitude()))).collect(Collectors.toList());

        List<Coordinate> sorted2 = coordinates.stream().sorted((a, b) -> {
            if (a.longitude.equals(b.longitude)) {
                return a.latitude.compareTo(b.latitude);
            }
            return a.longitude.compareTo(b.longitude);
        }).collect(Collectors.toList());

        for (int i = 0; i < getForecastSectionCoordinates().size(); ++i) {
            if (sorted1.get(i).longitude.compareTo(sorted2.get(i).longitude) != 0 ||
                sorted1.get(i).latitude.compareTo(sorted2.get(i).latitude) != 0) {
                return false;
            }
        }
        return true;
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
               ", road=" + road +
               ", startRoadSection=" + startRoadSection +
               ", endRoadSection=" + endRoadSection +
               ", forecastSectionCoordinates=" + forecastSectionCoordinates +
               '}';
    }
}
