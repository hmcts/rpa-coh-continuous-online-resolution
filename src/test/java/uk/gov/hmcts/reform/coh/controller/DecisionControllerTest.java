package uk.gov.hmcts.reform.coh.controller;

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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.states.DecisionsStates;
import uk.gov.hmcts.reform.coh.controller.decision.UpdateDecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.AllDecisionRepliesResponse;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyRequest;
import uk.gov.hmcts.reform.coh.controller.decisionreplies.DecisionReplyResponse;
import uk.gov.hmcts.reform.coh.controller.utils.CohISO8601DateFormat;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionReply;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.service.DecisionReplyService;
import uk.gov.hmcts.reform.coh.service.DecisionService;
import uk.gov.hmcts.reform.coh.service.DecisionStateService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.task.DecisionIssuedTask;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.coh.handlers.IdamHeaderInterceptor.IDAM_AUTHORIZATION;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class DecisionControllerTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private DecisionService decisionService;

    @Mock
    private DecisionStateService decisionStateService;

    @Mock
    private DecisionIssuedTask decisionIssuedTask;

    @Mock
    private DecisionReplyService decisionReplyService;

    @InjectMocks
    private DecisionController decisionController;

    @Autowired
    private MockMvc mockMvc;

    private String endpoint;

    private DecisionRequest request;

    private UpdateDecisionRequest updateDecisionRequest;

    private DecisionResponse response;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    private Decision decision;

    private DecisionState decisionState;

    private UUID decisionUUID;

    private Date expiryDate;
    private final String DECISION_REPLY_JSON = "decision/standard_decision_reply";
    private final String IDAM_AUTHOR_REFERENCE = "TEST_REFERENCE_IDAM";

    @Before
    public void setup() throws IOException {
        decisionState = new DecisionState();
        decisionState.setState("decision_drafted");
        uuid = UUID.randomUUID();
        decisionUUID = UUID.randomUUID();
        expiryDate = new Date();

        endpoint = "/continuous-online-hearings/" + uuid;

        onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(uuid);

        request = JsonUtils.toObjectFromTestName("decision/standard_decision", DecisionRequest.class);
        updateDecisionRequest = JsonUtils.toObjectFromTestName("decision/standard_decision", UpdateDecisionRequest.class);
        response = JsonUtils.toObjectFromTestName("decision/standard_decision_response", DecisionResponse.class);

        decision = new Decision();
        decision.setDecisionId(decisionUUID);
        decision.setOnlineHearing(onlineHearing);
        decision.setDecisionHeader(response.getDecisionHeader());
        decision.setDecisionText(response.getDecisionText());
        decision.setDecisionReason(response.getDecisionReason());
        decision.setDecisionAward(response.getDecisionAward());
        decision.setDeadlineExpiryDate(expiryDate);
        decision.setDecisionstate(decisionState);

        mockMvc = MockMvcBuilders.standaloneSetup(decisionController).build();
        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.of(onlineHearing));
        given(decisionService.createDecision(any(Decision.class))).willReturn(decision);
        given(decisionStateService.retrieveDecisionStateByState("decision_drafted")).willReturn(Optional.ofNullable(decisionState));
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.empty());
        given(decisionService.retrieveByOnlineHearingIdAndDecisionId(decisionUUID, decisionUUID)).willReturn(Optional.of(decision));
    }

    @Test
    public void testCreateDecisionAndCheckHeaderForLocation() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isCreated())
                .andReturn();

        String returnedUrl = result.getResponse().getHeader("Location");
        try {
            URL u = new URL(returnedUrl); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            assertTrue(true);
        }catch(MalformedURLException e){
            fail();
        }
    }

    @Test
    public void testCreateDecisionWithEmptyAuthorReferenceThrows400() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, ""))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertEquals("Authorization author id must not be empty", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateDuplicateDecision() throws Exception {

        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));

        mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateDecisionForNonExistentOnlineHearing() throws Exception {

        given(onlineHearingService.retrieveOnlineHearing(uuid)).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Online hearing not found", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateDecisionForNonExistentDecisionState() throws Exception {

        given(decisionStateService.retrieveDecisionStateByState("decision_drafted")).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        assertEquals("Unable to retrieve starting state for decision", result.getResponse().getContentAsString());
    }

    @Test
    public void testEmptyDecisionHeader() throws Exception {

        request.setDecisionHeader(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision header is required");
    }

    @Test
    public void testEmptyDecisionText() throws Exception {

        request.setDecisionText(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision text is required");
    }

    @Test
    public void testEmptyDecisionReason() throws Exception {

        request.setDecisionReason(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision reason is required");
    }

    @Test
    public void testEmptyDecisionAward() throws Exception {

        request.setDecisionAward(null);
        mockMvc.perform(MockMvcRequestBuilders.post(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision award is required");
    }

    @Test
    public void testGetDecision() throws Exception {
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        DecisionResponse expected = JsonUtils.toObjectFromTestName("decision/standard_decision_response", DecisionResponse.class);
        expected.setDecisionId(decisionUUID.toString());
        expected.setOnlineHearingId(uuid.toString());
        expected.setDeadlineExpiryDate(CohISO8601DateFormat.format(expiryDate));
        DecisionResponse actual = JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), DecisionResponse.class);

        assertEquals(expected.getDecisionId(), actual.getDecisionId());
        assertEquals(expected.getOnlineHearingId(), actual.getOnlineHearingId());
        assertEquals(expected.getDecisionHeader(), actual.getDecisionHeader());
        assertEquals(expected.getDecisionText(), actual.getDecisionText());
        assertEquals(expected.getDecisionReason(), actual.getDecisionReason());
        assertEquals(expected.getDecisionAward(), actual.getDecisionAward());
        assertEquals(expected.getDeadlineExpiryDate(), actual.getDeadlineExpiryDate());
        assertEquals(expected.getDecisionState().getName(), actual.getDecisionState().getName());
    }

    @Test
    public void testGetDecisionNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateNonExistentDecision() throws Exception {
        given(decisionService.retrieveByOnlineHearingIdAndDecisionId(decisionUUID, decisionUUID)).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision not found");
    }

    @Test
    public void testUpdateWhenDecisionIsNotDraft() throws Exception {
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        decisionState.setState("foo");
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Only draft decisions can be updated");
    }

    @Test
    public void testUpdateWhenDecisionInvalidStateInRequest() throws Exception {
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        updateDecisionRequest.setState("foo");
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Invalid state");
    }

    @Test
    public void testUpdateDecisionToDecisionIssuedFails() throws Exception {
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        updateDecisionRequest.setState("decision_issued");
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Invalid state");
    }

    @Test
    public void testUpdateWithEmptyDecisionHeader() throws Exception {

        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        updateDecisionRequest.setDecisionHeader(null);
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision header is required");
    }

    @Test
    public void testUpdateDecisionWithEmptyAuthorReferenceThrows400() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(request))
                .header(IDAM_AUTHORIZATION, ""))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertEquals("Authorization author id must not be empty", result.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateWithEmptyDecisionText() throws Exception {

        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        updateDecisionRequest.setDecisionText(null);
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision text is required");
    }

    @Test
    public void testUpdateWithEmptyDecisionReason() throws Exception {

        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        updateDecisionRequest.setDecisionReason(null);
        mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("Decision reason is required");
    }

    @Test
    public void testUpdateDecisionIssuePending() throws Exception {

        doNothing().when(decisionIssuedTask).execute(onlineHearing);
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        given(decisionStateService.retrieveDecisionStateByState("decision_issue_pending")).willReturn(Optional.of(decisionState));
        updateDecisionRequest.setState(DecisionsStates.DECISION_ISSUE_PENDING.getStateName());
            mockMvc.perform(MockMvcRequestBuilders.put(endpoint+ "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(updateDecisionRequest))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateReplyToDecision() throws Exception {
        DecisionState issuedState = new DecisionState();
        issuedState.setState(DecisionsStates.DECISION_ISSUED.getStateName());

        Decision decision = new Decision();
        decision.setDecisionstate(issuedState);
        given(decisionService.findByOnlineHearingId(any(UUID.class))).willReturn(Optional.of(decision));
        given(decisionReplyService.createDecision(any(DecisionReply.class))).willReturn(new DecisionReply());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.getJsonInput(DECISION_REPLY_JSON))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isCreated())
                .andReturn();

        String returnedUrl = result.getResponse().getHeader("Location");
        try {
            URL u = new URL(returnedUrl); // this would check for the protocol
            u.toURI(); // does the extra checking required for validation of URI
            assertTrue(true);
        }catch(MalformedURLException e){
            fail();
        }
    }

    @Test
    public void testCreateDecisionReplyWithEmptyAuthorReferenceThrows400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.getJsonInput(DECISION_REPLY_JSON))
                .header(IDAM_AUTHORIZATION, ""))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertEquals("Authorization author id must not be empty", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateReplyToHearingWithInvalidReplyThrowsBadRequest() throws Exception {
        given(decisionReplyService.createDecision(any(DecisionReply.class))).willReturn(new DecisionReply());

        DecisionReplyRequest request = JsonUtils.toObjectFromTestName(DECISION_REPLY_JSON, DecisionReplyRequest.class);
        request.setDecisionReply("invalid_state");

        String json = JsonUtils.toJson(request);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("Decision reply field is not valid", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateReplyToHearingWithDecisionNotIssuedThrowsNotFound() throws Exception {
        DecisionState issuedState = new DecisionState();
        issuedState.setState(DecisionsStates.DECISION_DRAFTED.getStateName());

        Decision decision = new Decision();
        decision.setDecisionstate(issuedState);
        given(decisionService.findByOnlineHearingId(any(UUID.class))).willReturn(Optional.of(decision));
        given(decisionReplyService.createDecision(any(DecisionReply.class))).willReturn(new DecisionReply());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.getJsonInput(DECISION_REPLY_JSON))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Decision must be issued before replying", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateReplyToDecisionWithInvalidOnlineHearingThrowsNotFound() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.empty());
        given(decisionService.findByOnlineHearingId(any(UUID.class))).willReturn(Optional.of(new Decision()));
        given(decisionReplyService.createDecision(any(DecisionReply.class))).willReturn(new DecisionReply());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.getJsonInput(DECISION_REPLY_JSON))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Online hearing not found", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateReplyToHearingWithoutDecisionThrowsNotFound() throws Exception {
        given(decisionReplyService.createDecision(any(DecisionReply.class))).willReturn(new DecisionReply());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.getJsonInput(DECISION_REPLY_JSON))
                .header(IDAM_AUTHORIZATION, IDAM_AUTHOR_REFERENCE))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Unable to find decision", result.getResponse().getContentAsString());
    }

    @Test
    public void testGetAllRepliesToDecision() throws Exception {
        List<DecisionReply> decisionReplies = new ArrayList<>();

        DecisionReply decisionReply = new DecisionReply();
        decisionReply.setId(UUID.randomUUID());
        decisionReply.setDecision(decision);
        decisionReply.setAuthorReferenceId("some author");
        decisionReply.setDecisionReply(true);
        decisionReply.setDecisionReplyReason("some reason");
        decisionReply.setDateOccured(new Date());
        decisionReplies.add(decisionReply);

        decisionReply = new DecisionReply();
        decisionReply.setId(UUID.randomUUID());
        decisionReply.setDecision(decision);
        decisionReply.setAuthorReferenceId("some author 1");
        decisionReply.setDecisionReply(true);
        decisionReply.setDecisionReplyReason("some reason 1");
        decisionReply.setDateOccured(new Date());
        decisionReplies.add(decisionReply);

        given(decisionReplyService.findAllDecisionReplyByDecision(any(Decision.class))).willReturn(decisionReplies);
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        AllDecisionRepliesResponse allDecisionRepliesResponse =
                JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), AllDecisionRepliesResponse.class);

        assertEquals(decisionReplies.size(), allDecisionRepliesResponse.getDecisionReplyList().size());

        int n = 0;
        for (DecisionReply expectedReply : decisionReplies) {
            DecisionReplyResponse decisionReplyResponse = allDecisionRepliesResponse.getDecisionReplyList().get(n);

            assertNotNull(decisionReplyResponse);

            assertEquals(expectedReply.getId().toString(), decisionReplyResponse.getDecisionReplyId());

            assertEquals(expectedReply.getDecision().getDecisionId().toString(), decisionReplyResponse.getDecisionId());

            assertEquals(expectedReply.getAuthorReferenceId(), decisionReplyResponse.getAuthorReference());

            assertEquals(DecisionsStates.DECISIONS_ACCEPTED.getStateName(), decisionReplyResponse.getDecisionReply());

            assertEquals(expectedReply.getDecisionReplyReason(), decisionReplyResponse.getDecisionReplyReason());

            n++;
        }
        assertEquals(decisionReplies.size(), n);
    }

    @Test
    public void testGetAllDecisionRepliesReturnsEmptyListIfNoReplies() throws Exception {
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        AllDecisionRepliesResponse allDecisionRepliesResponse = JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), AllDecisionRepliesResponse.class);
        assertEquals(0, allDecisionRepliesResponse.getDecisionReplyList().size());
    }

    @Test
    public void testGetAllDecisionRepliesOnlineHearingDoesNotExistThrows404() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.empty());
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Online hearing not found", result.getResponse().getContentAsString());
    }

    @Test
    public void testGetAllDecisionRepliesDecisionNotFoundThrows404() throws Exception {
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Unable to find decision", result.getResponse().getContentAsString());
    }

    @Test
    public void testGetASingleReplyToDecision() throws Exception {
        UUID decisionReplyId = UUID.randomUUID();

        DecisionReply decisionReply = new DecisionReply();
        decisionReply.setId(decisionReplyId);
        decisionReply.setDecision(decision);
        decisionReply.setAuthorReferenceId("some author");
        decisionReply.setDecisionReply(true);
        decisionReply.setDecisionReplyReason("some reason");
        decisionReply.setDateOccured(new Date());

        given(decisionReplyService.findByDecisionReplyId(decisionReplyId)).willReturn(Optional.of(decisionReply));
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies/" + decisionReplyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        DecisionReplyResponse decisionReplyResponse = JsonUtils.toObjectFromJson(result.getResponse().getContentAsString(), DecisionReplyResponse.class);
        assertEquals(decisionReply.getId().toString(), decisionReplyResponse.getDecisionReplyId());
        assertEquals(decisionReply.getDecision().getDecisionId().toString(), decisionReplyResponse.getDecisionId());
        assertEquals(decisionReply.getAuthorReferenceId(), decisionReplyResponse.getAuthorReference());
        assertEquals(DecisionsStates.DECISIONS_ACCEPTED.getStateName(), decisionReplyResponse.getDecisionReply());
        assertEquals(decisionReply.getDecisionReplyReason(), decisionReplyResponse.getDecisionReplyReason());
    }


    @Test
    public void testGetDecisionReplyOnlineHearingDoesNotExistThrows404() throws Exception {
        UUID decisionReplyId = UUID.randomUUID();
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.empty());
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies/" + decisionReplyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Online hearing not found", result.getResponse().getContentAsString());
    }

    @Test
    public void testGetDecisionReplyDecisionNotFoundThrows404() throws Exception {
        UUID decisionReplyId = UUID.randomUUID();
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.empty());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies/" + decisionReplyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Unable to find decision", result.getResponse().getContentAsString());
    }

    @Test
    public void testGetDecisionReplyDecisionReplyNotFoundThrows404() throws Exception {
        UUID decisionReplyId = UUID.randomUUID();
        given(decisionReplyService.findByDecisionReplyId(decisionReplyId)).willReturn(Optional.empty());
        given(decisionService.findByOnlineHearingId(uuid)).willReturn(Optional.of(decision));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(endpoint + "/decisionreplies/" + decisionReplyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("Unable to find decision reply", result.getResponse().getContentAsString());
    }
}