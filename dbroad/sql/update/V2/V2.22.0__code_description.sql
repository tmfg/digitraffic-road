CREATE TABLE IF NOT EXISTS code_description(
    domain CHARACTER VARYING(16) NOT NULL,
    code CHARACTER VARYING(8) NOT NULL,
    description CHARACTER VARYING(256) NOT NULL
);

ALTER TABLE code_description
ADD CONSTRAINT code_description_pk PRIMARY KEY (domain, code);

insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '111', 'mutka oikealle');
insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '112', 'mutka vasemmalle');
