ALTER TABLE open311_service DROP CONSTRAINT open311_service_service_type_check;
ALTER TABLE open311_service ALTER COLUMN type DROP NOT NULL;
ALTER TABLE open311_service ALTER COLUMN keywords DROP NOT NULL;
ALTER TABLE open311_service ALTER COLUMN "group" DROP NOT NULL;
