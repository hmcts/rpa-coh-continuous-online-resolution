INSERT INTO public.decision_state(decision_state_id, state)
select 1, 'decision_drafted'
 where not exists (select 1 from public.decision_state where decision_state_id = 1);

INSERT INTO public.decision_state(decision_state_id, state)
select 3, 'decision_issued'
 where not exists (select 1 from public.decision_state where decision_state_id = 3);

INSERT INTO public.decision_state(decision_state_id, state)
select 4, 'decision_rejected'
  where not exists (select 1 from public.decision_state where decision_state_id = 4);

INSERT INTO public.decision_state(decision_state_id, state)
select 5, 'decision_accepted'
  where not exists (select 1 from public.decision_state where decision_state_id = 5);