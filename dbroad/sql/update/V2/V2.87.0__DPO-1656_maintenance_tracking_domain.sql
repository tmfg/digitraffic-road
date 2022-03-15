INSERT into maintenance_tracking_domain(name, source)
values ('state-roads', 'Harja/Väylävirasto') on conflict (name) do nothing;

update maintenance_tracking_domain
  set source = 'Paikannin.com/Kuopion kaupunki'
where name = 'paikannin-kuopio';

update maintenance_tracking_domain_contract
  set source = 'Paikannin.com/Kuopion kaupunki'
where domain = 'paikannin-kuopio';

ALTER TABLE maintenance_tracking
ALTER COLUMN domain SET DEFAULT 'state-roads';

update maintenance_tracking
set domain = 'state-roads'
where domain = 'harja';

update maintenance_tracking_domain_contract
set domain = 'state-roads'
where domain = 'harja';

delete from maintenance_tracking_domain
where name = 'harja';

