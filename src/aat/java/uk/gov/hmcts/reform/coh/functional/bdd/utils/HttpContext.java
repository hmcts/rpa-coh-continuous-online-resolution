package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicResponseHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

@Component
public class HttpContext {

    private String rawResponseString;

    private int httpResponseStatusCode;

    public void setResponseBodyAndStatesForResponse(HttpResponse httpResponse) throws IOException {
        rawResponseString = new BasicResponseHandler().handleResponse(httpResponse);
        httpResponseStatusCode = httpResponse.getStatusLine().getStatusCode();
    }

    public void setResponseBodyAndStatesForResponse(ResponseEntity<String> responseEntity) throws IOException {
        rawResponseString = responseEntity.getBody();
        httpResponseStatusCode = responseEntity.getStatusCodeValue();
    }

    public void setResponseBodyAndStatesForException(HttpClientErrorException hcee) {
        rawResponseString = hcee.getResponseBodyAsString();
        httpResponseStatusCode = hcee.getRawStatusCode();
    }

    public String getRawResponseString() {
        return rawResponseString;
    }

    public void setRawResponseString(String rawResponseString) {
        this.rawResponseString = rawResponseString;
    }

    public int getHttpResponseStatusCode() {
        return httpResponseStatusCode;
    }

    public void setHttpResponseStatusCode(int httpResponseStatusCode) {
        this.httpResponseStatusCode = httpResponseStatusCode;
    }
}
