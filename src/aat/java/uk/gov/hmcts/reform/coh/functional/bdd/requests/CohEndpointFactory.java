package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CohEndpointFactory {

    @Autowired
    private List<CohEndpointHandler> requestEndpoints;

    private static final Map<String, CohEndpointHandler> mappedRequestEndpoints = new HashMap<>();

    @PostConstruct
    public void setRequestEndpoints() {
        requestEndpoints.forEach(r -> mappedRequestEndpoints.put(r.supports(), r));
    }

    public static CohEndpointHandler getRequestEndpoint(String type) {
        return mappedRequestEndpoints.get(type);
    }
}
