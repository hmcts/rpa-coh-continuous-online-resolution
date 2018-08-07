package uk.gov.hmcts.reform.coh.controller.validators;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;

import static org.junit.Assert.*;

public class DecisionRequestValidatorTest {

    private DecisionRequest request;

    private Validation validation;

    @Before
    public void setup() throws IOException {
        request = JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);
        validation = new Validation();
    }

    @Test
    public void testValidDecisionRequest() {
        ValidationResult result = validation.execute(DecisionRequestValidator.values(), request);
        assertTrue(result.isValid());
    }

    @Test
    public void testEmptyDecisionHeader() {
        request.setDecisionHeader(null);
        ValidationResult result = validation.execute(DecisionRequestValidator.values(),request);
        assertFalse(result.isValid());
        assertEquals("Decision header is required", result.getReason());
    }

    @Test
    public void testEmptyDecisionText() {
        request.setDecisionText(null);
        ValidationResult result = validation.execute(DecisionRequestValidator.values(),request);
        assertFalse(result.isValid());
        assertEquals("Decision text is required", result.getReason());
    }

    @Test
    public void testEmptyDecisionReason() {
        request.setDecisionReason(null);
        ValidationResult result = validation.execute(DecisionRequestValidator.values(),request);
        assertFalse(result.isValid());
        assertEquals("Decision reason is required", result.getReason());
    }

    @Test
    public void testEmptyDecisionAward() {
        request.setDecisionAward(null);
        ValidationResult result = validation.execute(DecisionRequestValidator.values(),request);
        assertFalse(result.isValid());
        assertEquals("Decision award is required", result.getReason());
    }
}
