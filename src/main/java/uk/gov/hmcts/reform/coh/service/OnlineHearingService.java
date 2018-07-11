package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Optional<OnlineHearing> retrieveOnlineHearing(UUID onlineHearingId) {
        return onlineHearingRepository.findById(onlineHearingId);
    }

    public Optional<OnlineHearing> retrieveOnlineHearing(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findById(onlineHearing.getOnlineHearingId());
    }

    public OnlineHearing retrieveOnlineHearingByCaseId(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findByCaseId(onlineHearing.getCaseId()).orElse(null);
    }

    public List<OnlineHearing> retrieveOnlineHearingByCaseIds(List<String> caseIds) {
        return onlineHearingRepository.findAllByCaseIdIn(caseIds);
    }

    public List<OnlineHearing> retrieveOnlineHearingByCaseIds(List<String> caseIds, Optional<Set<String>> states) {

        /**
         * Filter to accept a case only if it's in the list of states requested.
         * This is easier than trying to force JPA to execute a custom SQL
         */
        List<OnlineHearing> onlineHearings = retrieveOnlineHearingByCaseIds(caseIds);
        if (states.isPresent()) {
            onlineHearings = onlineHearings
                    .stream()
                    .filter(o -> states.get().contains(o.getOnlineHearingState().getState()))
                    .collect(Collectors.toList());
        }

        return onlineHearings;
    }

    public void deleteOnlineHearing(final OnlineHearing onlineHearing) {
        onlineHearingRepository.deleteById(onlineHearing.getOnlineHearingId());
    }

    public void deleteByCaseId(String caseId) {
        onlineHearingRepository.deleteByCaseId(caseId);
    }

}