package uk.gov.hmcts.reform.coh.controller.questionrounds;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionRoundState;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionRoundResponseMapperTest {

    private QuestionRound qr;

    @Before
    public void setUp() {
        qr = new QuestionRound();
        qr.setQuestionRoundNumber(1);
        qr.setQuestionRoundState(new QuestionRoundState(new QuestionState("foo")));
        List<Question> questions = new ArrayList<>();
        questions.add(QuestionEntityUtils.createTestQuestion());
        Question question = QuestionEntityUtils.createTestQuestion();
        question.setDeadlineExtCount(2);
        questions.add(question);
        qr.setQuestionList(questions);
    }

    @Test
    public void testMapper() {
        QuestionRoundResponse qrr = new QuestionRoundResponse();
        QuestionRoundResponseMapper.map(qr, qrr);
        assertEquals("1", qrr.getQuestionRound());
        assertEquals(2, qrr.getQuestionList().size());
        assertEquals(new Integer(2), qrr.getDeadlineExtCount());
        assertEquals(1, qrr.getQuestionList().get(0).getDeadlineExtCount());
        assertEquals(2, qrr.getQuestionList().get(1).getDeadlineExtCount());
    }
}