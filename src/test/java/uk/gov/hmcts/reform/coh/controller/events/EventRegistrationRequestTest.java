package uk.gov.hmcts.reform.coh.controller.events;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventRegistrationRequestTest {

    private EventRegistrationRequest request;
    private static final String ENDPOINT = "www.foo.com";
    @Before
    public void setUp() {
        request = new EventRegistrationRequest();
        request.setActive(false);
        request.setEndpoint(ENDPOINT);
        request.setMaxRetries(99);
    }

    @Test
    public void getEndpoint() {
        assertEquals(ENDPOINT, request.getEndpoint());
    }

    @Test
    public void setEndpoint() {
        request.setEndpoint(ENDPOINT + "A");
        assertEquals(ENDPOINT + "A", request.getEndpoint() );
    }

    @Test
    public void getMaxRetries() {
        assertEquals(99, request.getMaxRetries().intValue());
    }

    @Test
    public void setMaxRetries() {
        request.setMaxRetries(1);
        assertEquals(1, request.getMaxRetries().intValue());
    }

    @Test
    public void getActive() {
        assertEquals(false, request.getActive());
    }

    @Test
    public void setActive() {
        request.setActive(true);
        assertEquals(true, request.getActive());
    }
}