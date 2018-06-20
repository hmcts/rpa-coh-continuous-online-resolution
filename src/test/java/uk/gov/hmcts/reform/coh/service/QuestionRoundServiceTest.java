package uk.gov.hmcts.reform.coh.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRoundRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Configuration
public class QuestionRoundServiceTest {

    @Autowired
    private QuestionRoundService questionRoundService;

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private QuestionRoundRepository questionRoundRepository;

    @Before
    public void setup(){
    }

    @After
    public void teardown(){
        QuestionRound questionRound = new QuestionRound();
        questionRound.setState_id(2);
        questionRound.setQuestionRoundId(1);
        questionRoundRepository.save(questionRound);
    }

    @Test
    public void testUpdateQuestionRoundToIssued(){
        questionRoundService.issueQuestions("case_id_123", 1);
    }


}
