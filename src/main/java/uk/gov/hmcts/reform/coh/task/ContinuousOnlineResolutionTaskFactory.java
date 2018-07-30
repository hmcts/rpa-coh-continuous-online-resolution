package uk.gov.hmcts.reform.coh.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContinuousOnlineResolutionTaskFactory {

    @Autowired
    private Set<ContinuousOnlineResolutionTask> mappedTasks = new HashSet<>();

    public void setTasks(List<ContinuousOnlineResolutionTask> tasks) {
        tasks.forEach( t -> mappedTasks.add(t) );
    }

    public ContinuousOnlineResolutionTask getTask(String key) {
        for (ContinuousOnlineResolutionTask task : mappedTasks) {
            if (task.supports().contains(key)) {
                return task;
            }
        }

        return null;
    }
}
