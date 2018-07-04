insert into jurisdiction(jurisdiction_id, jurisdiction_name, url, max_question_rounds)
 select 1, 'SSCS', 'http://localhost:8080/SSCS/notifications', 2
 where not exists (select 1 from public.jurisdiction where jurisdiction_id = 1);