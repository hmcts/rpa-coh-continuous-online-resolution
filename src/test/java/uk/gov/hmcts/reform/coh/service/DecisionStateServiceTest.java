package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.repository.DecisionStateRepository;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class DecisionStateServiceTest {

    @Mock
    private DecisionStateRepository decisionStateRepository;

    private DecisionStateService decisionStateService;

    private DecisionState decisionState;

    private static final String STATE = "decision_drafted";
    @Before
    public void setup() throws IOException {
        decisionStateService = new DecisionStateService(decisionStateRepository);

        decisionState = new DecisionState();
        decisionState.setState(STATE);

        given(decisionStateRepository.findByState(STATE)).willReturn(Optional.ofNullable(decisionState));
    }

    @Test
    public void testRetrieveDecisionStateByState() {
        assertEquals(decisionState, decisionStateService.retrieveDecisionStateByState(STATE).get());
    }

    @Test
    public void testRetrieveDecisionStateByStateFail() {
        given(decisionStateRepository.findByState(STATE)).willReturn(Optional.empty());
        assertFalse(decisionStateService.retrieveDecisionStateByState(STATE).isPresent());
    }
}