package fi.livi.digitraffic.tie.data.service.traveltime;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.annotation.PerformanceMonitor;
import fi.livi.digitraffic.tie.data.dao.LinkFastLaneRepository;
import fi.livi.digitraffic.tie.data.dao.TravelTimeRepository;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.LinkFastLaneDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.ProcessedMeasurementDataDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.ProcessedMedianDataDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementLinkDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMeasurementsDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMedianDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.TravelTimeMediansDto;
import fi.livi.digitraffic.tie.metadata.model.DataType;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Service
public class TravelTimeUpdater {

    private final static Logger log = LoggerFactory.getLogger(TravelTimeUpdater.class);

    private final TravelTimeClient travelTimeClient;
    private final LinkFastLaneRepository linkFastLaneRepository;
    private final TravelTimePostProcessor travelTimePostProcessor;
    private final TravelTimeRepository travelTimeRepository;
    private final DataStatusService dataStatusService;

    @Autowired
    public TravelTimeUpdater(final TravelTimeClient travelTimeClient,
                             final LinkFastLaneRepository linkFastLaneRepository,
                             final TravelTimePostProcessor travelTimePostProcessor,
                             final TravelTimeRepository travelTimeRepository,
                             final DataStatusService dataStatusService) {
        this.travelTimeClient = travelTimeClient;
        this.linkFastLaneRepository = linkFastLaneRepository;
        this.travelTimePostProcessor = travelTimePostProcessor;
        this.travelTimeRepository = travelTimeRepository;
        this.dataStatusService = dataStatusService;
    }

    @Transactional
    public void updateIndividualMeasurements(final ZonedDateTime from) {

        TravelTimeMeasurementsDto data = travelTimeClient.getMeasurements(from);

        if (data != null && data.measurements != null) {
            log.info("Fetched PKS individual measurements for {} links. Period start {} and duration {}",
                     data.measurements.size(), data.periodStart, data.duration);
        } else {
            log.warn("Travel time measurement data was empty @ {}", from.format(DateTimeFormatter.ISO_DATE_TIME));
            dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEASUREMENTS_DATA, from);
            return;
        }

        final Map<Long, LinkFastLaneDto> validLinks = linkFastLaneRepository.findNonObsoleteLinks();

        log.info("Valid PKS links in database {}", validLinks.size());

        final Set<Long> validLinkNaturalIds = validLinks.keySet();

        final List<Long> linkIdsForMissingLinks = data.measurements.stream().filter(m -> !validLinkNaturalIds.contains(m.linkNaturalId))
                                                                            .map(m -> m.linkNaturalId)
                                                                            .collect(Collectors.toList());

        if (!linkIdsForMissingLinks.isEmpty()) {
            log.info("following links have data but no link in db: {}", linkIdsForMissingLinks);
        }

        List<TravelTimeMeasurementLinkDto> measurementsForValidLinks =
                                                    data.measurements.stream().filter(m -> validLinkNaturalIds.contains(m.linkNaturalId))
                                                                              .collect(Collectors.toList());

        List<ProcessedMeasurementDataDto> processed =
                TravelTimePostProcessor.processMeasurements(new TravelTimeMeasurementsDto(data.periodStart,
                                                                                          data.duration,
                                                                                          measurementsForValidLinks), validLinks);

        travelTimeRepository.insertMeasurementData(processed);
        dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEASUREMENTS_DATA, from);

        log.info("Processed and saved PKS measurements for {} links", processed.size());
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Transactional
    public void updateMedians(final ZonedDateTime from) {

        final TravelTimeMediansDto data = travelTimeClient.getMedians(from);

        if (data != null && data.medians != null) {
            log.info("Fetched PKS medians for {} links. Period start {} and duration {}", data.medians.size(), data.periodStart, data.duration);
        } else {
            log.warn("Travel time median data was empty @ {}", from.format(DateTimeFormatter.ISO_DATE_TIME));
            dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEDIANS_DATA, from);
            return;
        }

        final Map<Long, LinkFastLaneDto> validLinks = linkFastLaneRepository.findNonObsoleteLinks();

        final Set<Long> validLinkNaturalIds = validLinks.keySet();

        final List<Long> linkIdsForMissingLinks = data.medians.stream().filter(m -> !validLinkNaturalIds.contains(m.linkNaturalId))
                                                                       .map(m -> m.linkNaturalId)
                                                                       .collect(Collectors.toList());

        if (!linkIdsForMissingLinks.isEmpty()) {
            log.info("following links have data but no link in db: {}", linkIdsForMissingLinks);
        }

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

        travelTimeRepository.insertMedianData(processedMedians);
        travelTimeRepository.updateLatestMedianData(processedMedians);
        dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEDIANS_DATA, from);

        log.info("Processed and saved PKS medians for {} links", processedMedians.size());
    }
}
