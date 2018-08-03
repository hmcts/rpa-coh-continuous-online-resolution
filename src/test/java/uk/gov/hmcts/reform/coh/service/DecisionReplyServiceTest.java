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
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
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
    public void setup() throws IOException {
        DecisionReplyRequest request = (DecisionReplyRequest) JsonUtils.toObjectFromTestName("decision/standard_decision_reply", DecisionReplyRequest.class);

        decisionReply = new DecisionReply();
        Decision decision = new Decision();
        DecisionReplyRequestMapper.map(request, decisionReply, decision);

        given(decisionReplyRepository.save(any(DecisionReply.class))).willReturn(decisionReply);
        decisionReplyService = new DecisionReplyService(decisionReplyRepository);
    }

    @Test
    public void testCreateDecisionReply() {
        DecisionReply returnedDecisionReply = decisionReplyService.createDecision(decisionReply);
        verify(decisionReplyRepository, times(1)).save(any(DecisionReply.class));
        assertEquals(decisionReply, returnedDecisionReply);
    }
}
