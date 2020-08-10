create unique index forecast_section_version_natural_id_key on forecast_section(version, natural_id, id);
drop index forecast_section_natural_version_id_key;