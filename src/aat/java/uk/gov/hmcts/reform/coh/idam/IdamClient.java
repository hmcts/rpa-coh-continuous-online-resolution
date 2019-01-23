package uk.gov.hmcts.reform.coh.idam;

public interface IdamClient {

    void createAccount(String email, String role);

    Integer findUserByEmail(String email);

    String lease(Integer userId, String role);
}
