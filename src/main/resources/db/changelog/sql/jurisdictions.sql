insert into jurisdiction(jurisdiction_id, jurisdiction_name, url)
 select 1, 'SSCS', 'http://localhost:8080/SSCS/notifications'
 where not exists (select 1 from public.jurisdiction where jurisdiction_id = 1);
