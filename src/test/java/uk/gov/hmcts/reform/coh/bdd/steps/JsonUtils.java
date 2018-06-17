package uk.gov.hmcts.reform.coh.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class JsonUtils {

    private static ClassLoader classLoader = JsonUtils.class.getClassLoader();

    public static String getJsonInput(String testName) throws IOException {

        File testFile = getTestFile(testName);
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(mapper.readValue(testFile, Object.class));
    }

    public Object toObject(String testName, Class clazz) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getJsonInput(testName), clazz);
    }

    private static File getTestFile(String testName) throws FileNotFoundException {
        String filename = "json/" + testName + ".json";
        File file = new File(Objects.requireNonNull(classLoader.getResource(filename)).getFile());

        if (!file.exists()) {
            throw new FileNotFoundException("Unable to find file " + filename);
        }
        return file;
    }
}
