package uk.gov.hmcts.reform.coh.controller.decision;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.DecisionStateHistory;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class DecisionResponseMapperTest {

    @Test
    public void testMappings() {
        DecisionState fooState = new DecisionState();
        fooState.setState("foo");

        DecisionState barState = new DecisionState();
        fooState.setState("bar");

        UUID onlineHearingUuid = UUID.randomUUID();
        UUID decisionUuid = UUID.randomUUID();

        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(onlineHearingUuid);

        Calendar yesterday = new GregorianCalendar();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Decision decision = new Decision();
        decision.setDecisionId(decisionUuid);
        decision.setOnlineHearing(onlineHearing);
        decision.setDecisionHeader("Decision header");
        decision.setDecisionText("Decision text");
        decision.setDecisionReason("Decision reason");
        decision.setDecisionAward("Decision award");
        decision.setDeadlineExpiryDate(new Date());
        decision.setDecisionstate(fooState);

        // Set up the history
        List<DecisionStateHistory> decisionStateHistories = new ArrayList<>();
        DecisionStateHistory history1 = new DecisionStateHistory(decision, barState);
        history1.setDateOccured(yesterday.getTime());
        DecisionStateHistory history2 = new DecisionStateHistory(decision, fooState);
        history2.setDateOccured(new Date());
        decisionStateHistories.add(history1);
        decisionStateHistories.add(history2);
        decision.setDecisionStateHistories(decisionStateHistories);

        DecisionResponse response = new DecisionResponse();
        DecisionResponseMapper.map(decision, response);

        // Check each field is mapped correctly
        assertEquals(decision.getDecisionId().toString(), response.getDecisionId());
        assertEquals(decision.getDecisionHeader(), response.getDecisionHeader());
        assertEquals(decision.getDecisionText(), response.getDecisionText());
        assertEquals(decision.getDecisionReason(), response.getDecisionReason());
        assertEquals(decision.getDecisionAward(), response.getDecisionAward());
        assertEquals(decision.getDeadlineExpiryDate().toString(), response.getDeadlineExpiryDate());
        assertEquals(decision.getDecisionstate().getState(), response.getDecisionState().getStateName());

        // This checks the sorting works
        assertEquals(history2.getDateOccured().toString(), response.getDecisionState().getStateDatetime());
    }
}
