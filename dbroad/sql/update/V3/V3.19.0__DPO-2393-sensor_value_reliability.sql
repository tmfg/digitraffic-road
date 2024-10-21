ALTER TABLE sensor_value
ADD COLUMN reliability TEXT;

ALTER TABLE sensor_value_history
  ADD COLUMN reliability TEXT;


ALTER TABLE sensor_value ADD CONSTRAINT sensor_value_reliability_check
  CHECK (reliability IN ('OK', 'SUSPICIOUS', 'FAULTY', 'UNKNOWN'));

ALTER TABLE sensor_value_history ADD CONSTRAINT sensor_value_history_reliability_check
  CHECK (reliability IN ('OK', 'SUSPICIOUS', 'FAULTY', 'UNKNOWN'));
