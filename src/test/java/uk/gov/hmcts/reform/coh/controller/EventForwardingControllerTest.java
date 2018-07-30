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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.coh.controller.events.EventRegistrationRequest;
import uk.gov.hmcts.reform.coh.controller.exceptions.ValidResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventType;
import uk.gov.hmcts.reform.coh.service.JurisdictionService;
import uk.gov.hmcts.reform.coh.service.SessionEventForwardingRegisterService;
import uk.gov.hmcts.reform.coh.service.SessionEventTypeService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @InjectMocks
    private EventForwardingController eventForwardingController;

    @Autowired
    private MockMvc mockMvc;

    private String validJson;

    private static final String ENDPOINT = "/continuous-online-hearings/events";

    private SessionEventForwardingRegister sessionEventForwardingRegister;

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


        given(sessionEventTypeService.retrieveEventType(any(String.class))).willReturn(Optional.of(sessionEventType));
        given(jurisdictionService.getJurisdictionWithName(any(String.class))).willReturn(Optional.of(jurisdiction));

        given(sessionEventForwardingRegisterService.retrieveEventForwardingRegister(
                any(SessionEventForwardingRegister.class)))
                .willReturn(Optional.empty());

        mockMvc = MockMvcBuilders.standaloneSetup(eventForwardingController)
                .build();
    }

    @Test
    public void testCreateEventForwardRegister() throws Exception {

        MvcResult result = mockMvc.perform(post(ENDPOINT+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is2xxSuccessful()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void testCreateEventForwardRegisterConflict() throws Exception {

        given(sessionEventForwardingRegisterService.retrieveEventForwardingRegister(
                any(SessionEventForwardingRegister.class)))
                .willReturn(Optional.of(new SessionEventForwardingRegister()));

        MvcResult result = mockMvc.perform(post(ENDPOINT+"/register")
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

        given(sessionEventForwardingRegisterService.retrieveEventForwardingRegister(
                any(SessionEventForwardingRegister.class)))
                .willReturn(Optional.of(sessionEventForwardingRegister));

        MvcResult result = mockMvc.perform(post(ENDPOINT+"/register")
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

        given(sessionEventForwardingRegisterService.retrieveEventForwardingRegister(
                any(SessionEventForwardingRegister.class)))
                .willReturn(Optional.of(sessionEventForwardingRegister));

        MvcResult result = mockMvc.perform(post(ENDPOINT+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().is4xxClientError()).andReturn();

        assertEquals("Jurisdiction not found", result.getResponse().getContentAsString());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
    }

    @Test
    public void testCreateEventForwardRegisterInvalidURL() throws Exception {
        EventRegistrationRequest eventRegistrationRequest = (EventRegistrationRequest) JsonUtils.toObjectFromTestName("event_forwarding_register/invalid_event_register", EventRegistrationRequest.class);

        MvcResult result = mockMvc.perform(post(ENDPOINT+"/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(eventRegistrationRequest)))
                .andExpect(status().is4xxClientError()).andReturn();

        assertThat(result.getResolvedException().getClass(),
                typeCompatibleWith(MethodArgumentNotValidException.class));
    }

}