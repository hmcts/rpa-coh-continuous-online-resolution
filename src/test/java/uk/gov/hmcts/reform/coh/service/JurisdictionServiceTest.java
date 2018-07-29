package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.repository.JurisdictionRepository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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