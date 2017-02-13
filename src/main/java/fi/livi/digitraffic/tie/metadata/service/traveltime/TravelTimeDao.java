package fi.livi.digitraffic.tie.metadata.service.traveltime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.data.service.TrafficFluencyService;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.ProcessedMeasurementDataDto;
import fi.livi.digitraffic.tie.metadata.service.traveltime.dto.ProcessedMedianDataDto;

@Repository
public class TravelTimeDao {

    private final JdbcTemplate jdbcTemplate;

    private final TrafficFluencyService trafficFluencyService;

    private final static int nobsFilteringLimit = 5;

    @Autowired
    public TravelTimeDao(final JdbcTemplate jdbcTemplate, final TrafficFluencyService trafficFluencyService) {
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

        jdbcTemplate.batchUpdate("INSERT INTO JOURNEYTIME_MEDIAN "
                                 + "(ID, END_TIMESTAMP, MEDIAN_TRAVEL_TIME, AVERAGE_SPEED, RATIO_TO_FREE_FLOW_SPEED, LINK_ID, NOBS)"
                                 + "VALUES (seq_journeytime_median.nextval, ?, ?, ?, ?, ?, ?)", new MedianBatchSetter(medians));
    }

    public void updateLatestMedianData(final List<ProcessedMedianDataDto> medians) {

        final String UPDATE_LATEST_MEDIANS_SQL = "MERGE INTO LATEST_JOURNEYTIME_MEDIAN m "
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

        jdbcTemplate.batchUpdate(UPDATE_LATEST_MEDIANS_SQL, new LatestMedianBatchSetter(medians));

        /*
         * Updates all latest medians that are not in an alert state (sets alert
         * start to null). Remove alerts only from links having enough observations.
         */
        final String UPDATE_FINISHED_ALERTS = "UPDATE LATEST_JOURNEYTIME_MEDIAN m SET "
                                              + "m.FLUENCY_ALERT_STARTED = NULL WHERE m.RATIO_TO_FREE_FLOW_SPEED > ? AND "
                                              + "m.NOBS > ?";

        /*
         * Updates all latest medians, that are in alert state, but haven't been
         * updated yet (alert start is null). Add alert only to links having enough observations.
         */
        final String UPDATE_NEW_ALERTS = "UPDATE LATEST_JOURNEYTIME_MEDIAN m SET "
                                         + "m.FLUENCY_ALERT_STARTED = m.END_TIMESTAMP WHERE m.FLUENCY_ALERT_STARTED IS NULL "
                                         + "AND m.RATIO_TO_FREE_FLOW_SPEED <= ? "
                                         + "AND m.NOBS > ?";

        // execute updates
        Object[] args = new Object[2];
        args[0] = trafficFluencyService.getAlertThreshold();
        args[1] = this.nobsFilteringLimit;

        int[] argTypes = new int[2];
        argTypes[0] = Types.DECIMAL;
        argTypes[1] = Types.INTEGER;

        jdbcTemplate.update(UPDATE_FINISHED_ALERTS, args, argTypes);
        jdbcTemplate.update(UPDATE_NEW_ALERTS, args, argTypes);
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
    private class MedianBatchSetter implements BatchPreparedStatementSetter {

        private Log log = LogFactory.getLog(getClass());

        private List<ProcessedMedianDataDto> medianDatas;

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

    /**
     * Batch setter for LATEST_JOURNEYTIME_MEDIAN table sql
     */
    private class LatestMedianBatchSetter implements BatchPreparedStatementSetter {

        private List<ProcessedMedianDataDto> medianDatas;

        public LatestMedianBatchSetter(List<ProcessedMedianDataDto> medianDatas) {
            this.medianDatas = medianDatas;
        }

        public int getBatchSize() {
            return medianDatas.size();
        }

        public void setValues(PreparedStatement ps, int i) throws SQLException {
            ProcessedMedianDataDto data = medianDatas.get(i);

            ps.setTimestamp(1, new Timestamp(data.periodEnd.getTime()));
            ps.setLong(2, data.medianTravelTime);
            ps.setBigDecimal(3, data.averageSpeed);
            ps.setBigDecimal(4, data.ratioToFreeFlowSpeed);
            ps.setLong(5, data.linkId);
            ps.setInt(6, data.nobs);
        }
    }

}
