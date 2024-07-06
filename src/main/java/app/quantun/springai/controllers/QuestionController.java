package app.quantun.springai.controllers;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final OpenAiServices openAiServices;

    @PostMapping("/question")
    public Answer getResponse(String message) {
        return openAiServices.getSimpleAnswerFromRandomQuestionString(message);
    }


    @PostMapping("/give-a-capital")
    public Answer getCapitalOrState(String countryOrState) {
        return openAiServices.getCapitalWithInfo(countryOrState);
    }

    @PostMapping("/give-info-movie")
    public Answer getInfoMovie(@RequestBody Question question) {
        return openAiServices.getAnswerFromDatabaseMovies(question);
    }

}
