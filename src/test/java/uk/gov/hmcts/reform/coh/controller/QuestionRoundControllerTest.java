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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
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

    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(questionRoundController).build();

        List<QuestionRound> questionRounds = new ArrayList<>();
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionRoundNumber(ROUNDID);
        QuestionRoundState questionRoundState = new QuestionRoundState();

        QuestionState questionState = new QuestionState();
        questionState.setState("ISSUED");
        questionState.setQuestionStateId(3);

        questionRoundState.setState(questionState);

        List<Question> questions = new ArrayList<>();
        Question question = new Question();
        question.setQuestionState(questionState);
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

        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.ofNullable(questionState));
        willDoNothing().willDoNothing().given(questionRoundService).issueQuestionRound(any(OnlineHearing.class), any(QuestionState.class), anyInt());
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
    public void testUpdateQuestionRoundToIssued() throws Exception {
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithInvalidOnlineHearing() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithNotKnownState() throws Exception {
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.empty());
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdateQuestionRoundWithInvalidStateChange() throws Exception {
        QuestionState questionState = new QuestionState();
        questionState.setQuestionStateId(1);
        questionState.setState("SUBMITTED");
        given(questionStateService.retrieveQuestionStateByStateName(anyString())).willReturn(Optional.ofNullable(questionState));
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void testUpdateAnInvalidQuestionRound() throws Exception {
        String json = JsonUtils.getJsonInput("question_round/issue_question_round");
        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + cohId + "/questionrounds/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
