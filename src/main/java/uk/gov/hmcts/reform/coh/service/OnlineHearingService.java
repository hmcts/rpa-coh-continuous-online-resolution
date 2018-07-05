package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.List;
import java.util.Optional;

@Service
@Component
public class OnlineHearingService {

    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    public OnlineHearingService(OnlineHearingRepository onlineHearingRepository) {
        this.onlineHearingRepository = onlineHearingRepository;
    }

    public OnlineHearing createOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.save(onlineHearing);
    }

    public Optional<OnlineHearing> retrieveOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findById(onlineHearing.getOnlineHearingId());
    }

    public OnlineHearing retrieveOnlineHearingByCaseId(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findByCaseId(onlineHearing.getCaseId()).orElse(null);
    }

    public List<OnlineHearing> retrieveOnlineHearingByCaseIdAndState(List<String> caseIds, List<String> states) {
        return onlineHearingRepository.findByCaseIdAndState(caseIds, states);
    }

    public void deleteOnlineHearing(final OnlineHearing onlineHearing) {
        onlineHearingRepository.deleteById(onlineHearing.getOnlineHearingId());
    }

    public void deleteByCaseId(String externalRef) {
        onlineHearingRepository.deleteByCaseId(externalRef);
    }
}