package fi.livi.digitraffic.tie.data.model.trafficfluency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@ApiModel(description = "The message contains the latest 5 minute median, corresponding average speed, fluency class, and timestamp of the latest update for each link.")
@Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LatestMedianData {

    @Id
    @JsonIgnore
    private int id;

    @ApiModelProperty(value = "Average speed, calculated based on the median journey time [km/h]", required = true)
    @NotNull
    private BigDecimal averageSpeed;

    @JsonIgnore
    private LocalDateTime measurementTimestamp;

    @ApiModelProperty(value = "Median of journey times for this link, based on 5 minutes [s]")
    private Long medianJourneyTime;

    @JsonIgnore
    @NotNull
    private BigDecimal ratioToFreeFlowSpeed;

    @ApiModelProperty(name = "linkId", value = "Link identifier (naturalId)", required = true)
    @JsonProperty(value = "linkId")
    @NotNull
    private long linkNaturalId;

    @ApiModelProperty(value = "Number of observations that were used to calculate the median journey time")
    private Integer nobs;

    @Transient
    private FluencyClass fluencyClass;

    public BigDecimal getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(BigDecimal averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public LocalDateTime getMeasurementTimestamp() {
        return measurementTimestamp;
    }

    public void setMeasurementTimestamp(LocalDateTime measurementTimestamp) {
        this.measurementTimestamp = measurementTimestamp;
    }

    @ApiModelProperty(value = "Measurement timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getMeasurementLocalTime() {
        return ToStringHelpper.toString(measurementTimestamp, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Measurement timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getMeasurementUtc() {
        return ToStringHelpper.toString(measurementTimestamp, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    public Long getMedianJourneyTime() {
        return medianJourneyTime;
    }

    public void setMedianJourneyTime(Long medianJourneyTime) {
        this.medianJourneyTime = medianJourneyTime;
    }

    public BigDecimal getRatioToFreeFlowSpeed() {
        return ratioToFreeFlowSpeed;
    }

    public void setRatioToFreeFlowSpeed(BigDecimal ratioToFreeFlowSpeed) {
        this.ratioToFreeFlowSpeed = ratioToFreeFlowSpeed;
    }

    public long getLinkNaturalId() {
        return linkNaturalId;
    }

    public void setLinkNaturalId(long linkId) {
        this.linkNaturalId = linkId;
    }

    public Integer getNobs() {
        return nobs;
    }

    public void setNobs(Integer nobs) {
        this.nobs = nobs;
    }


    public FluencyClass getFluencyClass() {
        return fluencyClass;
    }

    public void setFluencyClass(FluencyClass fluencyClass) {
        this.fluencyClass = fluencyClass;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", avg speed=" + averageSpeed
                + ", end time=" + measurementTimestamp + ", median tt="
                + medianJourneyTime + ", nobs=" + nobs + ", ratio="
                + ratioToFreeFlowSpeed + ", link=" + linkNaturalId;
    }

}
