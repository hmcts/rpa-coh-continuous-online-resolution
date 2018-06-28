package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnlineHearingPanelMemberRepository extends CrudRepository<OnlineHearingPanelMember,Long> {

}