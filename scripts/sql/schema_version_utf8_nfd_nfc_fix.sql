UPDATE "schema_version" SET "description" = REPLACE("description", UNISTR('\00E4'), 'a'), "script" = REPLACE("script", UNISTR('\00E4'), 'a');
UPDATE "schema_version" SET "description" = REPLACE("description", UNISTR('\00F6'), 'o'), "script" = REPLACE("script", UNISTR('\00F6'), 'o');

UPDATE "schema_version" SET "description" = REPLACE("description", 'a?', 'a'), "script" = REPLACE("script", 'a?', 'a');
UPDATE "schema_version" SET "description" = REPLACE("description", 'o?', 'o'), "script" = REPLACE("script", 'o?', 'o');