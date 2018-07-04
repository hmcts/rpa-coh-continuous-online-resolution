INSERT INTO public.answer_state(answer_state_id, state)
select 1, 'DRAFTED'
where not exists (select 1 from public.answer_state where answer_state_id = 1);

INSERT INTO public.answer_state(answer_state_id, state)
select 2, 'answer_edited'
where not exists (select 1 from public.answer_state where answer_state_id = 2);

INSERT INTO public.answer_state(answer_state_id, state)
select 3, 'SUBMITTED'
where not exists (select 1 from public.answer_state where answer_state_id = 3);
