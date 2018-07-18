INSERT INTO public.answer_state(answer_state_id, state)
select 1, 'answer_drafted'
where not exists (select 1 from public.answer_state where answer_state_id = 1);

INSERT INTO public.answer_state(answer_state_id, state)
select 3, 'answer_submitted'
where not exists (select 1 from public.answer_state where answer_state_id = 3);
