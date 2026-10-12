-- Last-Modified and /api/info/v1/update-times for traffic messages by type and for traffic data
CREATE INDEX IF NOT EXISTS data_datex2_situation_latest_modified_i
    ON data_datex2_situation(situation_type, modified_at)
    WHERE is_latest_version = true;

CREATE INDEX IF NOT EXISTS datex2_rtti_latest_modified_i
    ON datex2_rtti(modified_at)
    WHERE is_latest_version = true;
