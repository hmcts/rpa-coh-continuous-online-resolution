package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.OnlinehearingPanelMember;
import uk.gov.hmcts.reform.coh.repository.OnlinehearingPanelMemberRepository;

@Service
@Component
public class OnlinehearingPanelMemberService {

    private OnlinehearingPanelMemberRepository onlinehearingPanelMemberRepository;

    @Autowired
    public OnlinehearingPanelMemberService(OnlinehearingPanelMemberRepository onlinehearingPanelMemberRepository) {
        this.onlinehearingPanelMemberRepository = onlinehearingPanelMemberRepository;
    }

    public OnlinehearingPanelMember createOnlinehearing(final OnlinehearingPanelMember onlinehearingPanelMember) {
        return onlinehearingPanelMemberRepository.save(onlinehearingPanelMember);
    }

    public void deleteByOnlinehearing(Onlinehearing onlinehearing) {
        onlinehearingPanelMemberRepository.deleteByOnlinehearing(onlinehearing);
    }
}