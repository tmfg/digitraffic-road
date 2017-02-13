package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.math.BigDecimal;
import java.util.Date;

public class ProcessedMedianDataDto {

    public final Long linkId;

    public final Long linkNaturalId;

    public final Date periodEnd;

    public final Long medianTravelTime;

    public final Integer nobs;

    public final BigDecimal averageSpeed;

    public final BigDecimal ratioToFreeFlowSpeed;

    public final Long fluencyClassNumber;

    public ProcessedMedianDataDto(Long linkId, Long linkNaturalId, Date periodEnd, Long medianTravelTime, Integer nobs, BigDecimal averageSpeed,
                                  BigDecimal ratioToFreeFlowSpeed, Long fluencyClassNumber) {
        this.linkId = linkId;
        this.linkNaturalId = linkNaturalId;
        this.periodEnd = periodEnd;
        this.medianTravelTime = medianTravelTime;
        this.nobs = nobs;
        this.averageSpeed = averageSpeed;
        this.ratioToFreeFlowSpeed = ratioToFreeFlowSpeed;
        this.fluencyClassNumber = fluencyClassNumber;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", link=" + linkId
                + ", periodEnd=" + periodEnd
                + ", link natural id=" + linkNaturalId + ", median="
                + medianTravelTime + ", nobs=" + nobs
                + ", avgspeed=" + averageSpeed + ", ratio="
                + ratioToFreeFlowSpeed + ", fluency=" + fluencyClassNumber;
    }
}