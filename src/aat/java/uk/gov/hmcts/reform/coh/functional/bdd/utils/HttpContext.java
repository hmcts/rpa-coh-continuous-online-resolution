package uk.gov.hmcts.reform.coh.functional.bdd.utils;

import org.springframework.stereotype.Component;

@Component
public class HttpContext {

    private String rawResponseString;

    private int httpResponseStatusCode;

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
