package uk.gov.hmcts.reform.coh.controller.validators;

public class ValidationResult {
    private boolean isValid;
    private String reason;

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
