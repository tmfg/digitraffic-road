package fi.livi.digitraffic.tie.dao.weather.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSection;
import fi.livi.digitraffic.tie.model.weather.forecast.ForecastSectionType;
import jakarta.persistence.EntityManager;

public class ForecastSectionRepositoryTest extends AbstractServiceTest {

    private static final String TEST_ROAD = "99998_";

    @Autowired
    private ForecastSectionRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void findByTypeReturnsOnlyCurrentSectionsOfTheType() {
        repository.saveAndFlush(new ForecastSection(TEST_ROAD + "002_00000_2_0", ForecastSectionType.DETAILED, "detailed 2"));
        repository.saveAndFlush(new ForecastSection(TEST_ROAD + "001_00000_2_0", ForecastSectionType.DETAILED, "detailed 1"));
        repository.saveAndFlush(new ForecastSection(TEST_ROAD + "003_00000_1_0", ForecastSectionType.SIMPLE, "simple"));
        final ForecastSection obsolete = new ForecastSection(TEST_ROAD + "004_00000_2_0", ForecastSectionType.DETAILED, "obsolete");
        obsolete.setObsoleteDate(new java.util.Date());
        repository.saveAndFlush(obsolete);
        entityManager.clear();

        assertEquals(List.of(TEST_ROAD + "001_00000_2_0", TEST_ROAD + "002_00000_2_0"),
            naturalIdsOfTestRoad(ForecastSectionType.DETAILED));
        assertEquals(List.of(TEST_ROAD + "003_00000_1_0"), naturalIdsOfTestRoad(ForecastSectionType.SIMPLE));
    }

    private List<String> naturalIdsOfTestRoad(final ForecastSectionType type) {
        return repository.findDistinctByTypeIsAndObsoleteDateIsNullOrderByNaturalIdAsc(type).stream()
            .map(ForecastSection::getNaturalId)
            .filter(id -> id.startsWith(TEST_ROAD))
            .toList();
    }
}
