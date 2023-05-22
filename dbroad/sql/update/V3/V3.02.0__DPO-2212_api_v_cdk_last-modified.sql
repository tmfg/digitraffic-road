DO
$$
  BEGIN
    ALTER TABLE counting_site_counter RENAME COLUMN added_timestamp TO created;
  EXCEPTION
    WHEN undefined_column THEN RAISE NOTICE 'column counting_site_counter.added_timestamp already renamed, skipping';
  END;
$$;
ALTER TABLE counting_site_counter ALTER COLUMN created SET DEFAULT now();
ALTER TABLE counting_site_counter ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();

DO
$$
  BEGIN
    ALTER TABLE counting_site_domain RENAME COLUMN added_timestamp TO created;
  EXCEPTION
    WHEN undefined_column THEN RAISE NOTICE 'column counting_site_domain.added_timestamp already renamed, skipping';
  END;
$$;
ALTER TABLE counting_site_domain ALTER COLUMN created SET DEFAULT now();
ALTER TABLE counting_site_domain ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE counting_site_user_type ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();
ALTER TABLE counting_site_user_type ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE counting_site_data ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();
ALTER TABLE counting_site_data ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();

DO
$$
  BEGIN
    ALTER TABLE device_data_datex2 RENAME COLUMN updated_timestamp TO modified;
  EXCEPTION
    WHEN undefined_column THEN RAISE NOTICE 'column device_data_datex2.updated_timestamp already renamed, skipping';
  END;
$$;
ALTER TABLE device_data_datex2 ALTER COLUMN modified SET DEFAULT now();

DO
$$
  BEGIN
    ALTER TABLE device_data RENAME COLUMN created_date TO created;
  EXCEPTION
    WHEN undefined_column THEN RAISE NOTICE 'column device_data_datex2.updated_timestamp already renamed, skipping';
  END;
$$;
ALTER TABLE device_data ALTER COLUMN created SET DEFAULT now();
ALTER TABLE device_data ADD COLUMN IF NOT EXISTS modified TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();
ALTER TABLE device_data_datex2 ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();

DO
$$
  BEGIN
    ALTER TABLE device RENAME COLUMN updated_date TO modified;
  EXCEPTION
    WHEN undefined_column THEN RAISE NOTICE 'column device_data_datex2.updated_timestamp already renamed, skipping';
  END;
$$;
ALTER TABLE device ALTER COLUMN modified SET DEFAULT now();
ALTER TABLE device ADD COLUMN IF NOT EXISTS created TIMESTAMP(0) WITH TIME ZONE NOT NULL DEFAULT now();

CREATE INDEX ON device_data USING btree (modified);
CREATE INDEX ON counting_site_data USING btree (modified);
