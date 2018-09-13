insert into session_event_type(event_type_id, event_type_name)
 select 1, 'question_round_issued'
 where not exists (select 1 from public.session_event_type where event_type_id = 1);

insert into session_event_type(event_type_id, event_type_name)
 select 2, 'question_deadline_elapsed'
 where not exists (select 1 from public.session_event_type where event_type_id = 2);

insert into session_event_type(event_type_id, event_type_name)
 select 3, 'question_deadline_extended'
 where not exists (select 1 from public.session_event_type where event_type_id = 3);

insert into session_event_type(event_type_id, event_type_name)
 select 4, 'question_deadline_extension_denied'
 where not exists (select 1 from public.session_event_type where event_type_id = 4);

insert into session_event_type(event_type_id, event_type_name)
 select 5, 'question_deadline_extension_granted'
 where not exists (select 1 from public.session_event_type where event_type_id = 5);

insert into session_event_type(event_type_id, event_type_name)
 select 6, 'answers_submitted'
 where not exists (select 1 from public.session_event_type where event_type_id = 6);

insert into session_event_type(event_type_id, event_type_name)
 select 7, 'decision_rejected'
 where not exists (select 1 from public.session_event_type where event_type_id = 7);

insert into session_event_type(event_type_id, event_type_name)
 select 8, 'decision_issued'
 where not exists (select 1 from public.session_event_type where event_type_id = 8);

insert into session_event_type(event_type_id, event_type_name)
 select 9, 'continuous_online_hearing_relisted'
 where not exists (select 1 from public.session_event_type where event_type_id = 9);
