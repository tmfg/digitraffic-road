delete from maintenance_tracking
where domain in ('autori-oulu', 'autori-kuopio');

delete from maintenance_tracking_work_machine
where not exists (select null
                  from maintenance_tracking t
                  where t.work_machine_id = maintenance_tracking_work_machine.id);

delete from maintenance_tracking_domain_contract
where domain in ('autori-oulu', 'autori-kuopio');

delete from maintenance_tracking_domain_task_mapping
where domain in ('autori-oulu', 'autori-kuopio');

delete from  maintenance_tracking_domain
where name in ('autori-oulu', 'autori-kuopio');
