DROP TABLE open311_service_request_state;
CREATE TABLE open311_service_request_state (
    key SMALLINT NOT NULL,
    name TEXT NOT NULL,
    locale CHARACTER VARYING (2) NOT NULL,
    PRIMARY KEY (key, locale)
);
ALTER TABLE open311_service_request_state ADD CONSTRAINT open311_service_request_state_locale_check CHECK (locale IN ('fi', 'en'));
