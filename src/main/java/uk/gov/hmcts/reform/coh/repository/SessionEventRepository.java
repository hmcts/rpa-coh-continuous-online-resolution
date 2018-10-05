package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingRegister;
import uk.gov.hmcts.reform.coh.domain.SessionEventForwardingState;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionEventRepository extends CrudRepository<SessionEvent, UUID> {

    List<SessionEvent> findAllByOnlineHearing(OnlineHearing onlineHearing);

    @EntityGraph(value = "graph.SessionEvent.onlineHearing.onlineHearingStateHistories", type = EntityGraphType.LOAD)
    List<SessionEvent> findAllBySessionEventForwardingState(SessionEventForwardingState sessionEventForwardingState);

    void deleteByOnlineHearing(OnlineHearing onlineHearing);

    List<SessionEvent> findAllBySessionEventForwardingRegister(SessionEventForwardingRegister sessionEventForwardingRegister);

    List<SessionEvent> findAllBySessionEventForwardingRegisterAndSessionEventForwardingState(SessionEventForwardingRegister sessionEventForwardingRegister, SessionEventForwardingState eventForwardingState);
}
