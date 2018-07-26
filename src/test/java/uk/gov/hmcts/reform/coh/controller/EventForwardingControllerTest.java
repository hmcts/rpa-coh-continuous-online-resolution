package uk.gov.hmcts.reform.coh.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import static org.junit.Assert.assertEquals;
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
