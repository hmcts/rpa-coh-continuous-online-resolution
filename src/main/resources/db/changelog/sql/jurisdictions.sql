insert into jurisdiction(jurisdiction_id, jurisdiction_name, max_question_rounds)
 select 1, 'SSCS', 0
 where not exists (select 1 from public.jurisdiction where jurisdiction_id = 1);