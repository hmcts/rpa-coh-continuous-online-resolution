package uk.gov.hmcts.reform.coh.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.coh.domain.Onlinehearingstate;

import java.util.Optional;

@Repository
public interface OnlinehearingStateRepository extends CrudRepository<Onlinehearingstate, Integer> {
    Optional<Onlinehearingstate> findByState(String state);
}