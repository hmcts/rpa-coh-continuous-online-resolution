package uk.gov.hmcts.reform.coh.controller.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;
import uk.gov.hmcts.reform.coh.domain.SessionEvent;

import javax.validation.constraints.*;

public class EventRegistrationRequest extends SessionEventRequest {

    @NotNull(message = "Endpoint Required")
    @JsonProperty("endpoint") @NotBlank @URL
    @ApiModelProperty(required = true, value = "An absolute URL corresponding to the callback endpoint for the event")
    private String endpoint;

    @PositiveOrZero @Max(5)
    @JsonProperty("maximum_retries")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(allowableValues = "range[0,5]", value = "The number of retry attempt to send a notification message. Default 3")
    private Integer maxRetries = 3;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
}
