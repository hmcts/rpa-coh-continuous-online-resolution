package uk.gov.hmcts.reform.coh.service;

        import org.junit.Before;
        import org.junit.Test;
        import org.junit.runner.RunWith;
        import org.mockito.Mock;
        import org.springframework.test.context.junit4.SpringRunner;
        import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
        import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
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
    private Onlinehearing onlinehearing;
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

        given(questionRepository.findAllByOnlinehearingOrderByQuestionRoundDesc(any(Onlinehearing.class))).willReturn(questions);
        questionRoundService = new QuestionRoundService(questionRepository);

        onlinehearing = new Onlinehearing();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlinehearing.setJurisdiction(jurisdiction);
    }

    @Test
    public void testFindQuestionsOfOnlinehearingOrderedByQuestionRoundReturnsPopulatedList(){
        List<Question> questions = questionRoundService.getQuestionsOrderedByRound(new Onlinehearing());
        assertFalse(questions.isEmpty());
    }

    @Test
    public void testGetQuestionRoundReturnsHighestNumberFromQuestionList(){
        int questionRound = questionRoundService.getQuestionRound(new Onlinehearing());
        assertEquals(2, questionRound);
    }

    @Test
    public void testQuestionRoundMustBeZeroIfNoOtherQuestionsAreFound(){
        Question question = new Question();
        question.setQuestionRound(3);

        given(questionRepository.findAllByOnlinehearingOrderByQuestionRoundDesc(any(Onlinehearing.class))).willReturn(new ArrayList<>());
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertFalse(valid);
    }

    @Test
    public void testGetQuestionRoundReturns1IfNoPreviousQuestionsFound(){
        given(questionRepository.findAllByOnlinehearingOrderByQuestionRoundDesc(any(Onlinehearing.class))).willReturn(new ArrayList<>());
        int questionRound = questionRoundService.getQuestionRound(new Onlinehearing());
        assertEquals(0, questionRound);
    }


    @Test
    public void testIsIncrementedReturnsTrueWhenOneToTwo(){
        boolean valid = questionRoundService.isIncremented(2, 1);
        assertTrue(valid);
    }

    @Test
    public void testIsIncrementedReturnsFalseWhenOneToThree(){
        boolean valid = questionRoundService.isIncremented(3, 1);
        assertFalse(valid);
    }

    @Test
    public void testIsIncrementedReturnsFalseWhenTwoToOne(){
        boolean valid = questionRoundService.isIncremented(1, 2);
        assertFalse(valid);
    }

    @Test
    public void testIsMaxRoundLimitIsTrueIfValueSetAboveZero() {
        boolean valid = questionRoundService.isMaxRoundLimit(1);
        assertTrue(valid);
    }

    @Test
    public void testIsMaxRoundLimitIsFalseIfValueSetToZero() {
        boolean valid = questionRoundService.isMaxRoundLimit(0);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionsRoundWhenNoJurisdictionLimitSet(){
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(0);
        onlinehearing.setJurisdiction(jurisdiction);

        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundHappyPath(){
        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertTrue(valid);
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundMustBeOne(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(1);

        given(questionRepository.findAllByOnlinehearingOrderByQuestionRoundDesc(any(Onlinehearing.class))).willReturn(questions);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertTrue(valid);
    }

    @Test
    public void testIfCurrentQuestionRoundIsZeroThenQuestionRoundCannotBeTwo(){
        List<Question> questions = new ArrayList<>();

        Question question = new Question();
        question.setQuestionRound(2);

        given(questionRepository.findAllByOnlinehearingOrderByQuestionRoundDesc(any(Onlinehearing.class))).willReturn(questions);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertFalse(valid);
    }
    @Test
    public void testValidateQuestionRoundFailsWhenExceedingMaxQuestionRounds(){
        Question question = new Question();
        question.setQuestionRound(4);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundIsTrueWhenIncrementedByOne(){
        Question question = new Question();
        question.setQuestionRound(3);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertTrue(valid);
    }

    @Test
    public void testValidateQuestionRoundIsFalseWhenSettingToPreviousQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(1);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertFalse(valid);
    }

    @Test
    public void testValidateQuestionRoundAcceptsSameQuestionRound(){
        Question question = new Question();
        question.setQuestionRound(2);
        boolean valid = questionRoundService.validateQuestionRound(question, onlinehearing);
        assertTrue(valid);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIs0(){
        Question question = new Question();
        question.setQuestionRound(0);
        questionRoundService.validateQuestionRound(question, onlinehearing);
    }
    @Test(expected = EntityNotFoundException.class)
    public void testValidateQuestionRoundThrowsErrorIfQuestionRoundIsNull(){
        questionRoundService.validateQuestionRound(new Question(), onlinehearing);
    }
}
