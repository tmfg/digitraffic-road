package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinate;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinateList;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinateListPK;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSectionCoordinatePK;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v1.Coordinate;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.dto.v1.ForecastSectionCoordinatesDto;

@Service
public class ForecastSectionV1MetadataUpdater {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV1MetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;

    @Autowired
    public ForecastSectionV1MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionRepository forecastSectionRepository) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
    }

    /**
     * @return Returns true if one or more forecast sections were updated
     */
    @Transactional
    public boolean updateForecastSectionV1Metadata() {

        final List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = forecastSectionClient.getForecastSectionV1Metadata();

        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(1);
        final Set<String> existingForecastSections = forecastSections.stream().map(ForecastSection::getNaturalId).collect(Collectors.toSet());

        printLogInfo(forecastSectionCoordinates, forecastSections);

        final Map<String, ForecastSection> naturalIdToForecastSections = forecastSections.stream().collect(Collectors.toMap(ForecastSection::getNaturalId, fs -> fs));

        final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd = forecastSectionCoordinates.stream().filter(
                fs -> !existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSectionCoordinatesDto::getNaturalId, c -> c));

        final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate = forecastSectionCoordinates.stream().filter(
                fs -> existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSectionCoordinatesDto::getNaturalId, c -> c));

        final Set<String> receivedForecastSections = forecastSectionCoordinates.stream().map(ForecastSectionCoordinatesDto::getNaturalId).collect(Collectors.toSet());

        final Map<String, ForecastSection> forecastSectionsToDelete = forecastSections.stream().filter(
                fs -> !receivedForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSection::getNaturalId, c -> c));

        addForecastSections(naturalIdToForecastSections, forecastSectionsToAdd);
        boolean updated = updateForecastSections(naturalIdToForecastSections, forecastSectionsToUpdate);
        markForecastSectionsObsolete(naturalIdToForecastSections, forecastSectionsToDelete);

        forecastSectionRepository.saveAll(naturalIdToForecastSections.values());
        forecastSectionRepository.flush();
        return forecastSectionCoordinates.size() != existingForecastSections.size() || forecastSectionsToDelete.size() > 0 || updated;
    }

    private void addForecastSections(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd) {

        for (final Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToAdd.entrySet()) {

            final ForecastSectionCoordinatesDto forecastSection = fs.getValue();

            final ForecastSection newForecastSection = new ForecastSection(forecastSection.getNaturalId(), 1, forecastSection.getName());
            forecastSectionRepository.saveAndFlush(newForecastSection);
            newForecastSection.addCoordinates(forecastSection.getCoordinates());
            forecastSectionRepository.saveAndFlush(newForecastSection);
            forecastSections.put(fs.getValue().getNaturalId(), newForecastSection);
        }
    }

    private boolean updateForecastSections(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate) {

        boolean updated = false;
        for (final Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToUpdate.entrySet()) {
            final ForecastSection forecastSection = forecastSections.get(fs.getValue().getNaturalId());

            if (!forecastSection.corresponds(fs.getValue())) {
                log.info("Updating forecast section: " + forecastSection.toString() + " with data: " + fs.toString());
                updated = true;
            }
            forecastSection.setDescription(fs.getValue().getName());
            forecastSection.setObsoleteDate(null);

            forecastSection.removeCoordinateLists();
            forecastSectionRepository.saveAndFlush(forecastSection);

            addCoordinates(forecastSection, fs.getValue().getCoordinates());
        }
        return updated;
    }

    private void addCoordinates(final ForecastSection forecastSection, final List<Coordinate> coordinates) {
        final List<ForecastSectionCoordinate> coordinateList = new ArrayList<>();

        long orderNumber = 1;
        for (final Coordinate coordinate : coordinates) {
            if (!coordinate.isValid()) {
                log.info("Invalid coordinates for forecast section " + forecastSection.getNaturalId() + ". Coordinates were: " + coordinate.toString());
            } else {
                coordinateList.add(new ForecastSectionCoordinate(
                    new ForecastSectionCoordinatePK(forecastSection.getId(), 1L, orderNumber), coordinate.longitude, coordinate.latitude));
                orderNumber++;
            }
        }
        final ForecastSectionCoordinateList list =
            new ForecastSectionCoordinateList(new ForecastSectionCoordinateListPK(forecastSection.getId(), 1L), coordinateList);
        forecastSection.getForecastSectionCoordinateLists().add(list);
    }

    private void markForecastSectionsObsolete(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSection> forecastSectionsToDelete) {

        for (final Map.Entry<String, ForecastSection> fs : forecastSectionsToDelete.entrySet()) {
            final ForecastSection forecastSection = forecastSections.get(fs.getValue().getNaturalId());
            forecastSection.setObsoleteDate(Date.from(Instant.now()));
        }
    }

    private void printLogInfo(final List<ForecastSectionCoordinatesDto> roadSectionCoordinates, final List<ForecastSection> forecastSections) {

        log.info(String.format("Updating forecast sections. existingForecastSections=%s receivedForecastSections=%s",
                               forecastSections.size(), roadSectionCoordinates.size()));
        final List<String> externalNaturalIds = roadSectionCoordinates.stream().map(c -> c.getNaturalId()).collect(Collectors.toList());
        final List<String> existingNaturalIds = forecastSections.stream().map(f -> f.getNaturalId()).collect(Collectors.toList());
        final List<String> newForecastSectionNaturalIds = externalNaturalIds.stream().filter(n -> !existingNaturalIds.contains(n)).collect(Collectors.toList());
        final List<String> missingForecastSectionNaturalIds = existingNaturalIds.stream().filter(n -> !externalNaturalIds.contains(n)).collect(Collectors.toList());
        log.info(String.format("newForecastSections=%s with naturalIds=%s deletedForecastSections=%s with naturalIds=%s",
                               newForecastSectionNaturalIds.size(), StringUtils.join(newForecastSectionNaturalIds, ","),
                               missingForecastSectionNaturalIds.size(), StringUtils.join(missingForecastSectionNaturalIds, ",")));
    }
}
