package uk.gov.hmcts.reform.coh.controller.testHarness;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.coh.controller.testharness.SSCSNotificationController;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local"})
public class SSCSEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private SSCSNotificationController sscsNotificationController;

    @Before
    public void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(sscsNotificationController).build();
    }

    @Test
    public void testTestHarness() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/SSCS/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""));
    }
}
