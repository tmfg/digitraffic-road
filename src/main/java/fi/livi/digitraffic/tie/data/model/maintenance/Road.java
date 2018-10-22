
package fi.livi.digitraffic.tie.data.model.maintenance;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "number",
    "startDistance",
    "startPart",
    "endDistance",
    "endPart",
    "carriageway",
    "lane",
    "side",
    "startDate",
    "endDate",
    "mapDate"
})
public class Road implements Serializable
{

    private String name;

    @JsonProperty(required = true)
    private Integer number;

    /** aet */
    @JsonProperty(required = true)
    private Integer startDistance;

    /** aosa */
    @JsonProperty(required = true)
    private Integer startPart;

    /** let */
    private Integer endDistance;

    /** losa */
    private Integer endPart;

    /** ajr */
    private Integer carriageway;

    /** kaistta*/
    private Lane lane;

    private Integer side;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    private ZonedDateTime mapDate;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Road() {
    }

    public Road(String name, Integer number, Integer startDistance, Integer startPart, Integer endDistance, Integer endPart,
                Integer carriageway, Lane lane, Integer side, ZonedDateTime startDate, ZonedDateTime endDate, ZonedDateTime mapDate,
                Map<String, Object> additionalProperties) {
        this.name = name;
        this.number = number;
        this.startDistance = startDistance;
        this.startPart = startPart;
        this.endDistance = endDistance;
        this.endPart = endPart;
        this.carriageway = carriageway;
        this.lane = lane;
        this.side = side;
        this.startDate = startDate;
        this.endDate = endDate;
        this.mapDate = mapDate;
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getStartDistance() {
        return startDistance;
    }

    public void setStartDistance(Integer startDistance) {
        this.startDistance = startDistance;
    }

    public Integer getStartPart() {
        return startPart;
    }

    public void setStartPart(Integer startPart) {
        this.startPart = startPart;
    }

    public Integer getEndDistance() {
        return endDistance;
    }

    public void setEndDistance(Integer endDistance) {
        this.endDistance = endDistance;
    }

    public Integer getEndPart() {
        return endPart;
    }

    public void setEndPart(Integer endPart) {
        this.endPart = endPart;
    }

    public Integer getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(Integer carriageway) {
        this.carriageway = carriageway;
    }

    public Lane getLane() {
        return lane;
    }

    public void setLane(Lane lane) {
        this.lane = lane;
    }

    public Integer getSide() {
        return side;
    }

    public void setSide(Integer side) {
        this.side = side;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public ZonedDateTime getMapDate() {
        return mapDate;
    }

    public void setMapDate(ZonedDateTime mapDate) {
        this.mapDate = mapDate;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Road road = (Road) o;

        return new EqualsBuilder()
            .append(name, road.name)
            .append(number, road.number)
            .append(startDistance, road.startDistance)
            .append(startPart, road.startPart)
            .append(endDistance, road.endDistance)
            .append(endPart, road.endPart)
            .append(carriageway, road.carriageway)
            .append(lane, road.lane)
            .append(side, road.side)
            .append(startDate, road.startDate)
            .append(endDate, road.endDate)
            .append(mapDate, road.mapDate)
            .append(additionalProperties, road.additionalProperties)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .append(number)
            .append(startDistance)
            .append(startPart)
            .append(endDistance)
            .append(endPart)
            .append(carriageway)
            .append(lane)
            .append(side)
            .append(startDate)
            .append(endDate)
            .append(mapDate)
            .append(additionalProperties)
            .toHashCode();
    }
}