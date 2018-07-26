INSERT INTO public.decision_state(decision_state_id, state)
select 10, 'decision_drafted'
 where not exists (select 1 from public.decision_state where decision_state_id = 10);

INSERT INTO public.decision_state(decision_state_id, state)
select 20, 'decision_issue_pending'
 where not exists (select 1 from public.decision_state where decision_state_id = 20);

INSERT INTO public.decision_state(decision_state_id, state)
select 30, 'decision_issued'
 where not exists (select 1 from public.decision_state where decision_state_id = 30);

INSERT INTO public.decision_state(decision_state_id, state)
select 40, 'decision_rejected'
  where not exists (select 1 from public.decision_state where decision_state_id = 40);

INSERT INTO public.decision_state(decision_state_id, state)
select 50, 'decision_accepted'
  where not exists (select 1 from public.decision_state where decision_state_id = 50);