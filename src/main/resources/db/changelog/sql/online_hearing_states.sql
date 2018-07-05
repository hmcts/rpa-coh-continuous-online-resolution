INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 1, 'continuous_online_hearing_started'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 1);

INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 2, 'continuous_online_hearing_questions_issued'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 2);

INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 3, 'continuous_online_hearing_answers_sent'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 3);
