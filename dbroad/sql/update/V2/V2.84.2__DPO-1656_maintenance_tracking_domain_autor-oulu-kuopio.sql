INSERT INTO maintenance_tracking_domain(name, source)
VALUES ('autori-oulu', 'Autori/Oulun kaupunki'),
       ('autori-kuopio', 'Autori/Kuopion kaupunki')
 ON CONFLICT (name) DO NOTHING;