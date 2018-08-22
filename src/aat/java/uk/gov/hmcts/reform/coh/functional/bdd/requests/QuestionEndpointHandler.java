package uk.gov.hmcts.reform.coh.functional.bdd.requests;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.coh.controller.utils.CohUriBuilder;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.functional.bdd.utils.TestContext;

@Component
public class QuestionEndpointHandler extends AbstractRequestEndpoint {

    @Override
    public String getUrl(HttpMethod method, TestContext testContext) {
        OnlineHearing onlineHearing = testContext.getScenarioContext().getCurrentOnlineHearing();
        Question question = testContext.getScenarioContext().getCurrentQuestion();

        String url = null;
        if (HttpMethod.POST.equals(method)) {
            url = CohUriBuilder.buildQuestionPost(onlineHearing.getOnlineHearingId());
        } else if (HttpMethod.GET.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.DELETE.equals(method)) {
            url = CohUriBuilder.buildQuestionGet(onlineHearing.getOnlineHearingId(), question.getQuestionId());
        } else {
            throw new NotImplementedException(String.format("Http method %s not implemented for %s", method, getClass()));
        }

        return baseUrl + url;
    }

    @Override
    public String supports() {
        return CohEntityTypes.QUESTION.toString();
    }
}
