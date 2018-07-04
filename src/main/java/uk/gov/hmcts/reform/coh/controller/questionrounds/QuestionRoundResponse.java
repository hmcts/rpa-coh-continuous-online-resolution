package uk.gov.hmcts.reform.coh.controller.questionrounds;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.coh.domain.QuestionRound;

import java.util.List;

public class QuestionRoundResponse {

    @JsonProperty(value = "previous_question_round")
    private Integer previousQuestionRound;

    @JsonProperty(value = "current_question_round")
    private Integer currentQuestionRound;

    @JsonProperty(value = "next_question_round")
    private Integer nextQuestionRound;

    @JsonProperty(value = "max_number_of_question_rounds")
    private Integer maxQuestionRound;

    @JsonProperty(value = "question_rounds")
    private List<QuestionRound> questionRounds;


    public void setQuestionRounds(List<QuestionRound> questionRounds) {
        this.questionRounds = questionRounds;
    }
}
