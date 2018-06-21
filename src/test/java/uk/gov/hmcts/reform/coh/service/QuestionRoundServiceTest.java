package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.coh.Notification.QuestionRoundDespatcher;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;

import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class QuestionRoundServiceTest {

    @InjectMocks
    private QuestionRoundService questionRoundService;

    @Mock
    private QuestionRoundRepository questionRoundRepository;

    @Mock
    private QuestionRoundDespatcher questionRoundDespatcher;

    private QuestionRound questionRound;

    private UUID questionRoundId = UUID.fromString("d6248584-4aa5-4cb0-aba6-d2633ad5a375");

    @Mock
    private Jurisdiction jurisdiction;

    @Before
    public void setup(){
        jurisdiction = mock(Jurisdiction.class);
        when(jurisdiction.getJurisdictionId()).thenReturn(1);
        when(jurisdiction.getJurisdictionName()).thenReturn("SSCS");
        when(jurisdiction.getUrl()).thenReturn("http://SSCS-endpoint.com");

        OnlineHearing onlineHearing = mock(OnlineHearing.class);
        when(onlineHearing.getJurisdiction()).thenReturn(jurisdiction);

        QuestionState questionState = new QuestionState();
        questionState.setQuestionStateId(2);
        questionState.setState("SUBMITTED");

        questionRound = new QuestionRound();
        questionRound.setOnlineHearing(onlineHearing);
        questionRound.setQuestionState(questionState);

        when(questionRoundRepository.findById(any(UUID.class))).thenReturn(Optional.ofNullable(questionRound));
        when(questionRoundRepository.save(any(QuestionRound.class))).thenReturn(new QuestionRound());

        when(questionRoundDespatcher.sendRequestToJuridiction(any(Jurisdiction.class),any(QuestionRound.class)))
                .thenReturn(new ResponseEntity<>("SSCS response body", HttpStatus.OK));
    }

    @Test
    public void testGetQuestionRoundReturnsQuestionRoundObject(){
        Optional<QuestionRound> questionRound = questionRoundService.getQuestionRound(questionRoundId);
        assertTrue(questionRound.isPresent());
    }

    @Test
    public void testRequestToSSCSEndpointWhenDownReturnsFalse(){
        when(questionRoundDespatcher.sendRequestToJuridiction(any(Jurisdiction.class),any(QuestionRound.class)))
                .thenReturn(new ResponseEntity<>("Boilerplate failure body", HttpStatus.SERVICE_UNAVAILABLE));
        boolean success = questionRoundService.setStateToIssued(jurisdiction, questionRound);
        assertEquals(false, success);
    }

    @Test
    public void testUpdateQuestionRoundToIssued(){
        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(questionRoundId);
        questionRound = optQuestionRound.get();
        boolean success = questionRoundService.notifyJurisdictionToIssued(questionRound);
        assertEquals(true, success);
    }

    @Test
    public void testNotifyJurisdictionToIssuedUpdatesQuestionRoundState(){
        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(questionRoundId);
        questionRound = optQuestionRound.get();
        questionRoundService.notifyJurisdictionToIssued(questionRound);
        assertEquals(3, questionRound.getQuestionState().getQuestionStateId());
    }
}
