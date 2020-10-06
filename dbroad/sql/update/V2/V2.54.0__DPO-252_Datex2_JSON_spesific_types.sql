ALTER TABLE datex2 ADD COLUMN IF NOT EXISTS detailed_message_type TEXT;
CREATE INDEX datex2_detailed_type_import_i ON datex2(detailed_message_type, import_date);
