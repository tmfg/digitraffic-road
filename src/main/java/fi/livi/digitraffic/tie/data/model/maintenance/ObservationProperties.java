
package fi.livi.digitraffic.tie.data.model.maintenance;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "ObservationProperties properties", value = "ObservationProperties")
@JsonPropertyOrder({
   "workMachine",
   "location",
   "direction",
   "contractId",
   "observationTime",
   "performedTasks"
})
public class ObservationProperties implements Serializable {

    @JsonProperty(required = true)
    private WorkMachine workMachine;

    private Road road;

    private Link link;

    private Double direction;

    private Integer contractId;

    @JsonProperty(required = true)
    private ZonedDateTime observationTime;

    private final List<PerformedTask> performedTasks;

    @JsonIgnore
    private final Map<String, Object> additionalProperties;

    @JsonCreator
    public ObservationProperties(final WorkMachine workMachine, final Road road, final Link link, final Double direction, final Integer contractId,
                                 final ZonedDateTime observationTime, final List<PerformedTask> performedTasks,
                                 final Map<String, Object> additionalProperties) {
        this.workMachine = workMachine;
        this.road = road;
        this.link = link;
        this.direction = direction;
        this.contractId = contractId;
        this.observationTime = observationTime;
        this.performedTasks = performedTasks;
        this.additionalProperties = additionalProperties;
    }

    public WorkMachine getWorkMachine() {
        return workMachine;
    }

    public void setWorkMachine(final WorkMachine workMachine) {
        this.workMachine = workMachine;
    }

    public Double getDirection() {
        return direction;
    }

    public void setDirection(final Double direction) {
        this.direction = direction;
    }

    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(final Integer contractId) {
        this.contractId = contractId;
    }

    public ZonedDateTime getObservationTime() {
        return observationTime;
    }

    public void setObservationTime(final ZonedDateTime observationTime) {
        this.observationTime = observationTime;
    }

    public List<PerformedTask> getPerformedTasks() {
        return performedTasks;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(final String name, final Object value) {
        this.additionalProperties.put(name, value);
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(final Road road) {
        this.road = road;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(final Link link) {
        this.link = link;
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

        ObservationProperties that = (ObservationProperties) o;

        return new EqualsBuilder()
            .append(workMachine, that.workMachine)
            .append(road, that.road)
            .append(link, that.link)
            .append(direction, that.direction)
            .append(contractId, that.contractId)
            .append(observationTime, that.observationTime)
            .append(performedTasks, that.performedTasks)
            .append(additionalProperties, that.additionalProperties)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(workMachine)
            .append(road)
            .append(link)
            .append(direction)
            .append(contractId)
            .append(observationTime)
            .append(performedTasks)
            .append(additionalProperties)
            .toHashCode();
    }
}
