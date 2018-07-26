INSERT INTO public.question_state(question_state_id, state)
select 1, 'question_drafted'
where not exists (select 1 from public.question_state where question_state_id = 1);

INSERT INTO public.question_state(question_state_id, state)
select 2, 'SUBMITTED' 
where not exists (select 1 from public.question_state where question_state_id = 2);

INSERT INTO public.question_state(question_state_id, state)
select 3, 'question_issued'
where not exists (select 1 from public.question_state where question_state_id = 3);

INSERT INTO public.question_state(question_state_id, state)
select 4, 'question_issued_pending'
where not exists (select 1 from public.question_state where question_state_id = 4);