package fi.livi.digitraffic.tie.data.dto.trafficfluency;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import fi.livi.digitraffic.tie.data.dto.MeasuredDataObjectDto;
import fi.livi.digitraffic.tie.data.model.FluencyClass;
import fi.livi.digitraffic.tie.helper.ToStringHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@ApiModel(value = "LatestMedianData",
          description = "The message contains the latest 5 minute median, corresponding average speed, fluency class, and timestamp of the latest update for each link.",
          parent = MeasuredDataObjectDto.class)
@Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LatestMedianDataDto implements MeasuredDataObjectDto {

    @Id
    @JsonIgnore
    private Integer id;

    @ApiModelProperty(value = "Median speed, calculated based on the median journey time [km/h]", required = true)
    @NotNull
    private BigDecimal medianSpeed;

    @ApiModelProperty(value = "Median of journey times for this link, based on 5 minutes [s]", required = true)
    private Long medianJourneyTime;

    @JsonIgnore
    @NotNull
    private BigDecimal ratioToFreeFlowSpeed;

    @ApiModelProperty(value = "Link identifier (naturalId)", required = true)
    @JsonProperty(value = "id")
    @NotNull
    private long linkNaturalId;

    @ApiModelProperty(value = "Number of observations that were used to calculate the median journey time")
    private Integer nobs;

    @Transient
    private FluencyClass fluencyClass;

    @ApiModelProperty(value = "Value measured " + ToStringHelper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    private ZonedDateTime measuredTime;

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }

    public BigDecimal getMedianSpeed() {
        return medianSpeed;
    }

    public void setMedianSpeed(final BigDecimal medianSpeed) {
        this.medianSpeed = medianSpeed;
    }

    public Long getMedianJourneyTime() {
        return medianJourneyTime;
    }

    public void setMedianJourneyTime(final Long medianJourneyTime) {
        this.medianJourneyTime = medianJourneyTime;
    }

    public BigDecimal getRatioToFreeFlowSpeed() {
        return ratioToFreeFlowSpeed;
    }

    public void setRatioToFreeFlowSpeed(final BigDecimal ratioToFreeFlowSpeed) {
        this.ratioToFreeFlowSpeed = ratioToFreeFlowSpeed;
    }

    public long getLinkNaturalId() {
        return linkNaturalId;
    }

    public void setLinkNaturalId(final long linkId) {
        this.linkNaturalId = linkId;
    }

    public Integer getNobs() {
        return nobs;
    }

    public void setNobs(final Integer nobs) {
        this.nobs = nobs;
    }


    public FluencyClass getFluencyClass() {
        return fluencyClass;
    }

    public void setFluencyClass(final FluencyClass fluencyClass) {
        this.fluencyClass = fluencyClass;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", median speed=" + medianSpeed
                + ", end time=" + getMeasuredTime() + ", median tt="
                + medianJourneyTime + ", nobs=" + nobs + ", ratio="
                + ratioToFreeFlowSpeed + ", link=" + linkNaturalId;
    }
}
