package uk.gov.hmcts.reform.coh.controller.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommonMessagesTest {

    @Test
    public void testInitateCommonMessages() {
        CommonMessages cm = new CommonMessages();
        assertNotNull(cm);
    }

    @Test
    public void testQuestionNotFoundIsSet() {
        assertEquals("Question not found", CommonMessages.QUESTION_NOT_FOUND);
    }

    @Test
    public void testOnlinehearingNotFoundIsSet() {
        String message = CommonMessages.ONLINE_HEARING_NOT_FOUND;
        assertEquals("Online hearing not found", message);
    }

    @Test
    public void testEventTypeNotFoundIsSet() {
        String message = CommonMessages.EVENT_TYPE_NOT_FOUND;
        assertEquals("Event type not found", message);
    }

    @Test
    public void testJurisdictionNotFoundIsSet() {
        String message = CommonMessages.JURISDICTION_NOT_FOUND;
        assertEquals("Jurisdiction not found", message);
    }


}