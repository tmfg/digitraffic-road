-- forecast_section.version (1 = simple, 2 = detailed road network) is replaced with a descriptive type.
-- Expand phase: version is kept and the application writes both columns, so the unique constraints can
-- still use version. Later: switch the constraints to type and drop version.
ALTER TABLE forecast_section ADD COLUMN type VARCHAR(50);

UPDATE forecast_section SET type = CASE version WHEN 1 THEN 'SIMPLE' ELSE 'DETAILED' END;

ALTER TABLE forecast_section ALTER COLUMN type SET NOT NULL;

CREATE INDEX forecast_section_type_modified_i ON forecast_section (type, modified);
CREATE INDEX forecast_section_type_natural_id_i ON forecast_section (type, natural_id);
