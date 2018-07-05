package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class QuestionRoundServiceTest {

    private QuestionRoundService questionRoundService;

    @Mock
    private QuestionRepository questionRepository;

    @Before
    public void setup(){
        List<Question> questions = new ArrayList<>();
        Question question = new Question();
        question.setQuestionRound(2);
        questions.add(question);
        question = new Question();
        question.setQuestionRound(1);
        questions.add(question);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(questions);
        questionRoundService = new QuestionRoundService(questionRepository);
    }

    @Test
    public void testFindQuestionsOfOnlineHearingOrderedByQuestionRoundReturnsPopulatedList(){
        List<Question> questions = questionRoundService.getQuestionsOrderedByRound(new OnlineHearing());
        assertFalse(questions.isEmpty());
    }

    @Test
    public void testGetQuestionRoundReturnsHighestNumberFromQuestionList(){
        int questionRound = questionRoundService.getQuestionRoundNumber(new OnlineHearing());
        assertEquals(2, questionRound);
    }

    @Test
    public void testQuestionRoundMustBeZeroIfNoOtherQuestionsAreFound(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(3);

        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(new ArrayList<>());
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testGetQuestionRoundReturns1IfNoPreviousQuestionsFound(){
        given(questionRepository.findAllByOnlineHearingOrderByQuestionRoundDesc(any(OnlineHearing.class))).willReturn(new ArrayList<>());
        int questionRound = questionRoundService.getQuestionRoundNumber(new OnlineHearing());
        assertEquals(0, questionRound);
    }

    @Test
    public void testValidateQuestionsRoundWhenNoJurisdictionLimitSet(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(0);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundHappyPath(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundFailsWhenExceedingMaxQuestionRounds(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(4);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundCanBeIncremented(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(1);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundAcceptsSameQuestionRound(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(2);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIsNull(){
        OnlineHearing onlineHearing = new OnlineHearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(0);
        boolean valid = questionRoundService.validateQuestionRound(question, onlineHearing);
        assertTrue(valid);
    }
}
