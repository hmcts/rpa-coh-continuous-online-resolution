insert into jurisdiction(jurisdiction_id, jurisdiction_name, url, max_question_rounds)
 select 1, 'SSCS', 'https://coh-cor-aat.service.core-compute-aat.internal:443/SSCS/notifications', 2
 where (select count(datname) from pg_database where datname='azure_maintenance') = 1
 AND not exists (select 1 from public.jurisdiction where jurisdiction_id = 1);

insert into jurisdiction(jurisdiction_id, jurisdiction_name, url, max_question_rounds)
select 1, 'SSCS', 'http://localhost:8080/SSCS/notifications', 2
where (select count(datname) from pg_database where datname='azure_maintenance') = 0
AND not exists (select 1 from public.jurisdiction where jurisdiction_id = 1);