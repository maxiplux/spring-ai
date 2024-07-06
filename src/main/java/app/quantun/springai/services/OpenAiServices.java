package app.quantun.springai.services;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;

public interface OpenAiServices {
    String getResponse(String message);

    Answer getSimpleAnswerFromRandomQuestionString(String message);

    Answer getCapital(String stateOrCountry);

    Answer getCapitalWithInfo(String stateOrCountry);

    Answer getAnswerFromDatabaseMovies(Question question);
}
