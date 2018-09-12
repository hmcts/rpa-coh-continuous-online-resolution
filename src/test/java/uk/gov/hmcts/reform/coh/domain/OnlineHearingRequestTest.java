package uk.gov.hmcts.reform.coh.domain;

import org.junit.Test;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class OnlineHearingRequestTest {

    @Test
    public void testOnlineHearingRequest() throws Exception {
        Date date = Date.from(Instant.parse("2018-01-01T02:30:00Z"));

        OnlineHearingRequest onlineHearing = JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        assertEquals("case_123", onlineHearing.getCaseId());
        assertEquals("SSCS", onlineHearing.getJurisdiction());
        assertEquals(date, onlineHearing.getStartDate());
    }

    @Test
    public void testOnlineHearingRequestNoPanel() throws Exception {
        Date date = Date.from(Instant.parse("2018-01-01T02:30:00Z"));

        OnlineHearingRequest onlineHearing = JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing_no_panel", OnlineHearingRequest.class);
        assertEquals("case_123", onlineHearing.getCaseId());
        assertEquals("SSCS", onlineHearing.getJurisdiction());
        assertEquals(date, onlineHearing.getStartDate());
    }
}
