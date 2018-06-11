package uk.gov.hmcts.reform.coh.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.Question;

@RestController
@RequestMapping("/online-hearings")
public class QuestionController {

    @GetMapping("/{oh_id}/question-rounds/{qr_id}/questions/{q_id}")
    public String getQuestion(@PathVariable Integer oh_id, @PathVariable Integer qr_id, @PathVariable Integer q_id) {
        System.out.println("oh_id " + oh_id);
        System.out.println("qr_id " + qr_id);
        System.out.println("q_id " + q_id);
        return "GETTING QUESTION";
    }

    @PostMapping(value = "/{oh_id}/question-rounds/{qr_id}/questions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Question> getQuestion(@PathVariable Integer oh_id, @PathVariable Integer qr_id, @RequestBody Question body) {
        System.out.println("question " + body.getQuestionText());
        System.out.println("subject " + body.getSubject());

        return ResponseEntity.ok(body);
    }


    //POST QUESTION
    // /online-hearings/{oh_id}/question-rounds/{qr_id}/questions/
}
