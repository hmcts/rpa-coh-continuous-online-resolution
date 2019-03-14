package uk.gov.hmcts.reform.coh.idam.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.coh.idam.IdamClient;
import uk.gov.hmcts.reform.coh.utils.JsonUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

public class RestTemplateIdamClient implements IdamClient {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateIdamClient.class);
    private final String idamUrl;
    private Integer idamUser = 0;
    private String response;

    public RestTemplateIdamClient(String idamUrl) {
        this.idamUrl = idamUrl;
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setOutputStreaming(false);
        return new RestTemplate(simpleClientHttpRequestFactory);
    }

    private static void withValidHttpCodes(RestTemplate rt, Consumer<RestTemplate> consumer, int... codes) {
        ResponseErrorHandler oldErrorHandler = rt.getErrorHandler();
        rt.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                for (int code : codes) {
                    if (response.getRawStatusCode() == code) {
                        return false;
                    }
                }
                return oldErrorHandler.hasError(response);
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                oldErrorHandler.handleError(response);
            }
        });
        consumer.accept(rt);
    }

    @Override
    public void createAccount(String email, String role, String password) {
        ImmutableMap<String, Object> body = ImmutableMap.of(
            "email", email,
            "password", password,
            "forename", "test",
            "surname", "test",
            "roles", ImmutableList.of(
                ImmutableMap.of(
                    "code", role
                )
            )
        );

        String json;
        try {
            json = JsonUtils.toJson(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(json, headers);

        RestTemplate requestTemplate = createRestTemplate();
        withValidHttpCodes(
            requestTemplate,
            rt -> {
                ResponseEntity<String> responseEntity = rt
                    .postForEntity(idamUrl + "/testing-support/accounts", request, String.class);

                if (responseEntity.getStatusCodeValue() == 204) {
                    idamUser = findUserByEmail(email);
                }
            },
            400, 401
        );
    }

    @Override
    public Integer findUserByEmail(String email) {
        String usersUri = UriComponentsBuilder.fromUriString(idamUrl)
            .path("/testing-support/accounts/" + email)
            .toUriString();

        withValidHttpCodes(
            createRestTemplate(),
            rt -> {
                ResponseEntity<Map> response = rt.getForEntity(usersUri, Map.class);
                if (response.getStatusCode() == HttpStatus.OK
                    && response.hasBody()
                    && response.getBody().containsKey("id")) {

                    idamUser = (Integer) response.getBody().get("id");
                }
            },
            404
        );
        return idamUser;
    }

    @Override
    public String lease(Integer userId, String role) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set("id", idamUser.toString());
        body.set("role", role);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = createRestTemplate()
            .postForEntity(idamUrl + "/testing-support/lease", request, String.class);

        if (response.hasBody()) {
            return response.getBody();
        } else {
            return "";
        }
    }

    @Override
    public String authenticate(String user, String password, String responseType, String clientId, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String s = user + ":" + password;
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + new String(Base64.getEncoder().encode(s.getBytes())));

        UriComponentsBuilder url = UriComponentsBuilder.fromHttpUrl(idamUrl)
            .path("/oauth2/authorize")
            .queryParam("response_type", responseType)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri);

        return postForString(url, new HttpEntity<>(headers), "code");
    }

    private String postForString(UriComponentsBuilder url, HttpEntity<String> request, String fieldName) {
        withValidHttpCodes(
            createRestTemplate(),
            rt -> {
                ResponseEntity<Map> response = rt.postForEntity(url.build().toUriString(), request, Map.class);

                if (response.hasBody()) {
                    this.response = (String) response.getBody().get(fieldName);
                } else {
                    log.warn("Empty response body");
                }
            },
            401
        );
        return this.response;
    }

    @Override
    public String exchangeCode(
        String code,
        String grantType,
        String clientId,
        String clientSecret,
        String redirectUri
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        UriComponentsBuilder url = UriComponentsBuilder.fromHttpUrl(idamUrl)
            .path("/oauth2/token")
            .queryParam("code", code)
            .queryParam("grant_type", grantType)
            .queryParam("client_id", clientId)
            .queryParam("client_secret", clientSecret)
            .queryParam("redirect_uri", redirectUri);

        return postForString(url, new HttpEntity<>(headers), "access_token");
    }
}
