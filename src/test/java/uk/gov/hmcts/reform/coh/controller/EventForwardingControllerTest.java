package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.*;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingPanelMemberService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingStateService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class EventForwardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/continuous-online-hearings/events/register";

    private EventRegistrationRequest eventRegistrationRequest;


    @Test
    public void testEventRegisteringWithJsonFile() throws Exception {
        eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromTestName("event_forwarding_register/valid_event_register", EventRegistrationRequest.class);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(eventRegistrationRequest)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testMaximumRetriesDefault() throws Exception {
        final Integer DEFAULT_RETRIES = 3;

        eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromTestName("event_forwarding_register/valid_event_register_no_retries", EventRegistrationRequest.class);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(eventRegistrationRequest)))
                .andExpect(status().is2xxSuccessful());

        assertEquals(DEFAULT_RETRIES, eventRegistrationRequest.getMaxRetries());

    }

    @Test
    public void testIsActiveIsBoolean() throws Exception {
        eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromTestName("event_forwarding_register/event_register_invalid_active", EventRegistrationRequest.class);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON).content(JsonUtils.toJson(eventRegistrationRequest)))
                .andExpect(status().is4xxClientError());
    }

}
