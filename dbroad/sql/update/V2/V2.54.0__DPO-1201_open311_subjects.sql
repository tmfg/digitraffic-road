CREATE TABLE open311_subject (
     id INTEGER NOT NULL,
     active SMALLINT NOT NULL,
     name TEXT NOT NULL,
     locale CHARACTER VARYING (2) NOT NULL,
     PRIMARY KEY (id, locale)
);

ALTER TABLE open311_subject ADD CONSTRAINT open311_subject_locale_check CHECK (locale IN ('fi', 'sv', 'en'));
ALTER TABLE open311_subject ADD CONSTRAINT open311_subject_active_check CHECK (active IN (0, 1));

CREATE TABLE open311_subsubject (
     id INTEGER NOT NULL,
     subject_id SMALLINT NOT NULL,
     active SMALLINT NOT NULL,
     name TEXT NOT NULL,
     locale CHARACTER VARYING (2) NOT NULL,
     PRIMARY KEY (id, locale)
);

ALTER TABLE open311_subsubject ADD CONSTRAINT open311_subsubject_locale_check CHECK (locale IN ('fi', 'sv', 'en'));
ALTER TABLE open311_subsubject ADD CONSTRAINT open311_subsubject_active_check CHECK (active IN (0, 1));

-- keys were numbers after all
ALTER TABLE open311_service_request_state ALTER COLUMN key TYPE SMALLINT USING (key::SMALLINT);
