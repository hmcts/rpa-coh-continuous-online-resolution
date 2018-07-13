insert into jurisdiction(jurisdiction_id, jurisdiction_name, url, max_question_rounds)
 select 1, 'SSCS', '${base-urls.test-url}/SSCS/notifications', 2
 where not exists (select 1 from public.jurisdiction where jurisdiction_id = 1);