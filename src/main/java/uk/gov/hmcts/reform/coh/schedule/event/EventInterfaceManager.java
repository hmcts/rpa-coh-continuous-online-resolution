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
 * Utility class to manage all the interfaces in the app 
 */

    @Autowired
    private Map<String, EventInterface> eventInterfaces = new HashMap<String, EventInterface>();

    /**
     * Add all the Spring managed interfaces to an internal map
     * @param iter
     */
    public void setInterfaces(List<EventInterface> iter) {
        for (EventInterface eventInterface : iter) {
            eventInterfaces.put(eventInterface.getName(), eventInterface);
        }
    }

    public EventInterface getInterface(String key) {
        return eventInterfaces.get(key);
    }

    public Map<String, EventInterface> getInterfaces() {
        return Collections.unmodifiableMap(this.eventInterfaces);
    }
}