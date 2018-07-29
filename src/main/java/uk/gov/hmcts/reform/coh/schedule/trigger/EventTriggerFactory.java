package uk.gov.hmcts.reform.coh.schedule.trigger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EventTriggerFactory {

    @Autowired
    private Set<EventTrigger> triggers  = new HashSet<>();

    public void setTriggers(List<EventTrigger> triggers) {
        triggers.forEach(triggers::add);
    }

    public Set<EventTrigger> getTriggers() {
        return triggers;
    }
}
