package uk.gov.hmcts.reform.coh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.Question;
import uk.gov.hmcts.reform.coh.service.QuestionService;

@RestController
@RequestMapping("/online-hearings")
public class QuestionController {

    private QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/{oh_id}/question-rounds/{qr_id}/questions/{question_id}")
    public ResponseEntity<Question> getQuestion(@PathVariable Integer oh_id, @PathVariable Integer qr_id, @PathVariable Integer question_id) {
        return ResponseEntity.ok(questionService.retrieveQuestionById(question_id));
    }

    @PostMapping(value = "/{oh_id}/question-rounds/{qr_id}/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> createQuestion(@PathVariable Integer oh_id, @PathVariable Integer qr_id, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.createQuestion(oh_id, qr_id, body));
    }
}
