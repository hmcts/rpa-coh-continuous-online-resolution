package uk.gov.hmcts.reform.coh.schedule.trigger;

public interface EventTrigger {

    void execute();

    default int order() {
        return 0;
    };
}
