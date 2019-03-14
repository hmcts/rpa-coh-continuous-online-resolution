package uk.gov.hmcts.reform.coh.idam;

public interface IdamClient {

    void createAccount(String email, String role, String password);

    Integer findUserByEmail(String email);

    String lease(Integer userId, String role);

    String authenticate(String user, String password, String responseType, String clientId, String redirectUri);

    String exchangeCode(String code, String grantType, String clientId, String clientSecret, String redirectUri);
}
