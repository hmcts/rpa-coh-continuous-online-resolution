package uk.gov.hmcts.reform.coh.task;

public interface ContinuousOnlineResolutionTask<T> {

    void execute(T t);
}
