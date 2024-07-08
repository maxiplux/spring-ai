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
    @DisplayName("Get response from openAI API")
    void getResponse() {
        String response = openAiServicesS.getResponse("Hello");
        assertTrue(response.contains("?"));

    }

    @Test
    @DisplayName("Get answer a New data structure")
    void getSimpleAnswerFromRandomQuestionString() {
        Answer answer = openAiServicesS.getSimpleAnswerFromRandomQuestionString("1+1");
        assertTrue(answer.answer().contains("2"));
    }

    @Test
    @DisplayName("Get capital simple")
    void getCapital() {
        Answer answer = openAiServicesS.getCapital("Nigeria");
        assertTrue(answer.answer().contains("Abuja"));
    }

    @Test
    @DisplayName("Get capital with info")
    void getCapitalWithInfo() {

        Answer answer = openAiServicesS.getCapitalWithInfo("Nigeria");
        assertTrue(answer.answer().contains("Abuja"));
    }

    @Test
    @DisplayName("Get answer from a question using RAG")
    void testGetAnswerFromMovieDatabase() {
        Answer answer = openAiServicesS.getAnswerFromDatabaseMovies(new Question("Avengers"));
        assertTrue(answer.answer().contains("Avengers"));
    }

    @Test
    @DisplayName("Get advice to buy a truck")
    void getAdviceToBuyATruck() {

        Answer answer = openAiServicesS.getAdviceToBuyATruck(new Question("Sportsman 212 boat"));
        assertTrue(answer.answer().contains("3,458"));
    }
}
