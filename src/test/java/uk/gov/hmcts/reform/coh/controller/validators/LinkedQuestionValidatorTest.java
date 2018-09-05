package uk.gov.hmcts.reform.coh.controller.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class LinkedQuestionValidatorTest {

    private QuestionRequest request;

    private OnlineHearing onlineHearing;

    private Question question;

    private Validation validation;

    private BiValidator[] validators;

    private Set<UUID> linkedQuestions;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private LinkedQuestionValidator validator;

    @Before
    public void setUp() throws Exception {
        request = JsonUtils.toObjectFromTestName("question/standard_question", QuestionRequest.class);
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        question = QuestionEntityUtils.createTestQuestion();
        question.setOnlineHearing(onlineHearing);
        validation = new Validation();
        validators = new BiValidator[] {validator};
        linkedQuestions = new HashSet<>();
        linkedQuestions.add(UUID.randomUUID());

        when(questionRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(question));
    }

    @Test
    public void testNullLinkedQuestions() {
        ValidationResult result = validation.execute(validators, onlineHearing, request);
        assertTrue(result.isValid());
    }

    @Test
    public void testLinkedQuestions() {
        request.setLinkedQuestionId(linkedQuestions);
        ValidationResult result = validation.execute(validators, onlineHearing, request);
        assertTrue(result.isValid());
    }

    @Test
    public void testLinkedQuestionsNotFound() {
        when(questionRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        request.setLinkedQuestionId(linkedQuestions);
        ValidationResult result = validation.execute(validators, onlineHearing, request);
        assertFalse(result.isValid());
    }

    @Test
    public void testRandomLinkedQuestions() {
        request.setLinkedQuestionId(linkedQuestions);
        ValidationResult result = validation.execute(validators, OnlineHearingEntityUtils.createTestOnlineHearingEntity(), request);
        assertFalse(result.isValid());
    }

}