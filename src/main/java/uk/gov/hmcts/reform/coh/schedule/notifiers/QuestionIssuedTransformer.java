package uk.gov.hmcts.reform.coh.schedule.notifiers; 
 
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;

import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.reform.coh.events.EventTypes.QUESTION_ROUND_ISSUED; 
 
@Component 
public class QuestionIssuedTransformer implements EventTransformer<OnlineHearing> {

    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    @Autowired
    private QuestionRoundService questionRoundService;
 
    @Override 
    public NotificationRequest transform(SessionEventType sessionEventType, OnlineHearing onlineHearing) {
        List<Question> qrQuestions = questionRoundService.getQuestionsByQuestionRound(onlineHearing, questionRoundService.getCurrentQuestionRoundNumber(onlineHearing));

        NotificationRequest request = new NotificationRequest();
        request.setCaseId(onlineHearing.getCaseId());
        request.setOnlineHearingId(onlineHearing.getOnlineHearingId());
        request.setEventType(QUESTION_ROUND_ISSUED.getEventType());
        if (qrQuestions.size() > 0) {
            request.setExpiryDate(df.format(qrQuestions.get(0).getDeadlineExpiryDate()));
        }
        return request; 
    } 
 
    @Override 
    public List<String> supports() { 
        return Arrays.asList( 
                QUESTION_ROUND_ISSUED.getEventType() 
        ); 
    } 
}