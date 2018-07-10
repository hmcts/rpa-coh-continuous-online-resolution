package uk.gov.hmcts.reform.coh.domain;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlinehearingRequest;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class OnlinehearingRequestTest {

    @Test
    public void testOnlinehearingRequest() throws Exception {
        Date date = Date.from(Instant.parse("2018-01-01T02:30:00Z"));

        OnlinehearingRequest onlinehearing = (OnlinehearingRequest)JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlinehearingRequest.class);
        assertEquals("case_123", onlinehearing.getCaseId());
        assertEquals("SSCS", onlinehearing.getJurisdiction());
        assertEquals(1, onlinehearing.getPanel().size());
        assertEquals("judge_123", onlinehearing.getPanel().get(0).getIdentityToken());
        assertEquals("Judge Dredd", onlinehearing.getPanel().get(0).getName());
        assertEquals(date, onlinehearing.getStartDate());
    }

    @Test
    public void testOnlinehearingRequestNoPanel() throws Exception {
        Date date = Date.from(Instant.parse("2018-01-01T02:30:00Z"));

        OnlinehearingRequest onlinehearing = (OnlinehearingRequest)JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing_no_panel", OnlinehearingRequest.class);
        assertEquals("case_123", onlinehearing.getCaseId());
        assertEquals("SSCS", onlinehearing.getJurisdiction());
        assertEquals(0, onlinehearing.getPanel().size());
        assertEquals(date, onlinehearing.getStartDate());
    }
}
