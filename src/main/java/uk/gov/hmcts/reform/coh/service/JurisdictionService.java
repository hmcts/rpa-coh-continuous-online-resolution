package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;

@Service
@Component
public class JurisdictionService {

    private JurisdictionRepository jurisdictionRepository;

    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    public JurisdictionService(JurisdictionRepository jurisdictionRepository, OnlineHearingRepository onlineHearingRepository) {
        this.onlineHearingRepository = onlineHearingRepository;
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public ResponseEntity<QuestionRound> issueQuestions(String external_ref) {

        Optional<OnlineHearing> onlineHearing = onlineHearingRepository.findByExternalRef(external_ref);
        if(onlineHearing.isPresent()) {
            jurisdictionRepository.findById(onlineHearing.get().getJuridictionId());
        }
        return null;
    }
}
