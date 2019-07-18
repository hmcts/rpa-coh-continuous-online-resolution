
UPDATE public.session_event SET retries=0
WHERE EVNT_ID IN (SELECT event_type_id FROM public.session_event_type WHERE event_type_name IN ('question_round_issued', 'decision_issued'))
  AND JURS_ID=(SELECT jurisdiction_id FROM public.jurisdiction WHERE jurisdiction_name='SSCS')
  AND forwarding_state_id IN (SELECT forwarding_state_id FROM public.session_event_forwarding_state WHERE forwarding_state_name IN ('event_forwarding_pending', 'event_forwarding_failed'));