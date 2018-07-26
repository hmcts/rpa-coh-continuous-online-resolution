package uk.gov.hmcts.reform.coh.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.coh.controller.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.coh.repository.SessionEventTypeRespository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(SpringRunner.class)
public class EventTypeServiceTest {
    @Mock
    private SessionEventTypeRespository sessionEventTypeRespository;
    @Mock
    private EventTypeService eventTypeService;

    @Before
    public void setup() {
        eventTypeService = new EventTypeService(sessionEventTypeRespository);

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testInvalidJurisdictionThrowsResourceNotFoundException() throws Exception {
        eventTypeService.retrieveEventType("Chocolate");
    }

    @Test
    public void testExceptionMessage() throws Exception {
        try {
            eventTypeService.retrieveEventType("Chocolate");
        } catch (ResourceNotFoundException e){
            assertThat(e.getMessage(), is("EventType Not Found"));
        }
    }

}