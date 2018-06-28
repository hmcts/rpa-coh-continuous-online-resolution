package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;

import java.util.Optional;

@Service
@Component
public class OnlineHearingPanelMemberService {

    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Autowired
    public OnlineHearingPanelMemberService(OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository) {
        this.onlineHearingPanelMemberRepository = onlineHearingPanelMemberRepository;
    }

    public OnlineHearingPanelMember createOnlineHearing(final OnlineHearingPanelMember onlineHearingPanelMember) {
        return onlineHearingPanelMemberRepository.save(onlineHearingPanelMember);
    }
}