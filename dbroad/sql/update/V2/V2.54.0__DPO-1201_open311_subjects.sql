CREATE TABLE open311_subject (
     id SMALLINT NOT NULL,
     active SMALLINT NOT NULL,
     name TEXT NOT NULL,
     locale CHARACTER VARYING (2) NOT NULL,
     PRIMARY KEY (id, locale)
);

CREATE TABLE open311_subsubject (
     id SMALLINT NOT NULL,
     subject_id SMALLINT NOT NULL,
     active SMALLINT NOT NULL,
     name TEXT NOT NULL,
     locale CHARACTER VARYING (2) NOT NULL,
     PRIMARY KEY (id, locale)
);

-- keys were numbers after all
ALTER TABLE open311_service_request_state ALTER COLUMN key TYPE SMALLINT USING (key::SMALLINT);
