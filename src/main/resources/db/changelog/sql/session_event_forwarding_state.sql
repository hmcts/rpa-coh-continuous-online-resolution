INSERT INTO public.session_event_forwarding_state(forwarding_state_id, forwarding_state_name)
select 1, 'event_forwarding_pending'
where not exists (select 1 from public.session_event_forwarding_state where forwarding_state_id = 1);

INSERT INTO public.session_event_forwarding_state(forwarding_state_id, forwarding_state_name)
select 2, 'event_forwarding_success'
where not exists (select 1 from public.session_event_forwarding_state where forwarding_state_id = 2);

INSERT INTO public.session_event_forwarding_state(forwarding_state_id, forwarding_state_name)
select 3, 'event_forwarding_failed'
where not exists (select 1 from public.session_event_forwarding_state where forwarding_state_id = 3);