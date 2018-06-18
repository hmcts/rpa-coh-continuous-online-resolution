package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OnlineHearingControllerTest {

    @Autowired
    private MockMvc mvc;

    private static final String ENDPOINT = "/online-hearings";

    @Test
    public void testCreateOnlineHearingWithJsonFile() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("json/create_online_hearing.json")).getFile());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(mapper.readValue(file, Object.class));

        System.out.println("JSONSTRING" + jsonString);

        this.mvc.perform(MockMvcRequestBuilders.post("/online-hearings/")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(jsonString))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testReadOnlineHearingWithJsonFile() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "/case_id_123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }
}
