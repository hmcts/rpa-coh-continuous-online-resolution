package uk.gov.hmcts.reform.coh.controller.validators;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.coh.controller.validators.ValidatorUtils.isPositiveInteger;

public class ValidationUtilsTest {

    @Test
    public void testUnparseableString() {
        assertFalse(isPositiveInteger("foo"));
    }

    @Test
    public void testNull() {
        assertFalse(isPositiveInteger(null));
    }

    @Test
    public void testNumberLessThanZero() {
        assertFalse(isPositiveInteger("-1"));
    }

    @Test
    public void testNumberZero() {
        assertTrue(isPositiveInteger("0"));
    }

    @Test
    public void testPositiveNumber() {
        assertTrue(isPositiveInteger("1"));
    }
}
