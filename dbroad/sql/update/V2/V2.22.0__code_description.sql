CREATE TABLE IF NOT EXISTS code_description(
    domain CHARACTER VARYING(16) NOT NULL,
    code CHARACTER VARYING(8) NOT NULL,
    description CHARACTER VARYING(256) NOT NULL
);

ALTER TABLE code_description
ADD CONSTRAINT code_description_pk PRIMARY KEY (domain, code);

insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '133', 'Liikenneruuhka');
insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '142', 'Tietyö');
insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '144', 'Liukas ajorata');
insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '165', 'Liikennevalot');
insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '183', 'Sivutuuli');
insert into code_description(domain, code, description) values ('VARIABLE_SIGN', '189', 'Muu vaara');
