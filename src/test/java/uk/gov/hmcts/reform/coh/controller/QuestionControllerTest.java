package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.coh.controller.question.CreateQuestionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionRequest;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Mock
    private OnlineHearingService onlineHearingService;

    @InjectMocks
    private QuestionController questionController;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/continuous-online-hearings/0c08b113-16d1-4fb5-b41f-a928aa64d39a/questions";

    private QuestionRequest questionRequest;

    private Question question;

    private UUID uuid;

    @Before
    public void setup() throws IOException {

        questionRequest = (QuestionRequest) JsonUtils.toObjectFromTestName("question/standard_question", QuestionRequest.class);

        OnlineHearing onlineHearing = new OnlineHearing();
        uuid = UUID.randomUUID();
        question = new Question();
        question.setQuestionId(uuid);
        question.setQuestionText("foo");
        question.setOnlineHearing(onlineHearing);
        question.setQuestionRound(1);
        QuestionState issuedState = new QuestionState();
        issuedState.setQuestionStateId(QuestionState.ISSUED);
        question.setQuestionState(issuedState);

        mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
        given(questionService.retrieveQuestionById(uuid)).willReturn(Optional.of(question));
        given(questionService.createQuestion(any(Question.class), any(OnlineHearing.class))).willReturn(question);
        given(questionService.editQuestion(uuid, question)).willReturn(question);
        given(questionService.updateQuestion(any(Question.class), any(Question.class))).willReturn(question);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(java.util.Optional.of(onlineHearing));
    }

    @Test
    public void testGetQuestion() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        QuestionResponse responseQuestion = (QuestionResponse)JsonUtils.toObjectFromJson(response, QuestionResponse.class);
        assertEquals("foo", responseQuestion.getQuestionBodyText());
    }

    @Test
    public void testCreateQuestion() throws Exception {

        String json = JsonUtils.getJsonInput("question/standard_question");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        CreateQuestionResponse responseQuestion = (CreateQuestionResponse) JsonUtils.toObjectFromJson(response, CreateQuestionResponse.class);
        assertNotNull(responseQuestion.getQuestionId());
    }

    @Test
    public void testValidateQuestionRound() throws Exception {

        questionRequest.setQuestionRound(null);
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionOrdinal() throws Exception {

        questionRequest.setQuestionOrdinal(null);
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionHeaderText() throws Exception {

        questionRequest.setQuestionHeaderText(null);
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionBodyText() throws Exception {

        questionRequest.setQuestionBodyText(null);
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testValidateQuestionOwnerReference() throws Exception {

        questionRequest.setOwnerReference(null);
        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetAllQuestions() throws Exception {

        List<Question> responses = new ArrayList<>();
        responses.add(question);

        given(questionService.finaAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.ofNullable(responses));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        List<QuestionResponse> questionResponses = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<QuestionResponse>>(){});

        assertEquals(1, questionResponses.size());
    }

    @Test
    public void testGetAllQuestionsNone() throws Exception {

        given(questionService.finaAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.ofNullable(null));
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllQuestionsWhenNone() throws Exception {

        List<Question> responses = new ArrayList<>();

        given(questionService.finaAllQuestionsByOnlineHearing(any(OnlineHearing.class))).willReturn(Optional.ofNullable(responses));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(questionRequest)))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        List<QuestionResponse> questionResponses = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<QuestionResponse>>(){});

        assertEquals(0, questionResponses.size());
    }

    @Test
    public void testEditQuestion() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/" + uuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(question)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Question responseQuestion = (Question)JsonUtils.toObjectFromJson(response, Question.class);
        assertEquals("foo", responseQuestion.getQuestionText());
    }
}