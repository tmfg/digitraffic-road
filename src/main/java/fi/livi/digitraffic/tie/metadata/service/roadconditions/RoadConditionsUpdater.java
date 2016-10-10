package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadSectionCoordinatesRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.RoadSectionCoordinates;
import fi.livi.digitraffic.tie.metadata.model.RoadSectionCoordinatesPK;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionNaturalIdHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoadConditionsUpdater {

    private static final Logger log = LoggerFactory.getLogger(RoadConditionsUpdater.class);

    @Autowired
    private RoadConditionsClient roadConditionsClient;

    @Autowired
    private RoadSectionCoordinatesRepository roadSectionCoordinatesRepository;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Transactional
    public void updateRoadSectionCoordinates() {

        List<RoadSectionCoordinatesDto> roadSectionCoordinates = roadConditionsClient.getRoadSections();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll();

        printLogInfo(roadSectionCoordinates, forecastSections);

        roadSectionCoordinatesRepository.deleteAllInBatch();

        for (ForecastSection forecastSection : forecastSections) {

            BigDecimal roadNumAndSectionNum = ForecastSectionNaturalIdHelper.getRoadNumAndSectionNum(forecastSection.getNaturalId());

            Optional<RoadSectionCoordinatesDto> coordinatesDto = roadSectionCoordinates.stream()
                    .filter(c -> ForecastSectionNaturalIdHelper.getRoadNumAndSectionNum(c.getNaturalId()).equals(roadNumAndSectionNum))
                    .findFirst();

            if (coordinatesDto.isPresent()) {
                forecastSection.getRoadSectionCoordinates().clear();
                long orderNumber = 1;
                for (Pair<BigDecimal, BigDecimal> coordinates : coordinatesDto.get().getCoordinates()) {
                    forecastSection.getRoadSectionCoordinates().add(
                            new RoadSectionCoordinates(forecastSection, new RoadSectionCoordinatesPK(forecastSection.getId(), orderNumber),
                                                       coordinates.getLeft(), coordinates.getRight()));
                    orderNumber++;
                }
            } else {
                log.info("ForecastSection naturalId mismatch while saving road section coordinates. Forecast section coordinates for ForecastSection with naturalId: " +
                        forecastSection.getNaturalId() + " do not exist in update data.");
            }
        }
        forecastSectionRepository.save(forecastSections);
        forecastSectionRepository.flush();
    }

    private void printLogInfo(List<RoadSectionCoordinatesDto> roadSectionCoordinates, List<ForecastSection> forecastSections) {

        Optional<Long> newCoordinatesCount = roadSectionCoordinates.stream().map(c -> c.getCoordinates().stream().count()).reduce((a, b) -> a + b);

        Long existingCoordinatesCount = roadSectionCoordinatesRepository.getRoadSectionCoordinatesCount();

        log.info("Updating road section coordinates. Number of coordinates in database: " + existingCoordinatesCount +
                 ". Number of coordinates received for update: " + newCoordinatesCount.orElse(null));
        List<String> externalNaturalIds = roadSectionCoordinates.stream().map(c -> c.getNaturalId()).collect(Collectors.toList());
        List<String> existingNaturalIds = forecastSections.stream().map(f -> f.getNaturalId()).collect(Collectors.toList());
        List<String> newForecastSectionNaturalIds = externalNaturalIds.stream().filter(n -> !existingNaturalIds.contains(n)).collect(Collectors.toList());
        List<String> missingForecastSectionNaturalIds = existingNaturalIds.stream().filter(n -> !externalNaturalIds.contains(n)).collect(Collectors.toList());
        log.info("Database is missing " + newForecastSectionNaturalIds.size() + " ForecastSections (naturalId): " + StringUtils.join(newForecastSectionNaturalIds, ", ") +
                 ". Update data is missing " + missingForecastSectionNaturalIds.size() + " ForecastSections (naturalId): " + StringUtils.join(missingForecastSectionNaturalIds, ", "));
    }
}
