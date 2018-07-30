package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequestMapper;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.repository.DecisionRepository;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private DecisionStateService decisionStateService;

    private DecisionService decisionService;

    private Decision decision;

    private Decision newDecision;

    private DecisionState decisionState;

    private UUID uuid;

    @Before
    public void setup() throws IOException {

        uuid = UUID.randomUUID();

        DecisionRequest request = (DecisionRequest) JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);

        decisionState = new DecisionState();
        decisionState.setState("decision_drafted");
        decisionService = new DecisionService(decisionRepository);

        decision = new Decision();
        DecisionRequestMapper.map(request, decision, decisionState);

        newDecision = new Decision();
        DecisionRequestMapper.map(request, newDecision, decisionState);
        newDecision.setDecisionId(UUID.randomUUID());

        given(decisionStateService.retrieveDecisionStateByState("decision_drafted")).willReturn(Optional.ofNullable(decisionState));
    }

    @Test
    public void testCreateDecision() {
        when(decisionRepository.save(decision)).thenReturn(newDecision);
        assertEquals(newDecision, decisionService.createDecision(decision));
    }

    @Test
    public void testFindByOnlineHearingId() {
        when(decisionRepository.findByOnlineHearingOnlineHearingId(uuid)).thenReturn(Optional.ofNullable(decision));
        assertEquals(decision, decisionService.findByOnlineHearingId(uuid).get());
    }

    @Test
    public void testFindByOnlineHearingIdFail() {
        when(decisionRepository.findByOnlineHearingOnlineHearingId(uuid)).thenReturn(Optional.empty());
        assertFalse(decisionService.findByOnlineHearingId(UUID.randomUUID()).isPresent());
    }

    @Test
    public void testRetrieveByOnlineHearingIdAndDecisionId() {
        when(decisionRepository.findByOnlineHearingOnlineHearingIdAndDecisionId(uuid, uuid)).thenReturn(Optional.empty());
        assertFalse(decisionService.retrieveByOnlineHearingIdAndDecisionId(uuid, uuid).isPresent());
    }

    @Test
    public void testUpdateDecision() {
        when(decisionRepository.save(decision)).thenReturn(decision);
        assertEquals(decision, decisionService.updateDecision(decision));
    }

    @Test
    public void testDeleteDecisionById() {
        doNothing().when(decisionRepository).deleteById(uuid);
        decisionService.deleteDecisionById(uuid);
    }

    @Test
    public void testDeadlineExpiryDate() {
        DateFormat df = new SimpleDateFormat("yyyyMMDDHHmmss");
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date expiryDate = ExpiryCalendar.getDeadlineExpiryDate();

        assertTrue(df.format(calendar.getTime()).equalsIgnoreCase(df.format(expiryDate)));
    }
}