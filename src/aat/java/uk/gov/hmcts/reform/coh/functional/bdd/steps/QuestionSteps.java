package uk.gov.hmcts.reform.coh.functional.bdd.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.repository.OnlineHearingRepository;
import uk.gov.hmcts.reform.coh.repository.QuestionRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@ContextConfiguration
@SpringBootTest
public class QuestionSteps extends BaseSteps{

    private TestRestTemplate restTemplate = new TestRestTemplate();
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

    @Before
    public void setup() throws IOException {
        header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        questionIds = new ArrayList<>();

        OnlineHearing preparedOnlineHearing = (OnlineHearing)JsonUtils.toObjectFromTestName("create_online_hearing", OnlineHearing.class);
        onlineHearingExternalRef = preparedOnlineHearing.getExternalRef();
    }

    @After
    public void cleanUp() {
        for (Long questionId : questionIds) {
            questionRepository.deleteById(questionId);
        }

        if(onlineHearingExternalRef!=null) {
            try {
                onlineHearingRepository.deleteByExternalRef(onlineHearingExternalRef);
            }catch(DataIntegrityViolationException e){
                System.out.println("Failure may be due to foreign key. This is okay because the online hearing will be deleted elsewhere.");
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
        ResponseEntity<Question> response = restTemplate.exchange(baseUrl + endpoint + "?_method=patch", HttpMethod.POST, request, Question.class);
        if(response.getStatusCode().is2xxSuccessful()){
            question = response.getBody();
        }

        if(expectedStatus.contains("Successful")){
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }else if(expectedStatus.contains("Server error")){
            assertTrue(response.getStatusCode().is5xxServerError());
        }
    }
}
