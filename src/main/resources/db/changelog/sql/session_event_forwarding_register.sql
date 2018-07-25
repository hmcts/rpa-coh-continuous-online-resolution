insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 1, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 1 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 2, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 2 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 3, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 3 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 4, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 4 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 5, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 5 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 6, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 6 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 7, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 7 and jurisdiction_id = 1);

insert into session_event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 8, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.session_event_forwarding_register where event_type_id = 8 and jurisdiction_id = 1);