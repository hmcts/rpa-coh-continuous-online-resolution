package uk.gov.hmcts.reform.coh.idam.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.fail;

public class RestTemplateIdamClient implements IdamClient {

    private final String idamUrl;
    private Integer idamUser = 0;

    public RestTemplateIdamClient(String idamUrl) {
        this.idamUrl = idamUrl;
    }

    private static void withValidHttpCodes(Consumer<RestTemplate> consumer, int... codes) {
        withValidHttpCodes(new RestTemplate(), consumer, codes);
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
    public void createAccount(String email, String role) {
        String password = UUID.randomUUID().toString();
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

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setOutputStreaming(false);
        withValidHttpCodes(
            new RestTemplate(simpleClientHttpRequestFactory),
            rt -> {
                ResponseEntity<String> responseEntity = rt
                    .postForEntity(idamUrl + "/testing-support/accounts", request, String.class);

                if (responseEntity.getStatusCodeValue() != 204) {
                    fail("Could not create account in IdAM");
                } else {
                    idamUser = findUserByEmail(email);
                }
            },
            401
        );
    }

    @Override
    public Integer findUserByEmail(String email) {
        String usersUri = UriComponentsBuilder.fromUriString(idamUrl)
            .path("/testing-support/accounts/" + email)
            .toUriString();

        withValidHttpCodes(
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
        ResponseEntity<String> response = new RestTemplate()
            .postForEntity(idamUrl + "/testing-support/lease", request, String.class);

        if (response.hasBody()) {
            return response.getBody();
        } else {
            return "";
        }
    }
}
