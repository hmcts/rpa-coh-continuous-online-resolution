package uk.gov.hmcts.reform.coh.schedule.notifiers; 
 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Component; 
import uk.gov.hmcts.reform.coh.domain.OnlineHearing; 
import uk.gov.hmcts.reform.coh.domain.Question; 
import uk.gov.hmcts.reform.coh.domain.QuestionState; 
import uk.gov.hmcts.reform.coh.domain.SessionEventType; 
import uk.gov.hmcts.reform.coh.service.QuestionService; 
import uk.gov.hmcts.reform.coh.service.QuestionStateService; 
import uk.gov.hmcts.reform.coh.states.QuestionStates; 
 
import java.util.Arrays; 
import java.util.List; 
import java.util.NoSuchElementException; 
import java.util.Optional; 
 
import static uk.gov.hmcts.reform.coh.events.EventTypes.QUESTION_ROUND_ISSUED; 
 
@Component 
public class QuestionIssuedTransformer implements EventTransformer<OnlineHearing> { 
 
    @Autowired 
    private QuestionService questionService; 
 
    @Autowired 
    private QuestionStateService questionStateService; 
 
    @Override 
    public NotificationRequest transform(SessionEventType sessionEventType, OnlineHearing onlineHearing) { 
 
        Optional<QuestionState> issuedPendingState = questionStateService.retrieveQuestionStateByStateName(QuestionStates.ISSUE_PENDING.getStateName()); 
        if(!issuedPendingState.isPresent()) { 
            throw new NoSuchElementException("Error: Required state not found."); 
        } 
 
        List<Question> onlineHearingQuestions = questionService.findAllQuestionsByOnlineHearingAndQuestionState(onlineHearing, issuedPendingState.get()); 
 
        NotificationRequest request = new NotificationRequest(); 
        request.setCaseId(onlineHearing.getCaseId()); 
        request.setOnlineHearingId(onlineHearing.getOnlineHearingId()); 
        request.setEventType(QUESTION_ROUND_ISSUED.getEventType()); 
        request.setExpiryDate(String.valueOf(onlineHearingQuestions.get(0).getDeadlineExpiryDate())); 
 
        System.out.println(request.toString()); 
        return request; 
    } 
 
    @Override 
    public List<String> supports() { 
        return Arrays.asList( 
                QUESTION_ROUND_ISSUED.getEventType() 
        ); 
    } 
}