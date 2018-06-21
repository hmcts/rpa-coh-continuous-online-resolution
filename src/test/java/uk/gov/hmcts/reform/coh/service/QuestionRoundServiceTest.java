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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class QuestionRoundServiceTest {

    @Autowired
    private QuestionRoundService questionRoundService;

    @Autowired
    private QuestionRoundRepository questionRoundRepository;

    private QuestionRound questionRound;

    @After
    public void teardown(){
        QuestionState questionState = new QuestionState();
        questionState.setQuestionStateId(2);

        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(1);
        questionRound = optQuestionRound.get();
        questionRound.setQuestionState(questionState);
        questionRound.setQuestionRoundId(1);

        questionRoundRepository.save(questionRound);
    }

    @Test
    public void testGetQuestionRoundUsingId(){

    }

    @Test
    public void testUpdateQuestionRoundToIssued(){
        Optional<QuestionRound> optQuestionRound = questionRoundService.getQuestionRound(1);
        questionRound = optQuestionRound.get();
        questionRoundService.notifyJurisdiction(questionRound);
    }
}
