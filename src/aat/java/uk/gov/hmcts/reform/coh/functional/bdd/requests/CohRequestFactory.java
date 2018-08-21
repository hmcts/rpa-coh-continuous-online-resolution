package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CohRequestFactory {

    @Autowired
    private List<CohRequestEndpoint> requestEndpoints;

    private static final Map<String, CohRequestEndpoint> mappedRequestEndpoints = new HashMap<>();

    @PostConstruct
    public void setRequestEndpoints() {
        requestEndpoints.forEach(r -> mappedRequestEndpoints.put(r.supports(), r));
    }

    public static CohRequestEndpoint getRequestEndpoint(String type) {
        return mappedRequestEndpoints.get(type);
    }
}
