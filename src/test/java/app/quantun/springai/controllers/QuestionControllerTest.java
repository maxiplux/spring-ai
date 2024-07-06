package app.quantun.springai.controllers;

import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAiServices openAiServices;

    @Test
    void testGetResponse() throws Exception {
        mockMvc.perform(post("/question")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hello"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCapitalOrState() throws Exception {
        mockMvc.perform(post("/give-a-capital")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("France"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetInfoMovie() throws Exception {
        mockMvc.perform(post("/give-info-movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Tell me about Inception.\"}"))
                .andExpect(status().isOk());
    }
}
