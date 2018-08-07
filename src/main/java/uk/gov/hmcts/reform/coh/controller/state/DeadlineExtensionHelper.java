package uk.gov.hmcts.reform.coh.controller.state;

import java.util.concurrent.atomic.AtomicLong;

public class DeadlineExtensionHelper {

    private AtomicLong eligible = new AtomicLong(0);
    private AtomicLong granted = new AtomicLong(0);
    private AtomicLong denied = new AtomicLong(0);

    public DeadlineExtensionHelper(long eligible, long granted, long denied) {
        this.eligible = new AtomicLong(eligible);
        this.granted = new AtomicLong(granted);
        this.denied = new AtomicLong(denied);
    }

    public Long getEligible() {
        return eligible.get();
    }

    public Long getAndIncrementEligible () {
        return eligible.getAndIncrement();
    }

    public Long getGranted() {
        return granted.get();
    }

    public Long getAndIncrementGranted() {
        return granted.getAndIncrement();
    }

    public Long getDenied() {
        return denied.get();
    }

    public Long getAndIncrementDenied() {
        return denied.getAndIncrement();
    }
}
