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
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundResponse;
import uk.gov.hmcts.reform.coh.controller.questionrounds.QuestionRoundsResponse;
import uk.gov.hmcts.reform.coh.domain.*;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class QuestionRoundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private QuestionRoundService questionRoundService;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionStateService questionStateService;

    @InjectMocks
    private QuestionRoundController questionRoundController;

    private UUID cohId;

    private static final String ENDPOINT = "/continuous-online-hearings/";
    private final int ROUNDID = 1;
    private QuestionRound questionRound;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(questionRoundController).build();

        List<QuestionRound> questionRounds = new ArrayList<>();
        questionRound = new QuestionRound();
        questionRound.setQuestionRoundNumber(ROUNDID);
        QuestionRoundState questionRoundState = new QuestionRoundState();

        QuestionState issuedState = new QuestionState();
        issuedState.setState("ISSUED");
        issuedState.setQuestionStateId(3);

        questionRoundState.setState(issuedState);

        List<Question> questions = new ArrayList<>();
        Question question = new Question();
        question.setQuestionState(issuedState);
        question.setQuestionRound(ROUNDID);
        questions.add(question);
        question.setQuestionId(UUID.randomUUID());
        questionRound.setQuestionList(questions);

        questionRound.setQuestionRoundState(questionRoundState);
        questionRounds.add(questionRound);


        cohId = UUID.randomUUID();
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(cohId);
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setMaxQuestionRounds(3);
        onlineHearing.setJurisdiction(jurisdiction);

        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(issuedState));
        given(questionRoundService.issueQuestionRound(any(OnlineHearing.class), any(QuestionState.class), anyInt())).willReturn(null);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.of(onlineHearing));
        given(questionRoundService.getAllQuestionRounds(any(OnlineHearing.class))).willReturn(questionRounds);
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);
        given(questionRoundService.getNextQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(3);
        given(questionRoundService.getPreviousQuestionRound(anyInt())).willReturn(1);
        given(questionRoundService.getQuestionRoundByRoundId(any(OnlineHearing.class), anyInt())).willReturn(questionRound);
    }

    @Test
    public void testGetAllQuestionRounds() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + cohId + "/questionrounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionRoundsResponse questionRoundsResponse = (QuestionRoundsResponse)JsonUtils.toObjectFromJson(response, QuestionRoundsResponse.class);
        assertEquals(3, (int) questionRoundsResponse.getMaxQuestionRound());
        assertEquals(2, (int) questionRoundsResponse.getCurrentQuestionRound());
        assertEquals(1, (int) questionRoundsResponse.getPreviousQuestionRound());
    }

    @Test
    public void testOnlineHearingNotFound() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + cohId + "/questionrounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testGetAQuestionRound() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionRoundResponse questionRoundResponse = (QuestionRoundResponse)JsonUtils.toObjectFromJson(response, QuestionRoundResponse.class);
        assertEquals(1, questionRoundResponse.getQuestionList().size());
        assertEquals("1", questionRoundResponse.getQuestionRound());
    }

    @Test
    public void testGetAQuestionRoundWithNoQuestions() throws Exception {
        questionRound.setQuestionList(new ArrayList<>());

        given(questionRoundService.getQuestionRoundByRoundId(any(OnlineHearing.class), anyInt())).willReturn(questionRound);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionRoundResponse questionRoundResponse = (QuestionRoundResponse)JsonUtils.toObjectFromJson(response, QuestionRoundResponse.class);
        assertEquals(0, questionRoundResponse.getQuestionList().size());
        assertEquals("1", questionRoundResponse.getQuestionRound());
    }

    @Test
    public void testUpdateANonExistingQuestionRoundThrowsNotFound() throws Exception {
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);

        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + 3)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithNonExistingOnlineHearingThrowsNotFound() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithNonExistingStateThrowsBadRequest() throws Exception {
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithStateOtherThanIssuedThrowsBadRequest() throws Exception {
        QuestionState draftedState = new QuestionState();
        draftedState.setState("DRAFTED");
        draftedState.setQuestionStateId(1);

        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(draftedState));
        String json = JsonUtils.toJson("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdatePreviousQuestionRoundThrowsBadRequest() throws Exception {
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);

        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();
    }

    @Test
    public void testUpdateCurrentQuestionRoundToIssued() throws Exception {
        QuestionState draftedState = new QuestionState();
        draftedState.setState("ISSUED");
        draftedState.setQuestionStateId(1);
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.of(draftedState));
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(1);

        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + ROUNDID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();
    }
}
