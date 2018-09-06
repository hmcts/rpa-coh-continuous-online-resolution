package uk.gov.hmcts.reform.coh.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.*;
import uk.gov.hmcts.reform.coh.states.SessionEventForwardingStates;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class EventForwardingControllerTest {

    @Mock
    private SessionEventTypeService sessionEventTypeService;

    @Mock
    private JurisdictionService jurisdictionService;

    @Mock
    private SessionEventForwardingRegisterService sessionEventForwardingRegisterService;

    @Mock
    private SessionEventForwardingStateService sessionEventForwardingStateService;

    @Mock
    private SessionEventService sessionEventService;

    @InjectMocks
    private EventForwardingController eventForwardingController;

    @Autowired
    private MockMvc mockMvc;

    private String validJson;

    private SessionEventForwardingRegister sessionEventForwardingRegister;

    private List<SessionEvent> sessionEventList;

    @Before
    public void setUp() throws IOException {
        validJson = JsonUtils.getJsonInput("event_forwarding_register/valid_event_register");

        sessionEventForwardingRegister = new SessionEventForwardingRegister();
        SessionEventType sessionEventType = new SessionEventType();
        sessionEventType.setEventTypeName("question_round_issued");
        sessionEventType.setEventTypeId(5);
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionName("JUI");
        jurisdiction.setJurisdictionId(2L);

        SessionEventForwardingState pendingEventForwardingState = new SessionEventForwardingState();
        pendingEventForwardingState.setForwardingStateName("event_forwarding_pending");
        pendingEventForwardingState.setForwardingStateId(1);

        SessionEventForwardingState sendEventForwardingState = new SessionEventForwardingState();
        sendEventForwardingState.setForwardingStateName("event_forwarding_success");
        sendEventForwardingState.setForwardingStateId(2);

        SessionEvent alreadyPendingEvent = new SessionEvent();
        alreadyPendingEvent.setSessionEventForwardingState(pendingEventForwardingState);

        SessionEvent sentEvent = new SessionEvent();
        sentEvent.setSessionEventForwardingState(sendEventForwardingState);

        sessionEventList = new ArrayList<>();
        sessionEventList.add(alreadyPendingEvent);
        sessionEventList.add(sentEvent);

        given(sessionEventTypeService.retrieveEventType(any(String.class))).willReturn(Optional.of(sessionEventType));
        given(jurisdictionService.getJurisdictionWithName(any(String.class))).willReturn(Optional.of(jurisdiction));

        mockSessionEventForwardingRegisterService(false);

        given(sessionEventForwardingStateService.retrieveEventForwardingStateByName(anyString())).willReturn(Optional.of(pendingEventForwardingState));
        mockSessionEventForwardingRegisterService(true);
        given(sessionEventService.retrieveAllByEventForwardingRegister(any(SessionEventForwardingRegister.class))).willReturn(sessionEventList);
        given(sessionEventService.updateSessionEvent(any(SessionEvent.class))).willReturn(new SessionEvent());
        mockMvc = MockMvcBuilders.standaloneSetup(eventForwardingController)
                .build();
    }

    @Test
    public void testResetSessionEvents() throws Exception {

        given(sessionEventService.findAllBySessionEventForwardingRegisterAndSessionEventForwardingState(any(SessionEventForwardingRegister.class), any(SessionEventForwardingState.class))).willReturn(sessionEventList);
        String json = JsonUtils.getJsonInput("event_forwarding_register/reset_answer_submitted_events");

        mockMvc.perform(put(CohUriBuilder.buildEventResetPut())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is2xxSuccessful());

        int size = sessionEventList.size();
        verify(sessionEventService, times(size)).updateSessionEvent(any(SessionEvent.class));
        long count = sessionEventList.stream()
                .filter(se -> {return se.getRetries()==0 && se.getSessionEventForwardingState().getForwardingStateName().equalsIgnoreCase(SessionEventForwardingStates.EVENT_FORWARDING_PENDING.getStateName());})
                .count();
        assertTrue(count == size);
    }

    @Test
    public void testResetEventsMissingEventType() throws Exception {

        given(sessionEventTypeService.retrieveEventType(any(String.class)))
                .willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(put(CohUriBuilder.buildEventResetPut())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is4xxClientError()).andReturn();

        assertEquals("Event type not found", result.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void testResetEventsMissingJurisdiction() throws Exception {

        given(jurisdictionService.getJurisdictionWithName(any(String.class)))
                .willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(put(CohUriBuilder.buildEventResetPut())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is4xxClientError()).andReturn();

        assertEquals("Jurisdiction not found", result.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void testResetEventsForNonExistingRegisterThrowsBadRequest() throws Exception {
        mockSessionEventForwardingRegisterService(false);
        String json = JsonUtils.getJsonInput("event_forwarding_register/reset_answer_submitted_events");

        MvcResult result = mockMvc.perform(put(CohUriBuilder.buildEventResetPut())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is4xxClientError())
                .andReturn();
        assertEquals(HttpStatus.FAILED_DEPENDENCY.value(), result.getResponse().getStatus());
    }

    @Test
    public void testResetEventsThrowsInternalServerErrorIfEventPendingStateNotFound() throws Exception {

        given(sessionEventForwardingStateService.retrieveEventForwardingStateByName(anyString())).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("event_forwarding_register/reset_answer_submitted_events");

        MvcResult result = mockMvc.perform(put(CohUriBuilder.buildEventResetPut())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().is5xxServerError())
                .andReturn();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
    }

    @Test
    public void testCreateEventForwardRegister() throws Exception {
        mockSessionEventForwardingRegisterService(false);
        MvcResult result = mockMvc.perform(post(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is2xxSuccessful()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    private void mockSessionEventForwardingRegisterService(boolean isPresent) {
        if (isPresent) {
            given(sessionEventForwardingRegisterService.retrieveEventForwardingRegister(
                    any(SessionEventForwardingRegister.class)))
                    .willReturn(Optional.of(sessionEventForwardingRegister));
        } else {
            given(sessionEventForwardingRegisterService.retrieveEventForwardingRegister(
                    any(SessionEventForwardingRegister.class)))
                    .willReturn(Optional.empty());
        }
    }

    @Test
    public void testCreateEventForwardRegisterConflict() throws Exception {

        MvcResult result = mockMvc.perform(post(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is4xxClientError()).andReturn();

        assertEquals("Jurisdiction already registered to event", result.getResponse().getContentAsString());
        assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
    }

    @Test
    public void testCreateEventForwardRegisterMissingEventType() throws Exception {

        given(sessionEventTypeService.retrieveEventType(any(String.class)))
                .willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(post(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is4xxClientError()).andReturn();

        assertEquals("Event type not found", result.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void testCreateEventForwardRegisterMissingJurisdiction() throws Exception {

        given(jurisdictionService.getJurisdictionWithName(any(String.class)))
                .willReturn(Optional.empty());

        MvcResult result = mockMvc.perform(post(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is4xxClientError()).andReturn();

        assertEquals("Jurisdiction not found", result.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void testCreateEventForwardRegisterInvalidURL() throws Exception {
        EventRegistrationRequest eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromTestName("event_forwarding_register/invalid_event_register", EventRegistrationRequest.class);

        MvcResult result = mockMvc.perform(post(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(eventRegistrationRequest)))
                .andExpect(status().is4xxClientError()).andReturn();

        assertThat(result.getResolvedException().getClass(),
                typeCompatibleWith(MethodArgumentNotValidException.class));
    }

    @Test
    public void testUpdateEventForwardRegister() throws Exception {
        mockMvc.perform(put(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateEventForwardRegisterNotFound() throws Exception {
        mockSessionEventForwardingRegisterService(false);
        mockMvc.perform(put(CohUriBuilder.buildEventRegisterPost())
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isNotFound());
    }
}