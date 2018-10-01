package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relisting {
    public final String reason;
    public final String state;

    public Relisting(
        @JsonProperty("reason") String reason,
        @JsonProperty("state") String state
    ) {
        this.reason = reason;
        this.state = state;
    }
}
