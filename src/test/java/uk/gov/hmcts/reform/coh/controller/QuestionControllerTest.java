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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/online-hearings/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questions";

    private static final Long QUESTION_ID = Long.valueOf(2000);

    @InjectMocks
    private QuestionController questionController;

    @Before
    public void setup() {
        Question question = new Question();
        question.setQuestionId(QUESTION_ID);

        mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
        given(questionService.retrieveQuestionById(QUESTION_ID)).willReturn(question);
        given(questionService.issueQuestion(any(Question.class))).willReturn(true);
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssued() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + QUESTION_ID + "/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals("{\"questionId\":2000,\"questionRoundId\":0,\"subject\":null,\"questionText\":null,\"questionState\":null,\"questionStateHistories\":[]}", response);
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssuedWithNullRoundIdReturnsClientError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + "/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssuedWithWrongRoundIdReturnsClientError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + "Not-A-valid-id" + "/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssuedWithJurisdictionEndpointDownReturnsFailedDependency() throws Exception {
        given(questionService.issueQuestion(any(Question.class))).willReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + QUESTION_ID + "/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isFailedDependency())
                .andReturn();
    }
}
