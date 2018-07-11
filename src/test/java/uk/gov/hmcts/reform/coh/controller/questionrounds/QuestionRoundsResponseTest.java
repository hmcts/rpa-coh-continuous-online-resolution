package uk.gov.hmcts.reform.coh.controller.questionrounds;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class QuestionRoundsResponseTest {

    private QuestionRoundsResponse questionRoundsResponse;

    @Before
    public void setup(){
        questionRoundsResponse = new QuestionRoundsResponse();
    }

    @Test
    public void testSetQuestionRounds() {
        questionRoundsResponse.setQuestionRounds(new ArrayList<>());
    }

    @Test
    public void testAddQuestionRoundResponse() {
        questionRoundsResponse.addQuestionRoundResponse(new QuestionRoundResponse());
        assertFalse(questionRoundsResponse.getQuestionRounds().isEmpty());
    }

    @Test
    public void testgetPreviousQuestionRound(){
        questionRoundsResponse.setPreviousQuestionRound(1);
        assertEquals(1, (int) questionRoundsResponse.getPreviousQuestionRound());
    }

    @Test
    public void testGetCurrentQuestionRound() {
        questionRoundsResponse.setCurrentQuestionRound(2);
        assertEquals(2, (int) questionRoundsResponse.getCurrentQuestionRound());
    }

    @Test
    public void testGetMaxQuestionRound() {
        questionRoundsResponse.setMaxQuestionRound(3);
        assertEquals(3, (int) questionRoundsResponse.getMaxQuestionRound());
    }

    @Test
    public void testGetNextQuestionRound() {
        questionRoundsResponse.setNextQuestionRound(3);
        assertEquals(3, (int) questionRoundsResponse.getNextQuestionRound());
    }
}
