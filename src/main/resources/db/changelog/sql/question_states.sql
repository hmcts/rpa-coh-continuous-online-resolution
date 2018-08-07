INSERT INTO public.question_state(question_state_id, state)
select 10, 'question_drafted'
where not exists (select 1 from public.question_state where question_state_id = 10);

INSERT INTO public.question_state(question_state_id, state)
select 20, 'question_issue_pending'
where not exists (select 1 from public.question_state where question_state_id = 20);

INSERT INTO public.question_state(question_state_id, state)
select 30, 'question_issued'
where not exists (select 1 from public.question_state where question_state_id = 30);

INSERT INTO public.question_state(question_state_id, state)
select 35, 'question_answered'
where not exists (select 1 from public.question_state where question_state_id = 35);

INSERT INTO public.question_state(question_state_id, state)
select 40, 'question_deadline_elapsed'
where not exists (select 1 from public.question_state where question_state_id = 40);

INSERT INTO public.question_state(question_state_id, state)
select 50, 'question_extension_requested'
where not exists (select 1 from public.question_state where question_state_id = 50);

INSERT INTO public.question_state(question_state_id, state)
select 60, 'question_deadline_extension_denied'
where not exists (select 1 from public.question_state where question_state_id = 60);

INSERT INTO public.question_state(question_state_id, state)
select 70, 'question_deadline_extension_granted'
where not exists (select 1 from public.question_state where question_state_id = 70);