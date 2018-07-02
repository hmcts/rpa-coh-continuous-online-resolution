package uk.gov.hmcts.reform.coh.service;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

import java.util.List;
import java.util.Optional;

@Service
@Component
public class QuestionRoundService {

    @Autowired
    private JurisdictionService jurisdictionService;

    public QuestionRound getQuestionRound(OnlineHearing onlineHearing, Optional<List<Question>> optionalQuestions) throws NotFoundException {
        Optional<Jurisdiction> optionalJurisdiction = jurisdictionService.getJurisdictionWithName(onlineHearing.getJurisdictionName());
        if(!optionalJurisdiction.isPresent()){
            throw new NotFoundException("Error: No jurisdiction assigned to online hearing -" + onlineHearing.getOnlineHearingId());
        }

        QuestionRound questionRound = new QuestionRound();

        questionRound.setQuestionList(optionalQuestions.get());

        Integer nextQuestionRound = getNextQuestionRound(optionalJurisdiction.get());

        questionRound.setNextQuestionRound();

    }

    private Integer getNextQuestionRound(Jurisdiction jurisdiction, ) {
        int maxQuestionRounds = jurisdiction.getMaxQuestionRounds();
    }


}
