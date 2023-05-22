-- Modified column functions
-- Updates modified column only when data has changed
CREATE OR REPLACE FUNCTION update_modified_column()
  RETURNS TRIGGER AS
$$
BEGIN
  IF (to_jsonb(OLD.*) <> to_jsonb(NEW.*)) THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Updates modified column also when data has not changed
CREATE OR REPLACE FUNCTION update_modified_column_always()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.modified = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Updates modified column only when data is changed, excluding changes to pic_last_modified-field
CREATE OR REPLACE FUNCTION update_camera_preset_modified_column()
  RETURNS TRIGGER AS
$$
BEGIN
  -- remove field from json with -
  IF (to_jsonb(OLD.*) - 'pic_last_modified') <> (to_jsonb(NEW.*) - 'pic_last_modified') THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_counting_site_counter_modified_column()
  RETURNS TRIGGER AS
$$
BEGIN
  -- remove field from json with -
  IF (to_jsonb(OLD.*) - 'last_data_timestamp') <> (to_jsonb(NEW.*) - 'last_data_timestamp') THEN
    NEW.modified = now();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Delete prevention function
CREATE OR REPLACE FUNCTION raise_prevent_delete_exception()
  RETURNS TRIGGER AS
$$
BEGIN
  RAISE USING hint = -20100, message = 'You can not delete ' ||  TG_TABLE_NAME, detail = 'User-defined exception';
END;
$$
  LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_camera_preset_publishable()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.publishable :=
    CASE
      WHEN (NEW.obsolete_date IS NULL AND NEW.is_public = true) THEN true
      ELSE false
      END;
  RETURN NEW;
END;
$$
  LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION update_road_station_publishable()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.publishable :=
    CASE
      WHEN (NEW.obsolete_date IS NULL AND NEW.collection_status <> 'REMOVED_PERMANENTLY' AND NEW.is_public = true) THEN true
      ELSE false
      END;
  RETURN NEW;
END;
$$
  LANGUAGE plpgsql;



-- Function to generate new sequence number on every insert per preset history
CREATE OR REPLACE FUNCTION update_camera_preset_history_preset_seq_column()
  RETURNS TRIGGER AS
$$
DECLARE
  _preset_seq BIGINT;
BEGIN
  SELECT MAX(preset_seq) + 1 INTO _preset_seq FROM camera_preset_history WHERE preset_id = NEW.preset_id;
  NEW.preset_seq := coalesce(_preset_seq, 1);
  NEW.preset_seq_prev := coalesce(_preset_seq, 1) - 1;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Function that updates effective_end_time
CREATE OR REPLACE FUNCTION update_datex2_situation_record_effective_end_time()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.effective_end_time =
    case
      when NEW.life_cycle_management_canceled = true then NEW.version_time
      when NEW.validy_status = 'ACTIVE' then 'infinity'::timestamp without time zone
      when NEW.validy_status = 'DEFINED_BY_VALIDITY_TIME_SPEC' then coalesce(NEW.overall_end_time, 'infinity'::timestamp without time zone)
      when NEW.validy_status = 'SUSPENDED' then coalesce(NEW.overall_end_time, NEW.version_time)
      end;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION update_forecast_section_weather_type()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.type :=
    CASE NEW.forecast_name
      WHEN '0h' THEN 'OBSERVATION'
      ELSE 'FORECAST'
      END;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION road_station_sensor_publishable()
  RETURNS TRIGGER AS
$$
BEGIN
  NEW.PUBLISHABLE :=
    CASE
      WHEN (NEW.IS_PUBLIC IS true AND NEW.OBSOLETE_DATE IS NULL) THEN true
      ELSE false
      END;
  RETURN NEW;
END;
$$
  LANGUAGE plpgsql;
