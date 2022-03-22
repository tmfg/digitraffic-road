DELETE FROM maintenance_tracking_domain_task_mapping
  WHERE maintenance_tracking_domain_task_mapping.domain = 'autori-oulu';

INSERT INTO maintenance_tracking_domain_task_mapping (name, original_id, info, ignore, domain)
VALUES ('PLOUGHING_AND_SLUSH_REMOVAL', '2a1ae628-fa9f-4c04-be6a-87b11ce27c50','Auraus ja sohjonpoisto', false, 'autori-oulu'),
       ('LEVELLING_OF_ROAD_SURFACE', '702606c7-2439-4f63-9d3f-d368715e7c83','Polanteen poisto ja pinnan tasaus', false, 'autori-oulu'),
       ('TRANSFER_OF_SNOW', '004cb2f8-8695-4b3b-9c96-cbedf9727ff4','Lumensiirto', false, 'autori-oulu'),
       ('LINE_SANDING', '53efec80-7e16-4fc0-8e38-5d138477702d','Hiekoitus', false, 'autori-oulu'),
       ('SALTING', '576a64f6-c7bc-4876-bc49-f44628eef062','Suolaus', false, 'autori-oulu'),
       ('DUST_BINDING_OF_GRAVEL_ROAD_SURFACE', '098b259d-f81e-4e66-8142-c43bbeb7aa3a','Hiekoitusmateriaalin poisto ja pölynsidonta\r\n', false, 'autori-oulu'),
       ('SNOW_PLOUGHING_STICKS_AND_SNOW_FENCES', '78afc463-4ed6-4548-81d7-b5123f08a360','Aurausviitoitus\r\n', false, 'autori-oulu'),
       ('OTHER', '813996f2-8004-4c9b-a099-923ecbc991ba','Hiihtoladut ja kuntopolut', false, 'autori-oulu'),
       ('OTHER', '625d08de-dedc-482c-8c4f-fdde75713de9','Jätehuolto', false, 'autori-oulu'),
       ('OTHER', 'c9710ce7-92c7-4582-a826-bc791fd8cf17','Kuljetus', false, 'autori-oulu'),
       ('OTHER', '86972bc4-005a-4d4f-8c80-da6f253069c7','Siirtoajo', true, 'autori-oulu'),
       ('OTHER', '44a46ede-363c-4fc0-803a-8badc3f0551b','Tuntityö', false, 'autori-oulu'),
       ('OTHER', '642cec5c-5657-4df7-b3e7-aca8851ecb26','Muu', false, 'autori-oulu'),
       ('CLIENTS_QUALITY_CONTROL', '1232ce32-7000-4c3d-94f5-869da1dae18a','Laaduntarkastus', false, 'autori-oulu'),
       ('OTHER', '7475c864-e62e-4c50-a20e-00564bad79d0','Kevyenliikenteenväylä', false, 'autori-oulu'),
       ('MECHANICAL_CUT', 'a5513fce-dc0e-4964-9a45-5ba68873fa24','Tienvarsien niitto/raivaus', false, 'autori-oulu'),
       ('LEVELLING_GRAVEL_ROAD_SURFACE', 'e89fa3a1-8495-4aee-baba-8ff7539af2c9','Sorateiden tasaus (höyläys, lanaus)', false, 'autori-oulu'),
       ('DUST_BINDING_OF_GRAVEL_ROAD_SURFACE', '9d04e8c0-c8fc-4ab9-833a-8e364fdebda7','Sorateiden pölynsidonta', false, 'autori-oulu'),
       ('SPREADING_OF_CRUSH', 'a8852e82-97f3-4b90-873f-4422008104fc','Sorastus', false, 'autori-oulu'),
       ('OTHER', '90f87580-c65d-4696-8a4f-1e739920a377','Ulkoilureittien kunnossapito', false, 'autori-oulu'),
       ('BRUSHING', '0dc73fde-3897-40b3-ad66-129d850d97cf','Harjaus', false, 'autori-oulu'),
       ('DITCHING', '59c329da-79b0-4e7d-aabb-b7a80639ddd2','Ojitus', false, 'autori-oulu'),
       ('OTHER', '72f63b64-ce24-4ba5-a99f-9353162082ef','Roskankeräys', false, 'autori-oulu'),
       ('BRUSHING', '6bea7a9a-6e68-4cc8-a576-097873587c30','Viherkaistojen harjaus', false, 'autori-oulu'),
       ('OTHER', '87ba5ce5-2504-40bf-a072-f8dd7904523b','Nurmikon leikkaus', false, 'autori-oulu')
 ON CONFLICT (domain, original_id) DO NOTHING;
