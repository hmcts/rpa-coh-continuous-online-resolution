package uk.gov.hmcts.reform.coh.schedule.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventInterfaceManager {

/**
 * Utility class to manage all the interceptors in the app 
 */

    @Autowired
    private Map<String, EventInterface> interceptors = new HashMap<String, EventInterface>();

    /**
     * Add all the Spring managed interceptors to an internal map
     * @param iter
     */
    public void setInterceptors(List<EventInterface> iter) {
        for (EventInterface interceptor : iter) {
            interceptors.put(interceptor.getName(), interceptor);
        }
    }

    public EventInterface getInterceptor(String key) {
        return interceptors.get(key);
    }

    public Map<String, EventInterface> getInterceptors() {
        return Collections.unmodifiableMap(this.interceptors);
    }
}