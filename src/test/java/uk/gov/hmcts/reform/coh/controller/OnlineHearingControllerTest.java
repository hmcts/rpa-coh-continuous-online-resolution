package uk.gov.hmcts.reform.coh.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OnlineHearingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testCreateOnlineHearing() throws Exception {
        String json = "{\n" +
                "\t\"onlineHearingId\": 4,\n" +
                "\t\"externalRef\" : \"case_id_123\" \n" +
                "}";

        mvc.perform(MockMvcRequestBuilders.post("/online-hearings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

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

}
