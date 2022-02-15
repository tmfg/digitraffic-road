-- insert know contracts
insert into maintenance_tracking_domain_contract (domain, contract, name, source, start_date, end_date)
values  ('autori-oulu', 'd8fa02f3-e834-4088-8ce5-81cfeb883324', 'Oulunsalo', 'Autori/Oulun kaupunki', '2020-09-01T00:00:00.000Z', '2026-10-01T00:00:00Z'),
        ('autori-oulu', '4fac86b1-b1d3-485d-9237-5005417178f4', 'Ritaharju - Kuivasjärvi', 'Autori/Oulun kaupunki', '2020-09-01T00:00:00Z', '2026-10-01T00:00:00Z'),
        ('autori-oulu', 'ef60e0ea-112c-44c3-9890-5c4bfbf25fd6', 'Keskusta kehä, pohjoinen', 'Autori/Oulun kaupunki', '2020-09-01T00:00:00Z', '2026-10-01T00:00:00Z'),
        ('autori-oulu', '6c129e92-5b2e-4d4a-9008-1216f06a8a75', 'Maikkula-Madekoski-Kaakkuri', 'Autori/Oulun kaupunki', '2020-09-01T00:00:00Z', '2026-10-01T00:00:00Z'),
        ('autori-oulu', '35977052-291e-412d-b425-ec78147ffbcb', 'Keskusta kehä, eteläinen', 'Autori/Oulun kaupunki', '2020-09-01T00:00:00Z', '2026-10-01T00:00:00Z'),
        ('autori-kuopio', '3059f1ef-d3c1-4e66-b60f-e8b646685c89', 'Kuopion kaupunki', 'Autori/Kuopion kaupunki', '2016-01-10T00:00:00Z', '2022-09-30T23:00:00Z')
on conflict (domain, contract) do nothing;

-- set source values for contracts
update maintenance_tracking_domain_contract
set source = 'Autori/Oulun kaupunki'
where domain = 'autori-oulu';

update maintenance_tracking_domain_contract
set source = 'Autori/Kuopion kaupunki'
where domain = 'autori-kuopio';

update maintenance_tracking_domain
  set source = 'Harja/Väylävirasto'
where name = 'harja';