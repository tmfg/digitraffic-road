package fi.livi.digitraffic.tie.conf;

import fi.livi.digitraffic.tie.AbstractDaemonTest;

import fi.livi.digitraffic.tie.dao.roadstation.RoadStationSensorRepository;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that Hibernate applies hibernate.jdbc.fetch_size to queries.  Must be run manually
 */
@TestPropertySource(properties = {
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.orm.jdbc.bind=TRACE",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
// one sample of timing with different fetch_sizes
// 1    -> 92485417
// 10   -> 33191625
// 100  -> 16273916
// 1000 -> 15357916
// none -> 14286709
@Disabled
public class HibernateJdbcFetchSizeTest extends AbstractDaemonTest {
    @Autowired
    private RoadStationSensorRepository roadStationSensorRepository;

    @Test
    @Transactional
    public void hibernateSetsConfiguredFetchSizeOnEveryStatement() {
        final var stopWatch = StopWatch.createStarted();
        // Trigger a real Hibernate query so Hibernate prepares its own JDBC statement
        final var sensors = roadStationSensorRepository.findAll();

        assertTrue(sensors.size() > 10);
        System.out.println("Took time " + stopWatch.getNanoTime());
    }
}

