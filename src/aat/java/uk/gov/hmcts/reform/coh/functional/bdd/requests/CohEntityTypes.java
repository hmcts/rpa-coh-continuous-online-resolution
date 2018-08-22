package uk.gov.hmcts.reform.coh.functional.bdd.requests;

public enum CohEntityTypes {
    ONLINE_HEARING;

    public String getString() {
        return name().replaceAll("_", " ").toLowerCase();
    }
}
