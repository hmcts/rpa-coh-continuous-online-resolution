package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.coh.controller.onlinehearing.OnlineHearingRequest;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestTrustManager;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingPanelMemberRepository;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps extends BaseSteps{

    private RestTemplate restTemplate;
    private String ENDPOINT = "/online-hearings";
    private OnlineHearing onlineHearing;
    private HttpHeaders header;
    private Question question;
    private List<Long> questionIds;
    private String onlineHearingExternalRef;


    @Autowired
    private OnlineHearingRepository onlineHearingRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OnlineHearingPanelMemberRepository onlineHearingPanelMemberRepository;

    @Before
    public void setup() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        restTemplate = new RestTemplate(TestTrustManager.getInstance().getTestRequestFactory());
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        questionIds = new ArrayList<>();

        OnlineHearingRequest preparedOnlineHearing = (OnlineHearingRequest)JsonUtils.toObjectFromTestName("online_hearing/standard_online_hearing", OnlineHearingRequest.class);
        onlineHearingExternalRef = preparedOnlineHearing.getCaseId();
    }

    @After
    public void cleanUp() {
        for (Long questionId : questionIds) {
            questionRepository.deleteById(questionId);
        }

        if (onlineHearingExternalRef != null) {
            try {
                onlineHearingPanelMemberRepository.deleteByOnlineHearing(onlineHearing);
                onlineHearingRepository.deleteByExternalRef(onlineHearingExternalRef);
            } catch(DataIntegrityViolationException e){
                System.out.println("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere." + e);
            }
        }
    }

    @And("^the draft a question for online_hearing$")
    public void theDraftAQuestion() throws Throwable {

        onlineHearing = onlineHearingRepository.findByExternalRef(onlineHearingExternalRef).get();

        String jsonBody = JsonUtils.getJsonInput("question/standard_question");
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);

        ResponseEntity<Question> response = restTemplate.exchange(baseUrl + ENDPOINT + "/" + onlineHearing.getOnlineHearingId() + "/questions", HttpMethod.POST, request, Question.class);
        question = response.getBody();
        questionIds.add(question.getQuestionId());
    }

    @Then("^the question state is ' \"([^\"]*)\" '$")
    public void theQuestionStateIs(String expectedState) throws Throwable {
       String state = question.getQuestionState().getState();
       assertEquals(expectedState, state);
    }

    @When("^a patch request is sent to ' \"([^\"]*)\" ' and response status is ' \"([^\"]*)\" '")
    public void aPatchRequestIsSentToOnlineHearingsOnlineHearingIdQuestionsQuestionId(String endpoint, String expectedStatus) throws Throwable {

        endpoint = endpoint.replaceAll("onlineHearing_id", String.valueOf(onlineHearing.getOnlineHearingId()));
        endpoint = endpoint.replaceAll("question_id", String.valueOf(question.getQuestionId()));

        System.out.println("Generated endpoint: " + endpoint);
        /**
         * This is a workaround for https://jira.spring.io/browse/SPR-15347
         *
         **/
        String jsonBody = JsonUtils.getJsonInput("question/issue_question");
        HttpEntity<String> request = new HttpEntity<>(jsonBody, header);
        int httpResponseCode = 0;
        try {
            ResponseEntity<Question> response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, Question.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                question = response.getBody();
            }
            httpResponseCode = response.getStatusCode().value();
        } catch (HttpServerErrorException hsee) {
            httpResponseCode = hsee.getRawStatusCode();
        }

        if(expectedStatus.contains("Successful")){
            assertEquals(200, httpResponseCode);
        }else if(expectedStatus.contains("Server error")){
            assertEquals(500, httpResponseCode);
        }
    }
}
