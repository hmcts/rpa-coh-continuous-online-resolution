package uk.gov.hmcts.reform.coh.controller.questionrounds;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.domain.QuestionRoundState;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class QuestionRoundResponseTest {

    private QuestionRoundResponse questionRoundResponse;
    private QuestionRoundState draftedQuestionRoundState;

    @Before
    public void setup(){
        questionRoundResponse = new QuestionRoundResponse();
        draftedQuestionRoundState = new QuestionRoundState();
        draftedQuestionRoundState.setState("DRAFTED");
        draftedQuestionRoundState.setStateId(1);
    }

    @Test
    public void testSetQuestionList() {
        questionRoundResponse.setQuestionList(new ArrayList<>());
    }

    @Test
    public void testAddQuestionResponse() {
        questionRoundResponse.addQuestionResponse(new QuestionResponse());
        assertFalse(questionRoundResponse.getQuestionList().isEmpty());
    }

    @Test
    public void testgetPreviousQuestionRound(){
        questionRoundResponse.setQuestionRound("1");
        assertEquals("1", questionRoundResponse.getQuestionRound());
    }

    @Test
    public void testGetCurrentQuestionRound() {
        questionRoundResponse.setQuestionRoundState(draftedQuestionRoundState);
        assertEquals(draftedQuestionRoundState.getState(), questionRoundResponse.getQuestionRoundState().getState());
    }
}
