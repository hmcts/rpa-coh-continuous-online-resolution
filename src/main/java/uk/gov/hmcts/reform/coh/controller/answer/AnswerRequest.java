package uk.gov.hmcts.reform.coh.controller.answer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AnswerRequest {

    @JsonProperty("question_id")
    private String questionId;

    @JsonProperty("question_part_id")
    private String questionPartId;

    private Answer answer;

    public AnswerState getAnswerState() {
        return answerState;
    }

    public void setAnswerState(AnswerState answerState) {
        this.answerState = answerState;
    }

    @JsonProperty("answer_state")
    private AnswerState answerState;

    public String getQuestionPartId() {
        return questionPartId;
    }

    public void setQuestionPartId(String questionPartId) {
        this.questionPartId = questionPartId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public class Answer {

        @JsonProperty("type_of_answer")
        private String typeOfAnswer;

        @JsonProperty("answer")
        private String answer;

        public String getTypeOfAnswer() {
            return typeOfAnswer;
        }

        public void setTypeOfAnswer(String typeOfAnswer) {
            this.typeOfAnswer = typeOfAnswer;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    public class AnswerState {

        @JsonProperty("state_name")
        private String stateName;

        @JsonProperty("state_datetime")
        private String stateStartTime;

        public String getStateName() {
            return stateName;
        }

        public void setStateName(String stateName) {
            this.stateName = stateName;
        }

        public String getStateStartTime() {
            return stateStartTime;
        }

        public void setStateStartTime(String stateStartTime) {
            this.stateStartTime = stateStartTime;
        }
    }
}
