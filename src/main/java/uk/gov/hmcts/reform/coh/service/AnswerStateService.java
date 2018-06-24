package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.AnswerState;
import uk.gov.hmcts.reform.coh.repository.AnswerStateRepository;

import java.util.Optional;

@Service
@Component
public class AnswerStateService {

    private AnswerStateRepository answerStateRepository;

    @Autowired
    public AnswerStateService(AnswerStateRepository answerStateRepository) {
        this.answerStateRepository = answerStateRepository;
    }

    public Optional<AnswerState> retrieveAnswerStateByState(String state) {
        return answerStateRepository.findByState(state);
    }

}
