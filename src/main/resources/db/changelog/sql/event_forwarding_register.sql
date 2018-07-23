insert into event_forwarding_register(event_type_id, jurisdiction_id, forwarding_endpoint, maximum_retries)
select 1, 1, '${base-urls.test-url}/SSCS/notifications', 3
where not exists (select 1 from public.event_forwarding_register where event_type_id = 1)
and not exists (select 1 from public.event_forwarding_register where jurisdiction_id = 1);