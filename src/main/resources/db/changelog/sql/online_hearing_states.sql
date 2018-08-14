INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 1, 'continuous_online_hearing_started'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 1);

INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 4, 'continuous_online_hearing_decision_issued'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 4);

INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 5, 'continuous_online_hearing_resolved'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 5);

INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 6, 'continuous_online_hearing_relisted'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 6);

INSERT INTO public.online_hearing_state(online_hearing_state_id, state)
select 7, 'continuous_online_hearing_closed'
where not exists (select 1 from public.online_hearing_state where online_hearing_state_id = 7);
