package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.repository.OnlinehearingRepository;

import java.util.Optional;

@Service
@Component
public class OnlinehearingService {

    private OnlinehearingRepository onlinehearingRepository;

    @Autowired
    public OnlinehearingService(OnlinehearingRepository onlinehearingRepository) {
        this.onlinehearingRepository = onlinehearingRepository;
    }

    public Onlinehearing createOnlinehearing(final Onlinehearing onlinehearing) {
        return onlinehearingRepository.save(onlinehearing);
    }

    public Optional<Onlinehearing> retrieveOnlinehearing(final Onlinehearing onlinehearing) {
        return onlinehearingRepository.findById(onlinehearing.getOnlinehearingId());
    }

    public Onlinehearing retrieveOnlinehearingByCaseId(final Onlinehearing onlinehearing) {
        return onlinehearingRepository.findByCaseId(onlinehearing.getCaseId()).orElse(null);
    }

    public void deleteOnlinehearing(final Onlinehearing onlinehearing) {
        onlinehearingRepository.deleteById(onlinehearing.getOnlinehearingId());
    }

    public void deleteByCaseId(String caseId) {
        onlinehearingRepository.deleteByCaseId(caseId);

    }

}