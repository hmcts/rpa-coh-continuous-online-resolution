package uk.gov.hmcts.reform.coh.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.coh.controller.exceptions.ResourceNotFoundException;
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
        Optional<Jurisdiction> jurisdiction = jurisdictionRepository.findByJurisdictionName(jurisdictionName);

        if (!jurisdiction.isPresent()){
            throw new ResourceNotFoundException("Jurisdiction Not Found");
        }

        return jurisdiction;
    }
}