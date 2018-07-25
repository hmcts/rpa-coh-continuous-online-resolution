package uk.gov.hmcts.reform.coh.schedule.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to manage all the interfaces in the app
 */
@Service
public class EventInterfaceManager {

    @Autowired
    private Map<String, EventTransformer> eventTransformers = new HashMap<String, EventTransformer>();

    /**
     * Add all the Spring managed interfaces to an internal map
     * @param iter
     */
    public void setEvenTransformers(List<EventTransformer> iter) {
        for (EventTransformer eventInterface : iter) {
            eventTransformers.put(eventInterface.getName(), eventInterface);
        }
    }

    public EventTransformer getEventTransformer(String key) {
        return eventTransformers.get(key);
    }

    public Map<String, EventTransformer> getEventTransformers() {
        return Collections.unmodifiableMap(this.eventTransformers);
    }
}