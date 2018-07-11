package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.repository.DecisionRepository;

import java.util.*;

@Service
public class DecisionService {

    private DecisionRepository decisionRepository;

    @Autowired
    public DecisionService(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    public Decision createDecision(Decision decision) {
        return decisionRepository.save(decision);
    }

    public Optional<Decision> findByOnlineHearingId(UUID onlineHearingId) {
        return decisionRepository.findByOnlineHearingOnlineHearingId(onlineHearingId);
    }

    public Optional<Decision> retrieveByOnlineHearingIdAndDecisionId(UUID onlineHearingId, UUID decisionId) {
        return decisionRepository.findByOnlineHearingOnlineHearingIdAndDecisionId(onlineHearingId, decisionId);
    }

    /**
     * Hard-coded to 7 days.
     * @return
     */
    public Date getDeadlineExpiryDate() {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, 6);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        return calendar.getTime();
    }
}
