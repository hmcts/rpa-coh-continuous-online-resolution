package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class OnlineHearingTimelinesResponse {

    @JsonProperty("online_hearing_id")
    private UUID onlineHearingId;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("start_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date startDate;

    @JsonProperty(value = "end_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date endDate;

    @JsonProperty("panel")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OnlineHearingResponse.PanelMember> panel;

    @JsonProperty(value = "current_state")
    private StateResponse currentState = new StateResponse();

    @JsonProperty(value = "history")
    private List<StateResponse> histories = new ArrayList<>();

    @JsonProperty(value = "question")

    public UUID getOnlineHearingId() {
        return onlineHearingId;
    }

    public void setOnlineHearingId(UUID onlineHearingId) {
        this.onlineHearingId = onlineHearingId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<OnlineHearingResponse.PanelMember> getPanel() {
        return panel;
    }

    public void setPanel(List<OnlineHearingResponse.PanelMember> panel) {
        this.panel = panel;
    }

    public StateResponse getCurrentState() {
        return currentState;
    }

    public void setCurrentState(StateResponse currentState) {
        this.currentState = currentState;
    }

    public List<StateResponse> getHistories() {
        return histories;
    }

    public void setHistories(List<StateResponse> histories) {
        this.histories = histories;
    }

    public static class PanelMember {
        @JsonProperty("name")
        private String name;

        public PanelMember() {
            super();
        }

        public PanelMember(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
