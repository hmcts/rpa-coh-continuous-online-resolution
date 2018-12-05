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
        assertEquals(CommonMessages.QUESTION_NOT_FOUND, "Question not found");
    }

    @Test
    public void testOnlinehearingNotFoundIsSet() {
        assertEquals(CommonMessages.ONLINE_HEARING_NOT_FOUND, "Online hearing not found");
    }

    @Test
    public void testEventTypeNotFoundIsSet() {
        assertEquals(CommonMessages.EVENT_TYPE_NOT_FOUND, "Event type not found");
    }

    @Test
    public void testJurisdictionNotFoundIsSet() {
        assertEquals(CommonMessages.JURISDICTION_NOT_FOUND, "Jurisdiction not found");
    }


}