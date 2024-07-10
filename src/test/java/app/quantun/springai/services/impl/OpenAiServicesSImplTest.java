package app.quantun.springai.services.impl;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class OpenAiServicesSImplTest {

    @Autowired
    OpenAiServices openAiServicesS;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }



    @Test
    @DisplayName("Test getWeatherInformation")
    void getWeatherInformation() {
        Question question = new Question("What is the weather in Tampa?");

        Answer answer = openAiServicesS.getWeatherInformation(question);
        assertTrue(answer.answer().contains("Humidity"));
    }
}
