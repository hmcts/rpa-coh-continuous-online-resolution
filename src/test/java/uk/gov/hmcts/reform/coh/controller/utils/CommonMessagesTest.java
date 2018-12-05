package uk.gov.hmcts.reform.coh.controller.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommonMessagesTest {

    @Test
    public void testInitateCommonMessages() {
        CommonMessages cm = new CommonMessages();
    }

    @Test
    public void testQuestionNotFoundIsSet() {
        assertEquals("Question not found", CommonMessages.QUESTION_NOT_FOUND);
    }

    @Test
    public void testOnlinehearingNotFoundIsSet() {
        assertEquals("Online hearing not found", CommonMessages.ONLINE_HEARING_NOT_FOUND);
    }

    @Test
    public void testEventTypeNotFoundIsSet() {
        assertEquals("Event type not found", CommonMessages.EVENT_TYPE_NOT_FOUND);
    }

    @Test
    public void testJurisdictionNotFoundIsSet() {
        assertEquals("Jurisdiction not found", CommonMessages.JURISDICTION_NOT_FOUND);
    }


}