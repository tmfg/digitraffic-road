package fi.livi.digitraffic.tie.data.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.dao.FluencyClassRepository;
import fi.livi.digitraffic.tie.data.dao.LinkFreeFlowSpeedRepository;
import fi.livi.digitraffic.tie.data.dao.TrafficFluencyRepository;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.LatestMedianDataDto;
import fi.livi.digitraffic.tie.data.dto.trafficfluency.TrafficFluencyRootDataObjectDto;
import fi.livi.digitraffic.tie.data.model.FluencyClass;

@Service
public class TrafficFluencyService {

    private final TrafficFluencyRepository trafficFluencyRepository;
    private final FluencyClassRepository fluencyClassRepository;
    private final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository;

    @Autowired
    TrafficFluencyService(final TrafficFluencyRepository trafficFluencyRepository,
                          final FluencyClassRepository fluencyClassRepository,
                          final LinkFreeFlowSpeedRepository linkFreeFlowSpeedRepository) {
        this.trafficFluencyRepository = trafficFluencyRepository;
        this.fluencyClassRepository = fluencyClassRepository;
        this.linkFreeFlowSpeedRepository = linkFreeFlowSpeedRepository;
    }

    @Transactional(readOnly = true)
    public TrafficFluencyRootDataObjectDto listCurrentTrafficFluencyData(final boolean onlyUpdateInfo) {

        LocalDateTime updated = trafficFluencyRepository.getLatestMeasurementTime();

        if (onlyUpdateInfo) {
            return new TrafficFluencyRootDataObjectDto(updated);
        } else {
            final List<LatestMedianDataDto> latestMedians = trafficFluencyRepository.findLatestMediansForNonObsoleteLinks();

            for (final LatestMedianDataDto lmd : latestMedians) {
                lmd.setFluencyClass(getMatchingFluencyClass(lmd.getRatioToFreeFlowSpeed()));
            }

            return new TrafficFluencyRootDataObjectDto(
                    latestMedians,
                    updated);
        }
    }

    @Transactional(readOnly = true)
    public TrafficFluencyRootDataObjectDto listCurrentTrafficFluencyData(final long linkId) {
        if (1 != linkFreeFlowSpeedRepository.linkExists(linkId)) {
            throw new ObjectNotFoundException("Link", linkId);
        }

        LocalDateTime updated = trafficFluencyRepository.getLatestMeasurementTime();

        final List<LatestMedianDataDto> latestMedians = trafficFluencyRepository.findLatestMediansForLink(linkId);

        for (final LatestMedianDataDto lmd : latestMedians) {
            lmd.setFluencyClass(getMatchingFluencyClass(lmd.getRatioToFreeFlowSpeed()));
        }

        return new TrafficFluencyRootDataObjectDto(
                latestMedians,
                updated);
    }

    @Transactional(readOnly = true)
    public List<FluencyClass> findAllFluencyClassesOrderByLowerLimitDesc() {
        return fluencyClassRepository.findAllOrderByLowerLimitDesc();
    }


    /**
     * Returns the correct fluency class for the given ratio to free flow speed
     * (between 0 and 1) If ratio is part of two classes, returns the "larger"
     * one
     *
     * @param ratioToFreeFlowSpeed
     * @return
     */
    private FluencyClass getMatchingFluencyClass(final BigDecimal ratioToFreeFlowSpeed) {
        if (ratioToFreeFlowSpeed == null) {
            throw new NullPointerException();
        }
        // findAllFluencyClassesOrderByLowerLimitDesc() returns classes sorted largest first
        // this way, if ratio belongs to two classes (such as 0.1 matches A (0 - 0.1), B (0.1 - 0.25),
        // we always return the larger one (B)
        final List<FluencyClass> desc = findAllFluencyClassesOrderByLowerLimitDesc();
        for (final FluencyClass fc : desc) {
            if ( ( fc.getUpperLimit() == null || ratioToFreeFlowSpeed.compareTo(fc.getUpperLimit()) <= 0 )
                    && ratioToFreeFlowSpeed.compareTo(fc.getLowerLimit()) >= 0 ) {
                return fc;
            }
        }
        return null;
    }
}
