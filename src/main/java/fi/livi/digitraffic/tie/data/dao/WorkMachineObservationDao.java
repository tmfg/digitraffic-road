package fi.livi.digitraffic.tie.data.dao;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WorkMachineObservationDao {
    private static final Logger log = LoggerFactory.getLogger(WorkMachineObservationDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @PersistenceContext
    @Autowired
    EntityManager entityManager;

    private static final String INSERT_WITH_OBSERVATION_TIME =
            "INSERT INTO WORK_MACHINE_OBSERVATION_COORDINATE (work_machine_observation_id, order_number, longitude, latitude, observation_time)\n" +
            "SELECT :observationId , COALESCE(MAX(order_number) + 1, 0), :longitude, :latitude, :observationTime\n" +
            "FROM WORK_MACHINE_OBSERVATION_COORDINATE\n" +
            "WHERE work_machine_observation_id=:observationId";

    private static final String INSERT_WITHOUT_OBSERVATION_TIME =
            "INSERT INTO WORK_MACHINE_OBSERVATION_COORDINATE (work_machine_observation_id, order_number, longitude, latitude, observation_time)\n" +
            "SELECT :observationId , COALESCE(MAX(order_number) + 1, 0), :longitude, :latitude, null\n" +
            "FROM WORK_MACHINE_OBSERVATION_COORDINATE\n" +
            "WHERE work_machine_observation_id=:observationId";


    @Autowired
    public WorkMachineObservationDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addCoordinates(final long observationId,
                              final BigDecimal longitude,
                              final BigDecimal latitude,
                              final ZonedDateTime observationTime) {

        // TODO static final string x 2 versiota

        Query query = entityManager.createNativeQuery(observationTime != null ?
                                                      INSERT_WITH_OBSERVATION_TIME: INSERT_WITHOUT_OBSERVATION_TIME);

        query.setParameter("observationId", observationId);
        query.setParameter("longitude", longitude);
        query.setParameter("latitude", latitude);
        // Workaround for a bug in Hibernate
        // https://stackoverflow.com/questions/8211195/postgresql-jdbc-null-string-taken-as-a-bytea
        // Caused by: org.postgresql.util.PSQLException: ERROR: column "observation_time" is of type timestamp with time zone but expression is of type bytea
        // Hint: You will need to rewrite or cast the expression.
        if (observationTime != null) {
            query.setParameter("observationTime", observationTime);
        }
        return query.executeUpdate();
    }
}
