package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

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

    public OnlineHearing retrieveOnlineHearingByExternalRef(final OnlineHearing onlineHearing) {
        return onlineHearingRepository.findByExternalRef(onlineHearing.getExternalRef()).orElse(null);
    }

    public void deleteOnlineHearingByExternalRef(final OnlineHearing onlineHearing){
        onlineHearingRepository.delete(retrieveOnlineHearingByExternalRef(onlineHearing));
    }


    public void deleteOnlineHearing(final OnlineHearing onlineHearing) {
        onlineHearingRepository.delete(onlineHearing);
    }
}