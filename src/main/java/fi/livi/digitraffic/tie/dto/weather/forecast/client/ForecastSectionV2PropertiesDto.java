package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.util.List;

public class ForecastSectionV2PropertiesDto {

    private String id;

    private String description;

    private Integer roadNumber;

    private Integer roadSectionNumber;

    private Double totalLengthKm;

    private List<RoadSegmentDto> roadSegmentList;

    private List<Long> linkIdList;

    public ForecastSectionV2PropertiesDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Integer getRoadNumber() {
        return roadNumber;
    }

    public void setRoadNumber(final Integer roadNumber) {
        this.roadNumber = roadNumber;
    }

    public Integer getRoadSectionNumber() {
        return roadSectionNumber;
    }

    public void setRoadSectionNumber(final Integer roadSectionNumber) {
        this.roadSectionNumber = roadSectionNumber;
    }

    public Double getTotalLengthKm() {
        return totalLengthKm;
    }

    public void setTotalLengthKm(final Double totalLengthKm) {
        this.totalLengthKm = totalLengthKm;
    }

    public List<RoadSegmentDto> getRoadSegmentList() {
        return roadSegmentList;
    }

    public void setRoadSegmentList(final List<RoadSegmentDto> roadSegmentList) {
        this.roadSegmentList = roadSegmentList;
    }

    public List<Long> getLinkIdList() {
        return linkIdList;
    }

    public void setLinkIdList(final List<Long> linkIdList) {
        this.linkIdList = linkIdList;
    }
}
