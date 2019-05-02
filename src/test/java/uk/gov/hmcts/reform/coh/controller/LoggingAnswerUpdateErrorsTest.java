package uk.gov.hmcts.reform.coh.controller;

import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.appinsights.EventRepository;
import uk.gov.hmcts.reform.coh.controller.answer.AnswerRequest;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.service.AnswerService;
import uk.gov.hmcts.reform.coh.service.AnswerStateService;
import uk.gov.hmcts.reform.coh.service.OnlineHearingService;
import uk.gov.hmcts.reform.coh.service.QuestionService;
import uk.gov.hmcts.reform.coh.service.QuestionStateService;
import uk.gov.hmcts.reform.coh.service.SessionEventService;
import uk.gov.hmcts.reform.coh.states.AnswerStates;
import uk.gov.hmcts.reform.coh.states.QuestionStates;
import uk.gov.hmcts.reform.coh.util.OnlineHearingEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionEntityUtils;
import uk.gov.hmcts.reform.coh.util.QuestionStateUtils;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class LoggingAnswerUpdateErrorsTest {

    @Mock
    private OnlineHearingService onlineHearingService;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionStateService questionStateService;

    @Mock
    private AnswerService answerService;

    @Mock
    private AnswerStateService answerStateService;

    @Mock
    private SessionEventService sessionEventService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private AnswerController answerController;

    @Autowired
    private MockMvc mockMvc;

    private static final String onlineHearingId = "d9248584-4aa5-4cb0-aba6-d2633ad5a375";
    private static final String questionId = "f93b4d57-709e-4d55-b45b-dcbfbea3303b";
    private static final String ENDPOINT =
        "/continuous-online-hearings/" + onlineHearingId + "/questions/" + questionId + "/answers";

    private AnswerRequest request;

    private Answer answer;

    private UUID uuid;

    private OnlineHearing onlineHearing;

    private AnswerState draftedState;

    private AnswerState submittedState;

    private Question question;

    private String jsonRequest;

    @Captor
    private ArgumentCaptor<String> trackedEventName;

    @Captor
    private ArgumentCaptor<Map<String, String>> trackedEventProperties;

    @Before
    public void setup() throws IOException, NotFoundException {
        jsonRequest = JsonUtils.getJsonInput("answer/standard_answer");

        answer = new Answer();
        uuid = UUID.fromString("399388b4-7776-40f9-bb79-0e900807063b");
        answer.answerId(uuid).answerText("foo");

        draftedState = new AnswerState();
        draftedState.setState(AnswerStates.DRAFTED.getStateName());
        draftedState.setAnswerStateId(1);
        answer.setAnswerState(draftedState);

        submittedState = new AnswerState();
        submittedState.setState(AnswerStates.SUBMITTED.getStateName());
        submittedState.setAnswerStateId(2);

        mockMvc = MockMvcBuilders.standaloneSetup(answerController).build();

        onlineHearing = OnlineHearingEntityUtils.createTestOnlineHearingEntity();
        onlineHearing.setOnlineHearingId(UUID.fromString(onlineHearingId));

        question = QuestionEntityUtils.createTestQuestion(QuestionStates.ISSUED);
        question.setOnlineHearing(onlineHearing);
        question.setQuestionId(UUID.fromString(questionId));

        given(sessionEventService.createSessionEvent(any(OnlineHearing.class), anyString()))
            .willReturn(new SessionEvent());
        given(questionService.retrieveQuestionById(any(UUID.class))).willReturn(Optional.of(question));
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.ofNullable(answer));
        given(answerService.createAnswer(any(Answer.class))).willReturn(answer);
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willReturn(answer);
        given(answerStateService.retrieveAnswerStateByState(draftedState.getState()))
            .willReturn(Optional.ofNullable(draftedState));
        given(answerStateService.retrieveAnswerStateByState(submittedState.getState()))
            .willReturn(Optional.ofNullable(submittedState));
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class)))
            .willReturn(Optional.ofNullable(onlineHearing));
        given(questionStateService.fetchQuestionState(QuestionStates.ANSWERED))
            .willReturn(QuestionStateUtils.get(QuestionStates.ANSWERED));

        request = JsonUtils.toObjectFromTestName("answer/standard_answer", AnswerRequest.class);
    }

    @Test
    public void onlineHearingNotFound() throws Exception {
        given(onlineHearingService.retrieveOnlineHearing(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest));

        verify(eventRepository, times(1)).trackEvent(trackedEventName.capture(), trackedEventProperties.capture());

        assertThat(trackedEventName.getValue()).isEqualTo("Online hearing not found");
        assertThat(trackedEventProperties.getValue())
            .containsEntry("onlineHearingId", onlineHearingId);
    }

    @Test
    public void invalidAnswerState() throws Exception {
        given(answerStateService.retrieveAnswerStateByState(anyString())).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest));

        verify(eventRepository, times(1)).trackEvent(trackedEventName.capture(), trackedEventProperties.capture());

        assertThat(trackedEventName.getValue()).isEqualTo("Invalid answer state");
    }

    @Test
    public void answerDoesNotExist() throws Exception {
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest));

        verify(eventRepository, times(1)).trackEvent(trackedEventName.capture(), trackedEventProperties.capture());

        assertThat(trackedEventName.getValue()).isEqualTo("Answer does not exist");
        assertThat(trackedEventProperties.getValue())
            .containsEntry("onlineHearingId", onlineHearingId)
            .containsEntry("requestedAnswerId", answer.getAnswerId().toString());
    }

    @Test
    public void answerAlreadySubmitted() throws Exception {
        draftedState.setState(AnswerStates.SUBMITTED.getStateName());
        answer.setAnswerState(draftedState);
        given(answerService.retrieveAnswerById(any(UUID.class))).willReturn(Optional.of(answer));

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest));

        verify(eventRepository, times(1)).trackEvent(trackedEventName.capture(), trackedEventProperties.capture());

        assertThat(trackedEventName.getValue()).isEqualTo("Submitted answers cannot be updated");
        assertThat(trackedEventProperties.getValue())
            .containsEntry("onlineHearingId", onlineHearingId)
            .containsEntry("answerId", answer.getAnswerId().toString())
            .containsEntry("state", answer.getAnswerState().getState())
            .containsEntry("requestedState", "answer_drafted");
    }

    @Test
    public void answerStateNotFound() throws Exception {
        given(answerService.updateAnswer(any(Answer.class), any(Answer.class))).willThrow(new NotFoundException("a"));

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT + "/" + uuid)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest));

        verify(eventRepository, times(1)).trackEvent(trackedEventName.capture(), trackedEventProperties.capture());

        assertThat(trackedEventName.getValue()).isEqualTo("Not found answer error");
        assertThat(trackedEventProperties.getValue())
            .containsEntry("onlineHearingId", onlineHearingId)
            .containsEntry("answerId", answer.getAnswerId().toString())
            .containsKeys("requestedState", "error");
    }
}
