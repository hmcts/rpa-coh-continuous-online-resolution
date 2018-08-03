package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;
import uk.gov.hmcts.reform.coh.repository.SessionEventForwardingStateRepository;

import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class SessionEventForwardingStateServiceTest {

    @Mock
    private SessionEventForwardingStateRepository sessionEventForwardingStateRepository;

    private SessionEventForwardingStateService sessionEventForwardingStateService;

    @Before
    public void setUp() {
        sessionEventForwardingStateService = new SessionEventForwardingStateService(sessionEventForwardingStateRepository);
    }

    @Test
    public void testRetrieveSessionEventForwardingStateReturnsState() {
        String forwardingStateName = "event_forwarding_pending";
        SessionEventForwardingState sessionEventForwardingState = new SessionEventForwardingState();
        sessionEventForwardingState.setForwardingStateName(forwardingStateName);
        given(sessionEventForwardingStateRepository.findByForwardingStateName(forwardingStateName)).willReturn(Optional.of(sessionEventForwardingState));

        Optional<SessionEventForwardingState> returnedState = sessionEventForwardingStateService.retrieveEventForwardingStateByName(forwardingStateName);
        assertTrue(returnedState.isPresent());
        assertEquals(forwardingStateName, returnedState.get().getForwardingStateName());
    }

}
