package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyRequestMapper;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.repository.DecisionReplyRepository;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class DecisionReplyServiceTest {

    @Mock
    private DecisionReplyRepository decisionReplyRepository;

    private DecisionReply decisionReply;
    private DecisionReplyService decisionReplyService;

    @Before
    public void setUp() throws IOException {
        DecisionReplyRequest request = (DecisionReplyRequest) JsonUtils.toObjectFromTestName("decision/standard_decision_reply", DecisionReplyRequest.class);

        decisionReply = new DecisionReply();
        Decision decision = new Decision();
        DecisionReplyRequestMapper.map(request, decisionReply, decision);

        given(decisionReplyRepository.save(any(DecisionReply.class))).willReturn(decisionReply);
        given(decisionReplyRepository.findById(any(UUID.class))).willReturn(Optional.of(decisionReply));
        decisionReplyService = new DecisionReplyService(decisionReplyRepository);
    }

    @Test
    public void testCreateDecisionReply() {
        DecisionReply returnedDecisionReply = decisionReplyService.createDecision(decisionReply);
        verify(decisionReplyRepository, times(1)).save(any(DecisionReply.class));
        assertEquals(decisionReply, returnedDecisionReply);
    }

    @Test
    public void testFindDecisionReplyById() {
        Optional<DecisionReply> returnedDecisionReply = decisionReplyService.findByDecisionReplyId(UUID.randomUUID());
        verify(decisionReplyRepository, times(1)).findById(any(UUID.class));

        assertTrue(returnedDecisionReply.isPresent());
        assertEquals(decisionReply, returnedDecisionReply.get());
    }
}
