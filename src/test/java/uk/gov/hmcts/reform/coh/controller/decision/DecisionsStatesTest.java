package uk.gov.hmcts.reform.coh.controller.decision;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;

import static org.junit.Assert.assertEquals;

public class DecisionsStatesTest {

    @Test
    public void testDecisionsStatesEnums() {
        assertEquals("decision_drafted",DecisionsStates.DECISION_DRAFTED.getStateName());
        assertEquals("decision_issued",DecisionsStates.DECISION_ISSUED.getStateName());
        assertEquals("decision_accepted",DecisionsStates.DECISIONS_ACCEPTED.getStateName());
        assertEquals("decision_rejected",DecisionsStates.DECISIONS_REJECTED.getStateName());
    }
}
