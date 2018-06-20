package uk.gov.hmcts.reform.coh.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;

public class JurisdictionService {

    private JurisdictionRepository jurisdictionRepository;

    public ResponseEntity<QuestionRound> issueQuestions(Integer oh_id) {

        return null;
    }
}
