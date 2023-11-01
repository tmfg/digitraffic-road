package fi.livi.digitraffic.tie.service.weather.forecast;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.Coordinate;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.helper.PostgisGeometryUtils;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSection;
import fi.livi.digitraffic.tie.service.DataStatusService;

@ConditionalOnNotWebApplication
@Service
public class ForecastSectionV1MetadataUpdater {

    private static final Logger log = LoggerFactory.getLogger(ForecastSectionV1MetadataUpdater.class);

    private final ForecastSectionClient forecastSectionClient;

    private final ForecastSectionRepository forecastSectionRepository;
    private final DataStatusService dataStatusService;

    @Autowired
    public ForecastSectionV1MetadataUpdater(final ForecastSectionClient forecastSectionClient,
                                            final ForecastSectionRepository forecastSectionRepository,
                                            final DataStatusService dataStatusService) {
        this.forecastSectionClient = forecastSectionClient;
        this.forecastSectionRepository = forecastSectionRepository;
        this.dataStatusService = dataStatusService;
    }

    /**
     * @return Returns true if one or more forecast sections were updated
     */
    @Transactional
    public boolean updateForecastSectionV1Metadata() {

        final List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = forecastSectionClient.getForecastSectionV1Metadata();

        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsAndObsoleteDateIsNullOrderByNaturalIdAsc(1);
        final Set<String> existingForecastSections = forecastSections.stream().map(ForecastSection::getNaturalId).collect(Collectors.toSet());

        printLogInfo(forecastSectionCoordinates, forecastSections);

        final Map<String, ForecastSection> naturalIdToForecastSections = forecastSections.stream().collect(Collectors.toMap(ForecastSection::getNaturalId, fs -> fs));

        final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd = forecastSectionCoordinates.stream().filter(
                fs -> !existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSectionCoordinatesDto::getNaturalId, c -> c));

        final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate = forecastSectionCoordinates.stream().filter(
                fs -> existingForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSectionCoordinatesDto::getNaturalId, c -> c));

        final Set<String> receivedForecastSections = forecastSectionCoordinates.stream().map(ForecastSectionCoordinatesDto::getNaturalId).collect(Collectors.toSet());
        // 00001_001_000_0, 00001_006_000_0
        final Map<String, ForecastSection> forecastSectionsToDelete = forecastSections.stream().filter(
                fs -> !receivedForecastSections.contains(fs.getNaturalId())).collect(Collectors.toMap(ForecastSection::getNaturalId, c -> c));

        addForecastSections(naturalIdToForecastSections, forecastSectionsToAdd);
        boolean updated = updateForecastSections(naturalIdToForecastSections, forecastSectionsToUpdate);
        markForecastSectionsObsolete(naturalIdToForecastSections, forecastSectionsToDelete);

        forecastSectionRepository.saveAll(naturalIdToForecastSections.values());
        forecastSectionRepository.flush();

        final boolean metadataUpdated = forecastSectionCoordinates.size() != existingForecastSections.size() || !forecastSectionsToDelete.isEmpty() || updated;

        if (metadataUpdated) {
            dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA);
        }
        dataStatusService.updateDataUpdated(DataType.FORECAST_SECTION_METADATA_CHECK);

        return metadataUpdated;
    }

    private void addForecastSections(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToAdd) {

        for (final Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToAdd.entrySet()) {

            final ForecastSectionCoordinatesDto forecastSection = fs.getValue();

            final ForecastSection newForecastSection = new ForecastSection(forecastSection.getNaturalId(), 1, forecastSection.getName());
            updateGeometry(newForecastSection, fs.getValue().getCoordinates());
            forecastSectionRepository.saveAndFlush(newForecastSection);
            forecastSections.put(fs.getValue().getNaturalId(), newForecastSection);
        }
    }

    /**
     *
     * @param forecastSection which geometry to update
     * @param coordinates coordinates to use as geometry
     * @return true if geometry was updated
     */
    private boolean updateGeometry(final ForecastSection forecastSection, final List<Coordinate> coordinates) {
        final Geometry oldGeometry = forecastSection.getGeometry();
        if (coordinates != null && !coordinates.isEmpty()) {
            final List<org.locationtech.jts.geom.Coordinate> dbCoords = coordinates.stream().filter(Coordinate::isValid)
                .map(c -> new org.locationtech.jts.geom.Coordinate(Objects.requireNonNull(c.longitude).doubleValue(),
                                                                   Objects.requireNonNull(c.latitude).doubleValue()))
                .collect(Collectors.toList());
            if (dbCoords.isEmpty()) {
                forecastSection.setGeometry(null);
                forecastSection.setGeometrySimplified(forecastSection.getGeometry());
            } else if (dbCoords.size() == 1) {
                forecastSection.setGeometry(PostgisGeometryUtils.createPointWithZ(dbCoords.get(0)));
                forecastSection.setGeometrySimplified(forecastSection.getGeometry());
            } else {
                forecastSection.setGeometry(PostgisGeometryUtils.createLineStringWithZ(dbCoords));
                forecastSection.setGeometrySimplified(PostgisGeometryUtils.simplify(forecastSection.getGeometry()));
            }
        } else {
            forecastSection.setGeometry(null);
        }
        return !Objects.equals(oldGeometry, forecastSection.getGeometry());
    }

    private boolean updateForecastSections(final Map<String, ForecastSection> forecastSections, final Map<String, ForecastSectionCoordinatesDto> forecastSectionsToUpdate) {

        boolean updated = false;
        for (final Map.Entry<String, ForecastSectionCoordinatesDto> fs : forecastSectionsToUpdate.entrySet()) {
            final ForecastSection to = forecastSections.get(fs.getValue().getNaturalId());
            final ForecastSectionCoordinatesDto from = fs.getValue();

            to.setDescription(from.getName());
            to.setObsoleteDate(null);

            final boolean changed = updateGeometry(to, from.getCoordinates());
            if ( changed || !Objects.equals(from.getName(), to.getDescription()) ) {
                log.info("Updating forecastSection: " + to + " with forecastSectionData: " + fs);
                updated = true;
            }
            forecastSectionRepository.saveAndFlush(to);
        }
        return updated;
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
        final List<String> externalNaturalIds = roadSectionCoordinates.stream().map(ForecastSectionCoordinatesDto::getNaturalId).toList();
        final List<String> existingNaturalIds = forecastSections.stream().map(ForecastSection::getNaturalId).toList();
        final List<String> newForecastSectionNaturalIds = externalNaturalIds.stream().filter(n -> !existingNaturalIds.contains(n)).collect(Collectors.toList());
        final List<String> missingForecastSectionNaturalIds = existingNaturalIds.stream().filter(n -> !externalNaturalIds.contains(n)).collect(Collectors.toList());
        log.info(String.format("newForecastSections=%s with naturalIds=%s deletedForecastSections=%s with naturalIds=%s",
                               newForecastSectionNaturalIds.size(), StringUtils.join(newForecastSectionNaturalIds, ","),
                               missingForecastSectionNaturalIds.size(), StringUtils.join(missingForecastSectionNaturalIds, ",")));
    }
}
