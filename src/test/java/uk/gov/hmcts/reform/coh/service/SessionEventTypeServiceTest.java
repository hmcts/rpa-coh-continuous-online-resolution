package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class SessionEventTypeServiceTest {
     private final String DECISION_REJECTED = "decision_rejected";
     private final String DECISION_ISSUED = "decision_issued";

    @Mock
    private SessionEventTypeRespository sessionEventTypeRespository;

    private SessionEventTypeService sessionEventTypeService;

    private SessionEventType sessionEventType;

    @Before
    public void setup() throws IOException {

        sessionEventTypeService = new SessionEventTypeService(sessionEventTypeRespository);

        sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(DECISION_REJECTED);

        when(sessionEventTypeRespository.save(sessionEventType)).thenReturn(sessionEventType);

    }

    @Test
    public void testGetEventTypeWithName() {

        when(sessionEventTypeRespository.findByEventTypeName(DECISION_REJECTED)).thenReturn(Optional.ofNullable(sessionEventType));
        assertEquals(sessionEventType, sessionEventTypeRespository.findByEventTypeName(DECISION_REJECTED).get());
    }

    @Test
    public void testRetrieveQuestion() {
        when(sessionEventTypeRespository.findByEventTypeName(DECISION_REJECTED)).thenReturn(Optional.of(sessionEventType));

        Optional<SessionEventType> newSessionEventType = sessionEventTypeService.retrieveEventType(DECISION_REJECTED);
        verify(sessionEventTypeRespository, times(1)).findByEventTypeName(DECISION_REJECTED);
        assertEquals(sessionEventType, newSessionEventType.get());
    }

    @Test
    public void testFindByOnlineHearingIdFail() {
        when(sessionEventTypeRespository.findByEventTypeName(DECISION_ISSUED)).thenReturn(Optional.empty());
        assertFalse(sessionEventTypeService.retrieveEventType(DECISION_ISSUED).isPresent());
    }

}