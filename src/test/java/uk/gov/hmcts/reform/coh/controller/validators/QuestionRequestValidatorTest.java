package uk.gov.hmcts.reform.coh.controller.validators;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class QuestionRequestValidatorTest {

    QuestionRequest request;

    Validation validation;

    @Before
    public void setup() throws IOException {
        request = JsonUtils.toObjectFromTestName("question/standard_question", QuestionRequest.class);
        validation = new Validation();
    }

    @Test
    public void testValidator() {
        assertTrue(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNonNumericQuestionRound() {
        request.setQuestionRound("abc");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNonPositiveQuestionRound() {
        request.setQuestionRound("-1");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testZeroQuestionRound() {
        request.setQuestionRound("0");
        assertTrue(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNonNumericQuestionOrdinal() {
        request.setQuestionRound("abc");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNonPositiveQuestionOrdinal() {
        request.setQuestionRound("-1");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testZeroQuestionOrdinal() {
        request.setQuestionRound("0");
        assertTrue(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNullHeaderText() {
        request.setQuestionHeaderText(null);
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testEmptyHeaderText() {
        request.setQuestionHeaderText("");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNullQuestionBodyText() {
        request.setQuestionBodyText(null);
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testEmptyQuestionBodyText() {
        request.setQuestionBodyText("");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testNulOwnerReference() {
        request.setOwnerReference(null);
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }

    @Test
    public void testEmptyOwnerReference() {
        request.setOwnerReference("");
        assertFalse(validation.execute(QuestionValidator.values(), request).isValid());
    }
}
