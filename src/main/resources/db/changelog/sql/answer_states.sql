WITH upsert AS (
     UPDATE public.answer_state
	 SET state = 'answer_drafted'
     WHERE answer_state_id = 1
     RETURNING *
)
INSERT INTO public.answer_state(answer_state_id, state)
select 1, 'answer_drafted'
where not exists (select 1 from public.answer_state where answer_state_id = 1);

WITH upsert AS (
     UPDATE public.answer_state
	 SET state = 'answer_submitted'
     WHERE answer_state_id = 3
     RETURNING *
)
INSERT INTO public.answer_state(answer_state_id, state)
select 3, 'answer_submitted'
where not exists (select 1 from public.answer_state where answer_state_id = 3);
