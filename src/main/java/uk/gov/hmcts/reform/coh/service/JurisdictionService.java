package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;

import java.util.Optional;

@Service
@Component
public class JurisdictionService {

    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    public JurisdictionService(JurisdictionRepository jurisdictionRepository) {
        this.jurisdictionRepository = jurisdictionRepository;
    }

    public Optional<Jurisdiction> getJurisdictionWithName(String jurisdictionName) {
        return jurisdictionRepository.findByJurisdictionName(jurisdictionName);
    }
}