package fi.livi.digitraffic.tie.data.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.FluencyClassRepository;
import fi.livi.digitraffic.tie.data.dao.TrafficFluencyRepository;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.LatestMedianDataDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyRootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.FluencyClass;
import fi.livi.digitraffic.tie.helper.DateHelpper;

@Service
public class TrafficFluencyServiceImpl implements TrafficFluencyService {

    private final TrafficFluencyRepository trafficFluencyRepository;
    private final FluencyClassRepository fluencyClassRepository;
    private final int thresholdClasses;

    @Autowired
    TrafficFluencyServiceImpl(final TrafficFluencyRepository trafficFluencyRepository,
                              final FluencyClassRepository fluencyClassRepository,
                              @Value("${fluencyClasses.below.treshold}")
                              final Integer thresholdClasses) {
        this.trafficFluencyRepository = trafficFluencyRepository;
        this.fluencyClassRepository = fluencyClassRepository;
        this.thresholdClasses = thresholdClasses;
    }

    @Transactional(readOnly = true)
    @Override
    public TrafficFluencyRootDataObjectDto listCurrentTrafficFluencyData(boolean onlyUpdateInfo) {

        LocalDateTime updated = trafficFluencyRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new TrafficFluencyRootDataObjectDto(updated);
        } else {
            List<LatestMedianDataDto> latestMedians = trafficFluencyRepository.findLatestMediansForNonObsoleteLinks();

            for (LatestMedianDataDto lmd : latestMedians) {
                updated = DateHelpper.getNewest(updated, lmd.getMeasured());
                lmd.setFluencyClass(getMatchingFluencyClass(lmd.getRatioToFreeFlowSpeed()));
            }

            return new TrafficFluencyRootDataObjectDto(
                    latestMedians,
                    updated);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<FluencyClass> findAllFluencyClassesOrderByLowerLimitAsc() {
        return fluencyClassRepository.findAllOrderByLowerLimitAsc();
    }


    /**
     * Returns the correct fluency class for the given ratio to free flow speed
     * (between 0 and 1) If ratio is part of two classes, returns the "larger"
     * one
     *
     * @param ratioToFreeFlowSpeed
     * @return
     */
    private FluencyClass getMatchingFluencyClass(BigDecimal ratioToFreeFlowSpeed) {
        if (ratioToFreeFlowSpeed == null) {
            throw new NullPointerException();
        }
        // findAll() returns classes sorted smallest first
        // reverse order so that largest is first
        // this way, if ratio belongs to two classes (such as 0.1
        // matches A (0 - 0.1), B (0.1 - 0.25), we always return
        // the larger one (B)
        List reverseOrderForBorderCases = findAllFluencyClassesOrderByLowerLimitAsc();
        List cloned = new ArrayList(reverseOrderForBorderCases);
        Collections.reverse(cloned);
        for (Object o : cloned) {
            FluencyClass fc = (FluencyClass) o;
            if ( ( fc.getUpperLimit() == null || ratioToFreeFlowSpeed.compareTo(fc.getUpperLimit()) <= 0 )
                    && ratioToFreeFlowSpeed.compareTo(fc.getLowerLimit()) >= 0 ) {
                return fc;
            }
        }
        return null;
    }

    /**
     * Returns the threshold of alerts (min freeflowspeed ratio to result in an
     * alert)
     *
     * @return
     *
     * private BigDecimal getAlertThreshold() {
     *   List<FluencyClass> fluencyClasses = findAllFluencyClassesOrderByLowerLimitAsc();
     *   FluencyClass fc = fluencyClasses.get(thresholdClasses - 1);
     *   return fc.getUpperLimit();
     * }
     */
}
