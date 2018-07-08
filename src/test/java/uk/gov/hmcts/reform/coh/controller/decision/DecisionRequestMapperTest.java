package uk.gov.hmcts.reform.coh.controller.decision;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;

import static org.junit.Assert.assertEquals;

public class DecisionRequestMapperTest {

    @Test
    public void testMappings() {
        DecisionRequest request = new DecisionRequest();
        DecisionState state = new DecisionState();
        state.setState("foo");
        request.setDecisionHeader("Decision header");
        request.setDecisionText("Decision text");
        request.setDecisionReason("Decision reason");
        request.setDecisionAward("Decision award");

        Decision decision = new Decision();
        DecisionRequestMapper.map(request, decision, state);

        assertEquals(request.getDecisionHeader(), decision.getDecisionHeader());
        assertEquals(request.getDecisionText(), decision.getDecisionText());
        assertEquals(request.getDecisionReason(), decision.getDecisionReason());
        assertEquals(request.getDecisionAward(), decision.getDecisionAward());
        assertEquals("foo", decision.getDecisionstate().getState());
    }
}
