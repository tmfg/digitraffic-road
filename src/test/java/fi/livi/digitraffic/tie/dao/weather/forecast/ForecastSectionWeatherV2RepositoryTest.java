package fi.livi.digitraffic.tie.dao.weather.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastSectionWeatherV2PK;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.ForecastTypeV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.OverallRoadConditionV2;
import fi.livi.digitraffic.tie.model.weather.forecast.v2.RoadConditionV2;
import jakarta.persistence.EntityManager;

public class ForecastSectionWeatherV2RepositoryTest extends AbstractServiceTest {

    @Autowired
    private ForecastSectionWeatherV2Repository repository;

    @Autowired
    private EntityManager entityManager;

    private long createForecastSection() {
        final Number id = (Number) entityManager.createNativeQuery("SELECT nextval('seq_forecast_section')").getSingleResult();
        entityManager.createNativeQuery("INSERT INTO forecast_section(id, natural_id, description, version, type) VALUES (:id, '99999_999_99999_2_0', 'test', 2, 'DETAILED')")
            .setParameter("id", id.longValue())
            .executeUpdate();
        return id.longValue();
    }

    private ForecastSectionWeatherV2 create(final long sectionId, final String name, final ForecastTypeV2 type) {
        final ForecastSectionWeatherV2 w = new ForecastSectionWeatherV2(new ForecastSectionWeatherV2PK(sectionId, name));
        w.setTime(Instant.parse("2026-10-02T12:00:00Z"));
        w.setType(type);
        w.setDaylight(true);
        w.setRoadTemperature(new BigDecimal("18.9"));
        w.setTemperature(new BigDecimal("13.3"));
        w.setWindSpeed(new BigDecimal("2.0"));
        w.setWindDirection(184);
        w.setOverallRoadCondition(OverallRoadConditionV2.NOT_KNOWN);
        w.setRoadCondition(RoadConditionV2.DRY);
        w.setWeatherSymbol(109);
        return w;
    }

    @Test
    public void saveAndReadAllFields() {
        final long sectionId = createForecastSection();
        repository.saveAndFlush(create(sectionId, "0h", ForecastTypeV2.OBSERVATION));
        entityManager.clear();

        final ForecastSectionWeatherV2 found = repository.findById(new ForecastSectionWeatherV2PK(sectionId, "0h")).orElseThrow();
        assertEquals(ForecastTypeV2.OBSERVATION, found.getType());
        assertEquals(Instant.parse("2026-10-02T12:00:00Z"), found.getTime());
        assertEquals(0, new BigDecimal("18.9").compareTo(found.getRoadTemperature()));
        assertEquals(0, new BigDecimal("13.3").compareTo(found.getTemperature()));
        assertEquals(184, found.getWindDirection());
        assertEquals(OverallRoadConditionV2.NOT_KNOWN, found.getOverallRoadCondition());
        assertEquals(RoadConditionV2.DRY, found.getRoadCondition());
        assertEquals(109, found.getWeatherSymbol());
        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getModifiedAt());
    }

    @Test
    public void nullableMeasurements() {
        final long sectionId = createForecastSection();
        final ForecastSectionWeatherV2 w = create(sectionId, "2h", ForecastTypeV2.FORECAST);
        w.setRoadTemperature(null);
        w.setWindSpeed(null);
        w.setOverallRoadCondition(null);
        w.setRoadCondition(null);
        w.setWeatherSymbol(null);
        repository.saveAndFlush(w);
        entityManager.clear();

        final ForecastSectionWeatherV2 found = repository.findById(new ForecastSectionWeatherV2PK(sectionId, "2h")).orElseThrow();
        assertNull(found.getRoadTemperature());
        assertNull(found.getRoadCondition());
        assertNull(found.getWeatherSymbol());
    }

    @Test
    public void findByIdsAndDeleteNotInNames() {
        final long sectionId = createForecastSection();
        repository.save(create(sectionId, "0h", ForecastTypeV2.OBSERVATION));
        repository.save(create(sectionId, "2h", ForecastTypeV2.FORECAST));
        repository.saveAndFlush(create(sectionId, "4h", ForecastTypeV2.FORECAST));

        assertEquals(3, repository.findByForecastSectionIds(List.of(sectionId)).size());
        assertEquals(1, repository.deleteByForecastSectionIdAndForecastNameNotIn(sectionId, List.of("0h", "2h")));
        entityManager.clear();
        assertEquals(2, repository.findByForecastSectionIds(List.of(sectionId)).size());
    }
}
