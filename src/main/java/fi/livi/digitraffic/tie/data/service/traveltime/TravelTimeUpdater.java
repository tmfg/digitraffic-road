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
import org.springframework.dao.DuplicateKeyException;
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
            log.info("Fetched PKS individual measurementsCount={} links. Period startDate={} and duration={}",
                     data.measurements.size(), data.periodStart, data.duration);
        } else {
            log.warn("Travel time measurement data was empty @ updateDataUpdatedDate={}", from.format(DateTimeFormatter.ISO_DATE_TIME));
            dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEASUREMENTS_DATA, from);
            return;
        }

        final Map<Long, LinkFastLaneDto> nonObsoleteLinks = linkFastLaneRepository.findNonObsoleteLinks();

        log.info("nonObsoleteLinksCount={}", nonObsoleteLinks.size());

        logMissingLinks(data.measurements.stream().map(m -> m.linkNaturalId).collect(Collectors.toSet()), nonObsoleteLinks.keySet());

        final List<TravelTimeMeasurementLinkDto> measurementsForNonObsoleteLinks =
                                                    data.measurements.stream().filter(m -> nonObsoleteLinks.containsKey(m.linkNaturalId))
                                                                              .collect(Collectors.toList());

        final List<ProcessedMeasurementDataDto> processed =
                TravelTimePostProcessor.processMeasurements(new TravelTimeMeasurementsDto(data.periodStart,
                                                                                          data.duration,
                                                                                          measurementsForNonObsoleteLinks), nonObsoleteLinks);

        travelTimeRepository.insertMeasurementData(processed);
        dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEASUREMENTS_DATA, from);

        log.info("Processed and saved PKS measurements for processedCount={} links", processed.size());
    }

    @PerformanceMonitor(maxWarnExcecutionTime = 20000)
    @Transactional
    public void updateMedians(final ZonedDateTime from) {

        final TravelTimeMediansDto data = travelTimeClient.getMedians(from);

        if (data != null && data.medians != null) {
            log.info("Fetched PKS medians for linkCount={} links. Period start={} and duration={}", data.medians.size(), data.periodStart, data.duration);
        } else {
            log.warn("Travel time median data was empty @ updateDataUpdatedDate={}", from.format(DateTimeFormatter.ISO_DATE_TIME));
            dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEDIANS_DATA, from);
            return;
        }

        final Map<Long, LinkFastLaneDto> nonObsoleteLinks = linkFastLaneRepository.findNonObsoleteLinks();

        logMissingLinks(data.medians.stream().map(m -> m.linkNaturalId).collect(Collectors.toSet()), nonObsoleteLinks.keySet());

        final List<TravelTimeMedianDto> mediansForNonObsoleteLinks =
                                                    data.medians.stream().filter(m -> nonObsoleteLinks.containsKey(m.linkNaturalId))
                                                                         .collect(Collectors.toList());

        final List<ProcessedMedianDataDto> processedMedians = travelTimePostProcessor.processMedians(
            new TravelTimeMediansDto(data.periodStart,
                                     data.duration,
                                     data.supplier,
                                     data.service,
                                     data.creationTime,
                                     data.lastStaticDataUpdate,
                                     mediansForNonObsoleteLinks), nonObsoleteLinks);

        try {
            travelTimeRepository.insertMedianData(processedMedians);
            travelTimeRepository.updateLatestMedianData(processedMedians);
        } catch (DuplicateKeyException e) {
            log.warn("Trying to insert duplicate medians. periodStart={}", from); // Avoid daylight saving time problems during autumn
        } finally {
            dataStatusService.updateDataUpdated(DataType.TRAVEL_TIME_MEDIANS_DATA, from); // Skip this period when trying to add a duplicates
        }

        log.info("Processed and saved PKS medians for processedMediansCount={} links", processedMedians.size());
    }

    private void logMissingLinks(final Set<Long> naturalIds, final Set<Long> nonObsoleteLinkNaturalIds) {
        final Set<Long> missingLinks = naturalIds.stream().filter(naturalId -> !nonObsoleteLinkNaturalIds.contains(naturalId))
                                                          .collect(Collectors.toSet());

        if (!missingLinks.isEmpty()) {
            log.warn("following links have data but no link in db: {}", missingLinks);
        }
    }
}
