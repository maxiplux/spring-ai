package app.quantun.springai.services;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;

public interface OpenAiServices {
    Answer getWeatherInformation(Question question);
}
