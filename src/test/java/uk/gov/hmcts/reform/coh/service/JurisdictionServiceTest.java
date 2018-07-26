package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequest;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionRequestMapper;
import uk.gov.hmcts.reform.coh.controller.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.coh.domain.Answer;
import uk.gov.hmcts.reform.coh.domain.Decision;
import uk.gov.hmcts.reform.coh.domain.DecisionState;
import uk.gov.hmcts.reform.coh.domain.Jurisdiction;
import uk.gov.hmcts.reform.coh.repository.DecisionRepository;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;
import uk.gov.hmcts.reform.coh.service.utils.ExpiryCalendar;
import uk.gov.hmcts.reform.coh.util.JsonUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class JurisdictionServiceTest {
    @Mock
    private JurisdictionRepository jurisdictionRepository;
    @Mock
    private JurisdictionService jurisdictionService;

    @Before
    public void setup() {
        jurisdictionService = new JurisdictionService(jurisdictionRepository);

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testInvalidJurisdictionThrowsResourceNotFoundException() throws Exception {
        jurisdictionService.getJurisdictionWithName("Chocolate");
    }

    @Test
    public void testExceptionMessage() throws Exception {
        try {
            jurisdictionService.getJurisdictionWithName("Chocolate");
        } catch (ResourceNotFoundException e){
            assertThat(e.getMessage(), is("Jurisdiction Not Found"));
        }
    }

}