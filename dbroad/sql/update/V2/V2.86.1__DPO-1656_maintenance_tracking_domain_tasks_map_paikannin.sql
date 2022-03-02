INSERT INTO maintenance_tracking_domain(name, source)
VALUES ('paikannin-kuopio', 'GSGroup/Kuopion kaupunki')
ON CONFLICT (name) DO NOTHING;

insert into maintenance_tracking_domain_contract (domain, contract, name, source, start_date, end_date)
values  ('paikannin-kuopio', 'paikannin-kuopio', 'paikannin-kuopio', 'paikannin.com/Kuopion kaupunki', '2022-01-01T00:00:00.000Z', null)
on conflict (domain, contract) do nothing;

-- Map values by text value of original paikannin values as there are tasks with same names
-- but different id's for distinct machines
INSERT INTO maintenance_tracking_domain_task_mapping (name, original_id, info, domain, ignore)
VALUES ('LEVELLING_OF_ROAD_SURFACE', 'Alusterä','Alusterä', 'paikannin-kuopio', false),
       ('PLOUGHING_AND_SLUSH_REMOVAL', 'Auraus','Auraus', 'paikannin-kuopio', false),
       ('BRUSHING', 'Harjaus','Harjaus', 'paikannin-kuopio', false),
       ('LINE_SANDING', 'Hiekoitus','Hiekoitus', 'paikannin-kuopio', false),
       ('OTHER', 'Käynti','Käynti', 'paikannin-kuopio', true),
       ('OTHER', 'Kuormaus','Kuormaus', 'paikannin-kuopio', true),
       ('OTHER', 'Latu','Latu', 'paikannin-kuopio', true),
       ('OTHER', 'Työajo','Työajo', 'paikannin-kuopio', true),
       ('OTHER', 'Valvonta','Valvonta', 'paikannin-kuopio', true)
 ON CONFLICT (domain, original_id) DO NOTHING;
