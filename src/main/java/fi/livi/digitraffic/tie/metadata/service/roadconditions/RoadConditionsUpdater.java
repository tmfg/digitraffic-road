package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionCoordinatesRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoadConditionsUpdater {

    private static final Logger log = LoggerFactory.getLogger(RoadConditionsUpdater.class);

    private final RoadConditionsClient roadConditionsClient;

    private final ForecastSectionCoordinatesRepository forecastSectionCoordinatesRepository;

    private final ForecastSectionRepository forecastSectionRepository;

    @Autowired
    public RoadConditionsUpdater(RoadConditionsClient roadConditionsClient, ForecastSectionCoordinatesRepository forecastSectionCoordinatesRepository, ForecastSectionRepository forecastSectionRepository) {
        this.roadConditionsClient = roadConditionsClient;
        this.forecastSectionCoordinatesRepository = forecastSectionCoordinatesRepository;
        this.forecastSectionRepository = forecastSectionRepository;
    }

    @Transactional
    public void updateForecastSectionCoordinates() {

        List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = roadConditionsClient.getForecastSectionMetadata();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll();
        Set<String> existingForecastSections = forecastSections.stream().map(fs -> fs.getNaturalId()).collect(Collectors.toSet());

        printLogInfo(forecastSectionCoordinates, forecastSections);

        forecastSectionCoordinatesRepository.deleteAllInBatch();

        Map<String, ForecastSection> naturalIdToForecastSections = forecastSections.stream().collect(Collectors.toMap(fs -> fs.getNaturalId(), fs -> fs));

        Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd = forecastSectionCoordinates.stream().filter(
                fs -> !existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(c -> c.getNaturalId(), c -> c));

        Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate = forecastSectionCoordinates.stream().filter(
                fs -> existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(c -> c.getNaturalId(), c -> c));

        Set<String> receivedForecastSections = forecastSectionCoordinates.stream().map(fs -> fs.getNaturalId()).collect(Collectors.toSet());
        Map<String, ForecastSection> forecastSectionsToDelete = forecastSections.stream().filter(
                fs -> !receivedForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(c -> c.getNaturalId(), c -> c));

        addForecastSections(naturalIdToForecastSections, forecastSectionsToAdd);
        updateForecastSections(naturalIdToForecastSections, forecastSectionsToUpdate);
        markForecastSectionsObsolete(naturalIdToForecastSections, forecastSectionsToDelete);

        forecastSectionRepository.save(naturalIdToForecastSections.values());
        forecastSectionRepository.flush();
    }

    private void addForecastSections(Map<String, ForecastSection> forecastSections, Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd) {

        for (Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToAdd.entrySet()) {

            ForecastSectionCoordinatesDto forecastSection = fs.getValue();

            ForecastSection newForecastSection = new ForecastSection(forecastSection.getNaturalId(), forecastSection.getName());
            forecastSectionRepository.saveAndFlush(newForecastSection);
            newForecastSection.addCoordinates(forecastSection.getCoordinates());
            forecastSections.put(fs.getValue().getNaturalId(), newForecastSection);
        }
    }

    private void updateForecastSections(Map<String, ForecastSection> forecastSections, Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate) {

        for (Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToUpdate.entrySet()) {
            ForecastSection forecastSection = forecastSections.get(fs.getValue().getNaturalId());
            forecastSection.setDescription(fs.getValue().getName());
            forecastSection.setObsoleteDate(null);
            forecastSection.addCoordinates(fs.getValue().getCoordinates());
        }
    }

    private void markForecastSectionsObsolete(Map<String, ForecastSection> forecastSections, Map<String, ForecastSection> forecastSectionsToDelete) {

        for (Map.Entry<String, ForecastSection> fs : forecastSectionsToDelete.entrySet()) {
            ForecastSection forecastSection = forecastSections.get(fs.getValue().getNaturalId());
            forecastSection.setObsoleteDate(Date.from(Instant.now()));
        }
    }

    private void printLogInfo(List<ForecastSectionCoordinatesDto> roadSectionCoordinates, List<ForecastSection> forecastSections) {

        int newCoordinatesCount = roadSectionCoordinates.stream().mapToInt(c -> c.getCoordinates().size()).sum();

        log.info("Updating forecast section coordinates. Number of coordinates in database: " + forecastSectionCoordinatesRepository.count() +
                 ". Number of coordinates received for update: " + newCoordinatesCount);
        List<String> externalNaturalIds = roadSectionCoordinates.stream().map(c -> c.getNaturalId()).collect(Collectors.toList());
        List<String> existingNaturalIds = forecastSections.stream().map(f -> f.getNaturalId()).collect(Collectors.toList());
        List<String> newForecastSectionNaturalIds = externalNaturalIds.stream().filter(n -> !existingNaturalIds.contains(n)).collect(Collectors.toList());
        List<String> missingForecastSectionNaturalIds = existingNaturalIds.stream().filter(n -> !externalNaturalIds.contains(n)).collect(Collectors.toList());
        log.info("Database is missing " + newForecastSectionNaturalIds.size() + " ForecastSections (naturalId): " + StringUtils.join(newForecastSectionNaturalIds, ", ") +
                 ". Update data is missing " + missingForecastSectionNaturalIds.size() + " ForecastSections (naturalId): " + StringUtils.join(missingForecastSectionNaturalIds, ", "));
    }
}
