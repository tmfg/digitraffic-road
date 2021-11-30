-- This has been driven to test and prod as text column -> convert them to json afterwards on lines below.
ALTER TABLE datex2 ADD COLUMN IF NOT EXISTS original_json_message JSON;

-- This is ok to drive also to already converted columns.
ALTER TABLE datex2 ALTER COLUMN original_json_message TYPE JSON USING original_json_message::JSON;
ALTER TABLE datex2 ALTER COLUMN json_message TYPE JSON USING json_message::JSON;