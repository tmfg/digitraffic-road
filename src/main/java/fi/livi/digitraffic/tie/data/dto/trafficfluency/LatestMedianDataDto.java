package fi.livi.digitraffic.tie.data.dto.trafficfluency;

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

import fi.livi.digitraffic.tie.data.dto.DataObjectDto;
import fi.livi.digitraffic.tie.data.model.trafficfluency.FluencyClass;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@ApiModel(value = "LatestMedianData", description = "The message contains the latest 5 minute median, corresponding average speed, fluency class, and timestamp of the latest update for each link.")
@Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LatestMedianDataDto implements DataObjectDto {

    @Id
    @JsonIgnore
    private int id;

    @ApiModelProperty(value = "Average speed, calculated based on the median journey time [km/h]", required = true)
    @NotNull
    private BigDecimal averageSpeed;

    @ApiModelProperty(value = "Median of journey times for this link, based on 5 minutes [s]", required = true)
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

    @JsonIgnore
    private LocalDateTime measured;

    public BigDecimal getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(BigDecimal averageSpeed) {
        this.averageSpeed = averageSpeed;
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
                + ", end time=" + getMeasuredLocalTime() + ", median tt="
                + medianJourneyTime + ", nobs=" + nobs + ", ratio="
                + ratioToFreeFlowSpeed + ", link=" + linkNaturalId;
    }

    @Override
    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(LocalDateTime measured) {
        this.measured = measured;
    }
}
