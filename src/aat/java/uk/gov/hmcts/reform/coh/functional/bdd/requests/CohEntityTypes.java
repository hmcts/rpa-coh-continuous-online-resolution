package uk.gov.hmcts.reform.coh.functional.bdd.requests;

public enum CohEntityTypes {
    ONLINE_HEARING,
    QUESTION,
    ANSWER,
    DECISION,
    DECISION_REPLY,
    CONVERSATIONS,
    EVENT;

    public String getString() {
        return name().replaceAll("_", " ").toLowerCase();
    }
}
