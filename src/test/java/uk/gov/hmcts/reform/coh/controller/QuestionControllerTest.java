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
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    @InjectMocks
    private QuestionController questionController;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/online-hearings/1/questions";

    private Question question;

    @Before
    public void setup() throws IOException {
        question = new Question();
        question.setOnlineHearingId(1);
        question.setQuestionText("foo");
        mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
        given(questionService.retrieveQuestionById(any(Long.class))).willReturn(question);
        given(questionService.createQuestion(1, question)).willReturn(question);
        given(questionService.editQuestion(1L, question)).willReturn(question);
    }

    @Test
    public void testGetQuestion() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Question responseQuestion = (Question)JsonUtils.toObjectFromJson(response, Question.class);
        assertEquals("foo", responseQuestion.getQuestionText());
    }

    @Test
    public void testCreateQuestion() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(question)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Question responseQuestion = (Question)JsonUtils.toObjectFromJson(response, Question.class);
        assertEquals("foo", responseQuestion.getQuestionText());
    }

    @Test
    public void testEditQuestion() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch(ENDPOINT + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(question)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Question responseQuestion = (Question)JsonUtils.toObjectFromJson(response, Question.class);
        assertEquals("foo", responseQuestion.getQuestionText());
    }
}