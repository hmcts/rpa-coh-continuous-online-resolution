package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.hmcts.reform.coh.controller.decision.DecisionResponse;
import uk.gov.hmcts.reform.coh.controller.question.QuestionResponse;
import uk.gov.hmcts.reform.coh.controller.state.StateResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnlineHearingResponse implements Serializable {

    @ApiModelProperty
    @JsonProperty("online_hearing_id")
    private UUID onlineHearingId;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("start_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String startDate;

    @JsonProperty(value = "end_date")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String endDate;

    @JsonProperty("panel")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PanelMember> panel;

    @JsonProperty(value = "current_state")
    private StateResponse currentState = new StateResponse();

    @JsonProperty(value = "history")
    private List<StateResponse> histories = new ArrayList<>();

    @JsonProperty(value = "uri")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

    @JsonProperty(value = "decision")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DecisionResponse decisionResponse;

    @JsonProperty(value = "questions")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<QuestionResponse> questions;

    @JsonProperty(value = "relist_reason")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String relistReason;

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<PanelMember> getPanel() {
        return panel;
    }

    public void setPanel(List<PanelMember> panel) {
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public DecisionResponse getDecisionResponse() {
        return decisionResponse;
    }

    public void setDecisionResponse(DecisionResponse decisionResponse) {
        this.decisionResponse = decisionResponse;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }

    public String getRelistReason() {
        return relistReason;
    }

    public void setRelistReason(String relistReason) {
        this.relistReason = relistReason;
    }

    public static class PanelMember implements Serializable {
        @JsonProperty("name")
        private String name;

        @JsonProperty("role")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String role;

        public PanelMember() {
            super();
        }

        public PanelMember(String name, String role) {
            this.name = name;
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
