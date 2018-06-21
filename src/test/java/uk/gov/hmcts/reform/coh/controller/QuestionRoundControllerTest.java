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
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;

import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class QuestionRoundControllerTest {

    @Mock
    private QuestionRoundService questionRoundService;

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/online-hearings/d9248584-4aa5-4cb0-aba6-d2633ad5a375/questionrounds";

    private static final UUID QUESTION_ROUND_ID = UUID.fromString("d6248584-4aa5-4cb0-aba6-d2633ad5a375");

    @InjectMocks
    private QuestionRoundController questionRoundController;

    @Before
    public void setup() {
        QuestionRound questionRound = new QuestionRound();
        questionRound.setQuestionRoundId(QUESTION_ROUND_ID);

        mockMvc = MockMvcBuilders.standaloneSetup(questionRoundController).build();
        given(questionRoundService.getQuestionRound(QUESTION_ROUND_ID)).willReturn(java.util.Optional.of(questionRound));
        given(questionRoundService.notifyJurisdiction(any(QuestionRound.class))).willReturn(true);
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssued() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + QUESTION_ROUND_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals("{\"questionRoundId\":\"d6248584-4aa5-4cb0-aba6-d2633ad5a375\",\"roundNumber\":0}", response);
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssuedWithNullRoundIdReturnsClientError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssuedWithWrongRoundIdReturnsClientError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + "Not-A-valid-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @Test
    public void testGetRequestToSetQuestionRoundStateToIssuedWithJurisdictionEndpointDownReturnsFailedDependency() throws Exception {
        given(questionRoundService.notifyJurisdiction(any(QuestionRound.class))).willReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/" + QUESTION_ROUND_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isFailedDependency())
                .andReturn();
    }
}
