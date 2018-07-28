package uk.gov.hmcts.reform.coh.controller.decision;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.DecisionStateHistory;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DecisionResponseMapperTest {

    private Decision decision;

    private DecisionStateHistory history2;

    @Before
    public void setUp() {
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

        decision = new Decision();
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
        history2 = new DecisionStateHistory(decision, fooState);
        history2.setDateOccured(new Date());
        decisionStateHistories.add(history1);
        decisionStateHistories.add(history2);
        decision.setDecisionStateHistories(decisionStateHistories);
    }

    @Test
    public void testMappings() {

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

    @Test
    public void testMappingNullExpiryDate() {
        decision.setDeadlineExpiryDate(null);
        DecisionResponse response = new DecisionResponse();
        DecisionResponseMapper.map(decision, response);
        assertNull(response.getDeadlineExpiryDate());
    }

    @Test
    public void testMappingsNullDecisionStateHistories() {
        DecisionState fooState = new DecisionState();
        fooState.setState("foo");

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

        DecisionResponse response = new DecisionResponse();
        DecisionResponseMapper.map(decision, response);


        // This checks the sorting works
        assertNull(response.getDecisionState().getStateDatetime());
    }
}
