package uk.gov.hmcts.reform.coh.schedule.notifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to manage all the interfaces in the app
 */
@Service
public class EventTransformerFactory {

    @Autowired
    private Set<EventTransformer> eventTransformers = new HashSet<EventTransformer>();

    /**
     * Add all the Spring managed interfaces to an internal map
     * @param transformers
     */
    public void setEvenTransformers(List<EventTransformer> transformers) {
        transformers.forEach(t -> eventTransformers.add(t));
    }

    public EventTransformer getEventTransformer(String key) {

        for (EventTransformer transformer : eventTransformers) {
            if (transformer.supports().contains(key)) {
                return transformer;
            }
        }

        return null;
    }
}