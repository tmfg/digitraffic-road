package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dao.RoadSectionCoordinatesRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import fi.livi.digitraffic.tie.metadata.model.RoadSectionCoordinates;
import fi.livi.digitraffic.tie.metadata.model.RoadSectionCoordinatesPK;
import fi.livi.digitraffic.tie.metadata.service.forecastsection.ForecastSectionNaturalIdHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

        List<RoadSectionCoordinatesDto> coordinates = roadConditionsClient.getRoadSections();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll();

        roadSectionCoordinatesRepository.deleteAllInBatch();

        for (ForecastSection forecastSection : forecastSections) {

            BigDecimal roadNumAndSectionNum = ForecastSectionNaturalIdHelper.getRoadNumAndSectionNum(forecastSection.getNaturalId());

            Optional<RoadSectionCoordinatesDto> coordinatesDto = coordinates.stream()
                    .filter(c -> ForecastSectionNaturalIdHelper.getRoadNumAndSectionNum(c.getNaturalId()).equals(roadNumAndSectionNum))
                    .findFirst();

            if (coordinatesDto.isPresent()) {
                forecastSection.getRoadSectionCoordinates().clear();
                long orderNumber = 1;
                for (Pair<BigDecimal, BigDecimal> coords : coordinatesDto.get().getCoordinates()) {
                    forecastSection.getRoadSectionCoordinates().add(
                            new RoadSectionCoordinates(forecastSection, new RoadSectionCoordinatesPK(forecastSection.getId(), orderNumber), coords.getLeft(), coords.getRight()));
                    orderNumber++;
                }
            } else {
                log.debug("ForecastNaturalIdMismatch while saving road section coordinates. Forecast section with naturalId: " +
                        forecastSection.getNaturalId() + " does not exist");
            }
        }
        forecastSectionRepository.save(forecastSections);
        forecastSectionRepository.flush();
    }
}
