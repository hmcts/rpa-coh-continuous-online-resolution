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
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionRoundService;
import uk.gov.hmcts.reform.coh.service.QuestionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class QuestionRoundControllerTest {

    @Mock
    private QuestionRoundService questionRoundService;

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuestionRoundController questionRoundController;

    @Autowired
    private MockMvc mockMvc;

    private UUID cohId;

    private static final String ENDPOINT = "/continuous-online-hearings/";

    @Before
    public void setup(){
        List<QuestionRound> questionRounds = new ArrayList<>();
        cohId = UUID.randomUUID();
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(cohId);
        given(onlineHearingService.retrieveOnlineHearing(any(OnlineHearing.class))).willReturn(java.util.Optional.of(onlineHearing));
        given(questionRoundService.getAllQuestionRounds(any(OnlineHearing.class))).willReturn(questionRounds);
        given(questionRoundService.getCurrentQuestionRoundNumber(any(OnlineHearing.class))).willReturn(2);
        given(questionRoundService.getNextQuestionRound(any(OnlineHearing.class), anyInt())).willReturn(3);
        given(questionRoundService.getPreviousQuestionRound(anyInt())).willReturn(1);
    }

    @Test
    public void testGetAllQuestionRounds() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + cohId + "/questionrounds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }
}
