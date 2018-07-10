package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.Onlinehearing;
import uk.gov.hmcts.reform.coh.domain.OnlinehearingPanelMember;

@Repository
public interface OnlinehearingPanelMemberRepository extends CrudRepository<OnlinehearingPanelMember,Long> {

    @Transactional
    void deleteByOnlinehearing(Onlinehearing onlinehearing);
}