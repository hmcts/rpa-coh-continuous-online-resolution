package uk.gov.hmcts.reform.coh.service;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.domain.QuestionState;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;

import java.util.Optional;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class QuestionRoundServiceTest {

    @Autowired
    private QuestionRoundService questionRoundService;

    @Autowired
    private QuestionRoundRepository questionRoundRepository;

    private QuestionRound questionRound;

    private UUID questionRoundId = UUID.fromString("d6248584-4aa5-4cb0-aba6-d2633ad5a375");
    @After
    public void teardown(){
        QuestionState questionState = new QuestionState();
        questionState.setQuestionStateId(2);

        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(questionRoundId);
        questionRound = optQuestionRound.get();
        questionRound.setQuestionState(questionState);

        questionRoundRepository.save(questionRound);
    }

    @Test
    public void testUpdateQuestionRoundToIssued(){
        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(questionRoundId);
        questionRound = optQuestionRound.get();
        questionRoundService.notifyJurisdiction(questionRound);
    }
}
