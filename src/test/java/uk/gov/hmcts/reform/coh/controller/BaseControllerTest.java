package uk.gov.hmcts.reform.coh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

abstract public class BaseControllerTest {

    protected ClassLoader classLoader = getClass().getClassLoader();

    public String getJsonInput(String testName) throws IOException {

        File testFile = getTestFile(testName);
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(mapper.readValue(testFile, Object.class));
    }

    private File getTestFile(String testName) throws FileNotFoundException  {
        String filename = "json/" + testName + ".json";
        File file = new File(Objects.requireNonNull(classLoader.getResource(filename)).getFile());

        if (!file.exists()) {
            throw new FileNotFoundException("Unable to find file " + filename);
        }
        return file;
    }
}
