CREATE SEQUENCE IF NOT EXISTS permit_id_seq;

CREATE TABLE IF NOT EXISTS permit
(
  id             INTEGER DEFAULT NEXTVAL('permit_id_seq') PRIMARY KEY,
  source_id      TEXT                     NOT NULL,
  source         TEXT                     NOT NULL,
  permit_type    TEXT                     NOT NULL,
  permit_subject TEXT,
  geometry       GEOMETRY                 NOT NULL,
  effective_from TIMESTAMP WITH TIME ZONE NOT NULL,
  effective_to   TIMESTAMP WITH TIME ZONE,
  created        TIMESTAMP WITH TIME ZONE NOT NULL,
  modified       TIMESTAMP WITH TIME ZONE NOT NULL,
  version        INTEGER DEFAULT 1        NOT NULL,
  removed        BOOLEAN DEFAULT FALSE    NOT NULL,
  UNIQUE (source_id, source)
);
