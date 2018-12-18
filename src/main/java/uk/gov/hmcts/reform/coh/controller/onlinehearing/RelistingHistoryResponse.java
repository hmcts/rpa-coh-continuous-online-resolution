package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.RelistingState;

import java.io.Serializable;
import java.util.Date;

public class RelistingHistoryResponse implements Serializable {

    private final String reason;
    private final RelistingState state;
    private final Date dateOccurred;

    public RelistingHistoryResponse(
        @JsonProperty("reason") String reason,
        @JsonProperty("state") RelistingState state,
        @JsonProperty("date_occurred") Date dateOccurred
    ) {
        this.reason = reason;
        this.state = state;
        this.dateOccurred = dateOccurred;
    }

    public String getReason() {
        return reason;
    }

    public RelistingState getState() {
        return state;
    }

    public Date getDateOccurred() {
        return dateOccurred;
    }
}
