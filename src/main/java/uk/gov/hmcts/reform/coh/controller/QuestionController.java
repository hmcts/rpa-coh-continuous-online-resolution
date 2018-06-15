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


    @GetMapping("/{oh_id}/questions/{questionId}")
    public ResponseEntity<Question> getQuestion(@PathVariable Integer oh_id, @PathVariable Integer questionId) {
        return ResponseEntity.ok(questionService.retrieveQuestionById(questionId));
    }

    @PostMapping(value = "/{oh_id}/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> createQuestion(@PathVariable Integer oh_id, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.createQuestion(oh_id, body));
    }

    @PatchMapping(value = "/{oh_id}/questions/{questionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> editQuestion(@PathVariable Integer questionId, @RequestBody Question body) {
        return ResponseEntity.ok(questionService.editQuestion(questionId, body));
    }

}
