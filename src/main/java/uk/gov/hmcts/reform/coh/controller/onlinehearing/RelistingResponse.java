package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.hmcts.reform.coh.domain.RelistingState;

import java.util.Date;

public class RelistingResponse {
    private final String reason;
    private final RelistingState state;

    @ApiModelProperty(value = "ISO-8601 format: yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private final Date created;

    @ApiModelProperty(value = "ISO-8601 format: yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private final Date updated;

    public RelistingResponse(
        @JsonProperty("reason") String reason,
        @JsonProperty("state") RelistingState state,
        @JsonProperty("created") Date created,
        @JsonProperty("updated") Date updated
    ) {
        this.reason = reason;
        this.state = state;
        this.created = created;
        this.updated = updated;
    }

    public String getReason() {
        return reason;
    }

    public RelistingState getState() {
        return state;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }
}
