package fi.livi.digitraffic.tie.data.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.service.TrafficFluencyService;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.ProcessedMeasurementDataDto;
import fi.livi.digitraffic.tie.data.service.traveltime.dto.ProcessedMedianDataDto;

@Repository
public class TravelTimeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final TrafficFluencyService trafficFluencyService;

    private static final int NOBS_FILTERING_LIMIT = 5;

    private static final String UPDATE_LATEST_MEDIANS_SQL =
          "MERGE INTO LATEST_JOURNEYTIME_MEDIAN m "
        + "USING ("
        + "SELECT ? end_timestamp, "
        + "? median_travel_time, "
        + "? average_speed, "
        + "? ratio_to_free_flow_speed, "
        + "? link_id, "
        + "? nobs "
        + "FROM dual) static_values "
        + "ON (m.link_id = static_values.link_id) "
        + "WHEN NOT MATCHED THEN "
        + "INSERT (m.id, m.end_timestamp, m.median_travel_time, "
        + "m.average_speed, m.ratio_to_free_flow_speed, m.link_id, m.nobs) "
        + "VALUES (seq_latest_journeytime_median.nextval, "
        + "static_values.end_timestamp, static_values.median_travel_time, "
        + "static_values.average_speed, static_values.ratio_to_free_flow_speed, "
        + "static_values.link_id, static_values.nobs) "
        + "WHEN MATCHED THEN "
        + "UPDATE SET m.end_timestamp = static_values.end_timestamp, "
        + "m.median_travel_time = static_values.median_travel_time, "
        + "m.average_speed = static_values.average_speed, "
        + "m.ratio_to_free_flow_speed = static_values.ratio_to_free_flow_speed, "
        + "m.nobs = static_values.nobs";

    private static final String INSERT_MEDIAN_SQL =
          "MERGE INTO JOURNEYTIME_MEDIAN m "
        + "USING ("
        + "SELECT ? end_timestamp, "
        + "? median_travel_time, "
        + "? average_speed, "
        + "? ratio_to_free_flow_speed, "
        + "? link_id, "
        + "? nobs "
        + "FROM dual) static_values "
        + "ON (m.link_id = static_values.link_id AND m.end_timestamp = static_values.end_timestamp) "
        + "WHEN NOT MATCHED THEN "
        + "INSERT (m.id, m.end_timestamp, m.median_travel_time, "
        + "m.average_speed, m.ratio_to_free_flow_speed, m.link_id, m.nobs) "
        + "VALUES (seq_journeytime_median.nextval, "
        + "static_values.end_timestamp, static_values.median_travel_time, "
        + "static_values.average_speed, static_values.ratio_to_free_flow_speed, "
        + "static_values.link_id, static_values.nobs)";

    /*
     * Updates all latest medians that are not in an alert state (sets alert
    * start to null). Remove alerts only from links having enough observations.
    */
    private static final String UPDATE_FINISHED_ALERTS = "UPDATE LATEST_JOURNEYTIME_MEDIAN m SET "
        + "m.FLUENCY_ALERT_STARTED = NULL WHERE m.RATIO_TO_FREE_FLOW_SPEED > ? AND "
        + "m.NOBS > ?";

    /*
     * Updates all latest medians, that are in alert state, but haven't been
     * updated yet (alert start is null). Add alert only to links having enough observations.
     */
    private static final String UPDATE_NEW_ALERTS = "UPDATE LATEST_JOURNEYTIME_MEDIAN m SET "
        + "m.FLUENCY_ALERT_STARTED = m.END_TIMESTAMP WHERE m.FLUENCY_ALERT_STARTED IS NULL "
        + "AND m.RATIO_TO_FREE_FLOW_SPEED <= ? "
        + "AND m.NOBS > ?";

    @Autowired
    public TravelTimeRepository(final JdbcTemplate jdbcTemplate, final TrafficFluencyService trafficFluencyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.trafficFluencyService = trafficFluencyService;
    }

    /**
     * Insert individual measurements to JOURNEYTIME_MEASUREMENT
     */
    public void insertMeasurementData(final List<ProcessedMeasurementDataDto> measurementDatas) {

        jdbcTemplate.batchUpdate("INSERT INTO JOURNEYTIME_MEASUREMENT (ID, END_TIMESTAMP, TRAVEL_TIME, LINK_ID) "
                                 + "VALUES (seq_journeytime_measurement.nextval, ?, ?, ?) ", new MeasurementBatchSetter(measurementDatas));
    }

    /**
     * Insert processed median data (avg speeds etc) to JOURNEYTIME_MEDIAN table
     */
    public void insertMedianData(final List<ProcessedMedianDataDto> medians) {

        jdbcTemplate.batchUpdate(INSERT_MEDIAN_SQL, new MedianBatchSetter(medians));
    }

    public void updateLatestMedianData(final List<ProcessedMedianDataDto> medians) {
        jdbcTemplate.batchUpdate(UPDATE_LATEST_MEDIANS_SQL, new MedianBatchSetter(medians));
        jdbcTemplate.update(UPDATE_FINISHED_ALERTS, trafficFluencyService.getAlertThreshold(), NOBS_FILTERING_LIMIT);
        jdbcTemplate.update(UPDATE_NEW_ALERTS, trafficFluencyService.getAlertThreshold(), NOBS_FILTERING_LIMIT);
    }

    /**
     * Batch setter for JOURNEYTIME_MEASUREMENT table sql
     */
    private class MeasurementBatchSetter implements BatchPreparedStatementSetter {

        private List<ProcessedMeasurementDataDto> measurementDatas;

        public MeasurementBatchSetter(final List<ProcessedMeasurementDataDto> measurementDatas) {
            this.measurementDatas = measurementDatas;
        }

        public int getBatchSize() {
            return measurementDatas.size();
        }

        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ProcessedMeasurementDataDto data = measurementDatas.get(i);

            ps.setTimestamp(1, new Timestamp(data.endTimestamp.getTime()));
            ps.setLong(2, data.travelTime);
            ps.setLong(3, data.linkId);
        }
    }

    /**
     * Batch setter for JOURNEYTIME_MEDIAN table sql
     */
    private static class MedianBatchSetter implements BatchPreparedStatementSetter {

        private static final Logger log = LoggerFactory.getLogger(MedianBatchSetter.class);

        private final List<ProcessedMedianDataDto> medianDatas;

        public MedianBatchSetter(List<ProcessedMedianDataDto> medianDatas) {
            this.medianDatas = medianDatas;
        }

        public int getBatchSize() {
            return medianDatas.size();
        }

        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ProcessedMedianDataDto data = medianDatas.get(i);

            log.debug("inserting median: " + data);
            ps.setTimestamp(1, new Timestamp(data.periodEnd.getTime()));
            ps.setLong(2, data.medianTravelTime);
            ps.setBigDecimal(3, data.averageSpeed);
            ps.setBigDecimal(4, data.ratioToFreeFlowSpeed);
            ps.setLong(5, data.linkId);
            ps.setInt(6, data.nobs);
        }
    }
}
