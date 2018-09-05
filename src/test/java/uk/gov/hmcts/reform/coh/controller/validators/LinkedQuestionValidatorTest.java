package uk.gov.hmcts.reform.coh.controller.validators;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import static junit.framework.TestCase.assertTrue;

public class LinkedQuestionValidatorTest {

    private QuestionRequest request;

    private OnlineHearing onlineHearing;

    private Validation validation;

    private LinkedQuestionValidator validator = new LinkedQuestionValidator();

    @Before
    public void setUp() throws Exception {
        request = JsonUtils.toObjectFromTestName("question/standard_question", QuestionRequest.class);
        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        validation = new Validation();
    }

    @Test
    public void testNullLinkedQuestions() {
        ValidationResult result = validation.execute(new BiValidator[] {validator}, onlineHearing, request);
        assertTrue(result.isValid());
    }
}