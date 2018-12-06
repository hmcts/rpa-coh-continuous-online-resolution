package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import org.junit.Test;

import static org.junit.Assert.*;

public class UpdateOnlineHearingRequestTest {

    @Test
    public void testCreateUpdateOnlineHearingRequest() {
        String reason = "some reason";
        String state = "continuous_online_hearing_relisted";
        UpdateOnlineHearingRequest request = new UpdateOnlineHearingRequest();
        request.setReason(reason);
        request.setState(state);

        assertEquals(state, request.getState());
        assertEquals(reason, request.getReason());
    }

}