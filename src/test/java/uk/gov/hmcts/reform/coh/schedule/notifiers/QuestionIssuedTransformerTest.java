package uk.gov.hmcts.reform.coh.schedule.notifiers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.events.EventTypes;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.states.QuestionStates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class QuestionIssuedTransformerTest {

    @Mock
    private QuestionRoundService questionRoundService;

    @InjectMocks
    private QuestionIssuedTransformer questionIssuedTransformer;


    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    private SessionEventType sessionEventType;
    private UUID uuid;
    private OnlineHearing onlineHearing;
    private Date expiryDeadline;

    @Before
    public void setUp() throws ParseException {
        sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName(EventTypes.QUESTION_ROUND_ISSUED.getEventType());

        uuid = UUID.randomUUID();
        Date date = new Date();
        onlineHearing = new OnlineHearing();
        onlineHearing.setCaseId("foo");
        onlineHearing.setOnlineHearingId(uuid);
        onlineHearing.setEndDate(date);

        QuestionState issuedPending = new QuestionState();
        issuedPending.setState(QuestionStates.ISSUE_PENDING.getStateName());
        issuedPending.setQuestionStateId(20);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        expiryDeadline = dateFormat.parse("2018-08-07 00:59:59");

        List<Question> questionRound = new ArrayList<>();
        Question question = new Question();
        question.setQuestionState(issuedPending);
        question.setQuestionRound(1);
        question.setDeadlineExpiryDate(expiryDeadline);
        questionRound.add(question);
        given(questionRoundService.getQuestionsByQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(questionRound);
    }

    @Test
    public void testMapping() {
        NotificationRequest request = questionIssuedTransformer.transform(sessionEventType, onlineHearing);

        assertEquals(df.format(expiryDeadline), request.getExpiryDate());
        assertEquals("foo", request.getCaseId());
        assertEquals(uuid, request.getOnlineHearingId());
        assertEquals(EventTypes.QUESTION_ROUND_ISSUED.getEventType(), request.getEventType());
    }

    @Test
    public void testSupportsExpectedEventType() {
        List<String> supports = questionIssuedTransformer.supports();
        assertTrue(supports.contains(EventTypes.QUESTION_ROUND_ISSUED.getEventType()));
    }
}
