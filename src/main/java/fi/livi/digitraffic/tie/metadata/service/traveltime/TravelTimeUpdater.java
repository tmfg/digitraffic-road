package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.LinkFastLaneDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.ProcessedMeasurementDataDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.ProcessedMedianDataDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TravelTimeMeasurementLinkDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TravelTimeMeasurementsDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TravelTimeMedianDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.TravelTimeMediansDto;

@Service
public class TravelTimeUpdater {

    private final static Logger log = LoggerFactory.getLogger(TravelTimeUpdater.class);

    private final TravelTimeClient travelTimeClient;
    private final LinkFastLaneDao linkFastLaneDao;
    private final TravelTimePostProcessor travelTimePostProcessor;
    private final TravelTimeDao travelTimeDao;

    @Autowired
    public TravelTimeUpdater(final TravelTimeClient travelTimeClient,
                             final LinkFastLaneDao linkFastLaneDao,
                             final TravelTimePostProcessor travelTimePostProcessor,
                             final TravelTimeDao travelTimeDao) {
        this.travelTimeClient = travelTimeClient;
        this.linkFastLaneDao = linkFastLaneDao;
        this.travelTimePostProcessor = travelTimePostProcessor;
        this.travelTimeDao = travelTimeDao;
    }

    @Transactional
    public void updateIndividualMeasurements(final ZonedDateTime from) {

        log.info("Importing individual PKS travel time measurements");

        TravelTimeMeasurementsDto data = travelTimeClient.getMeasurements(from);

        if (data != null && data.measurements != null) {
            log.info("Fetched PKS individual measurements for {} links. Period start {} and duration {}",
                     data.measurements.size(), data.periodStart, data.duration);
        }

        // determine currently valid links
        final Map<Long, LinkFastLaneDto> validLinks = linkFastLaneDao.findNonObsoleteLinks();

        log.info("Valid PKS links in database {}", validLinks);

        final Set<Long> validLinkNaturalIds = validLinks.keySet();

        final List<Long> linkIdsForMissingLinks = data.measurements.stream().filter(m -> !validLinkNaturalIds.contains(m.linkNaturalId))
                                                                            .map(m -> m.linkNaturalId)
                                                                            .collect(Collectors.toList());

        log.info("following links have data but no link in db: {}", linkIdsForMissingLinks);

        List<TravelTimeMeasurementLinkDto> measurementsForValidLinks =
                                                    data.measurements.stream().filter(m -> validLinkNaturalIds.contains(m.linkNaturalId))
                                                                              .collect(Collectors.toList());

        // post-process, calculate dates based on offset values
        List<ProcessedMeasurementDataDto> processed =
                TravelTimePostProcessor.processMeasurements(new TravelTimeMeasurementsDto(data.periodStart,
                                                                                          data.duration,
                                                                                          measurementsForValidLinks), validLinks);
        log.info("Processed PKS measurements: {}" + processed);

        travelTimeDao.insertMeasurementData(processed);
    }

    @Transactional
    public void updateMedians(final ZonedDateTime from) {

        log.info("Importing PKS travel time medians");

        final TravelTimeMediansDto data = travelTimeClient.getMedians(from);

        if (data != null && data.medians != null) {
            log.info("Fetched PKS medians for {} links. Period start {} and duration {}", data.medians.size(), data.periodStart, data.duration);
        }

        final Map<Long, LinkFastLaneDto> validLinks = linkFastLaneDao.findNonObsoleteLinks();

        final Set<Long> validLinkNaturalIds = validLinks.keySet();

        final List<Long> linkIdsForMissingLinks = data.medians.stream().filter(m -> !validLinkNaturalIds.contains(m.linkNaturalId))
                                                                       .map(m -> m.linkNaturalId)
                                                                       .collect(Collectors.toList());

        log.info("following links have data but no link in db: {}", linkIdsForMissingLinks);

        final List<TravelTimeMedianDto> mediansForValidLinks =
                                                    data.medians.stream().filter(m -> validLinkNaturalIds.contains(m.linkNaturalId))
                                                                         .collect(Collectors.toList());

        List<ProcessedMedianDataDto> processedMedians = travelTimePostProcessor.processMedians(new TravelTimeMediansDto(data.periodStart,
                                                                                                                        data.duration,
                                                                                                                        data.supplier,
                                                                                                                        data.service,
                                                                                                                        data.creationTime,
                                                                                                                        data.lastStaticDataUpdate,
                                                                                                                        mediansForValidLinks), validLinks);
        log.debug("Processed PKS medians: {}", processedMedians);

        travelTimeDao.insertMedianData(processedMedians);
        travelTimeDao.updateLatestMedianData(processedMedians);
    }
}
