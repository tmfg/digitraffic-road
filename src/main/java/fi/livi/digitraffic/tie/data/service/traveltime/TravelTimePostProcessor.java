package fi.livi.digitraffic.tie.data.service.traveltime;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.data.model.FluencyClass;
import fi.livi.digitraffic.tie.data.service.TrafficFluencyService;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.LinkFastLaneDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.ProcessedMeasurementDataDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.ProcessedMedianDataDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementLinkDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementsDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMedianDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMediansDto;

@Component
public class TravelTimePostProcessor {

    private final static Logger log = LoggerFactory.getLogger(TravelTimePostProcessor.class);

    private final TrafficFluencyService trafficFluencyService;

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
    // max values for database columns
    private static final BigDecimal MAX_SPEED = new BigDecimal("999");
    private static final Long MAX_TRAVEL_TIME = 9999999L;
    private static final Integer MAX_NOBS = 9999;
    private static final BigDecimal MAX_RATIO_TO_FREE_FLOW_SPEED = new BigDecimal("9.999");

    @Autowired
    public TravelTimePostProcessor(final TrafficFluencyService trafficFluencyService) {
        this.trafficFluencyService = trafficFluencyService;
    }

    /**
     * {@inheritDoc}
     */
    public List<ProcessedMedianDataDto> processMedians(final TravelTimeMediansDto medians, final Map<Long, LinkFastLaneDto> validLinks) {

        final List<ProcessedMedianDataDto> processed = new ArrayList<>();

        final Date periodStart = medians.periodStart;
        // calculate end timestamp of the 5 minute period
        final Date periodEnd = DateUtils.addMinutes(periodStart,5);

        for (TravelTimeMedianDto median : medians.medians) {

            LinkFastLaneDto linkData = validLinks.get(median.linkNaturalId);

            final BigDecimal avgSpeed = getAverageSpeed(linkData, median);
            final BigDecimal ratioToFreeFlowSpeed = getRatioToFreeFlowSpeed(linkData, avgSpeed);
            final FluencyClass fluency = trafficFluencyService.getMatchingFluencyClass(ratioToFreeFlowSpeed);
            log.debug("fluency class = {}", fluency);
            final Long fluencyClassNumber = fluency != null ? new Long(fluency.getCode()) : null;

            ProcessedMedianDataDto p = new ProcessedMedianDataDto(linkData.linkId, linkData.naturalId, periodEnd, median.median,
                                                                  median.numberOfObservations, avgSpeed, ratioToFreeFlowSpeed, fluencyClassNumber);

            if (isSomeFieldTooLarge(p)) {
                log.error("median with too large values does not fit in database, skipped: {}", p);
            } else {
                processed.add(p);
            }
        }
        return processed;
    }

    /**
     * Goes through a list of measurements, calculates measurement timestamps
     * based on base timestamp + offset, replaces link natural_ids with ids
     *
     * @param measurements
     * @param validLinks
     * @return
     */
    public static List<ProcessedMeasurementDataDto> processMeasurements(final TravelTimeMeasurementsDto measurements,
                                                                        final Map<Long, LinkFastLaneDto> validLinks) {

        final Date baseDate = measurements.periodStart;
        List<ProcessedMeasurementDataDto> processedMeasurements = new ArrayList<>();

        for (TravelTimeMeasurementLinkDto link : measurements.measurements) {
            final Long linkId = validLinks.get(link.linkNaturalId).linkId;

            for (TravelTimeMeasurementDto measurement : link.measurements) {

                final ProcessedMeasurementDataDto processed = new ProcessedMeasurementDataDto(DateUtils.addSeconds(baseDate, measurement.offset),
                                                                                              measurement.travelTime,
                                                                                              linkId);

                if (processed.travelTime > MAX_TRAVEL_TIME) {
                    log.error("measurement with too large values skipped: {}", processed);
                } else if (processed.travelTime <= 0) {
                    log.error("measurement with zero or negative travel time skipped: {}", processed);
                } else {
                    processedMeasurements.add(processed);
                }
            }
        }
        return processedMeasurements;
    }

    private BigDecimal getAverageSpeed(LinkFastLaneDto linkData, TravelTimeMedianDto median) {
        if (median.median != 0) {
            final BigDecimal avgSpeed = new BigDecimal(60 * 60 * linkData.length, MATH_CONTEXT)
                                                   .divide(new BigDecimal(1000 * median.median), MATH_CONTEXT);

            log.debug("link " + linkData.linkId + ", length=" + linkData.length + "m, median travel time="+
                      median.median + "s => avg speed=" + avgSpeed);
            return avgSpeed;
        } else {
            return null;
        }
    }

    private BigDecimal getRatioToFreeFlowSpeed(LinkFastLaneDto linkData, BigDecimal avgSpeed) {
        if (avgSpeed != null) {
            final BigDecimal ratioToFreeFlowSpeed = avgSpeed.divide(new BigDecimal(linkData.getFreeFlowSpeed()), MATH_CONTEXT);
            log.debug("link avg speed=" + avgSpeed + ", free speed=" + linkData.getFreeFlowSpeed() +
                      " => ratio to free flow speed = " + ratioToFreeFlowSpeed);

            return ratioToFreeFlowSpeed;
        } else {
            return null;
        }
    }

    /**
     * Check whether some field has too large value to fit in database column
     *
     * @param p
     * @return
     */
    private boolean isSomeFieldTooLarge(ProcessedMedianDataDto p) {
        if (p.averageSpeed != null && p.averageSpeed.compareTo(MAX_SPEED) > 0) {
            return true;
        } else if (p.medianTravelTime != null && p.medianTravelTime.compareTo(MAX_TRAVEL_TIME) > 0) {
            return true;
        } else if (p.nobs != null && p.nobs.compareTo(MAX_NOBS) > 0) {
            return true;
        } else if (p.ratioToFreeFlowSpeed != null && p.ratioToFreeFlowSpeed.compareTo(MAX_RATIO_TO_FREE_FLOW_SPEED) > 0) {
            return true;
        }
        return false;
    }
}