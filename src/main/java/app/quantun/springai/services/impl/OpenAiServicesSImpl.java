package app.quantun.springai.services.impl;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.dto.WeatherResponse;
import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
import app.quantun.springai.services.WeatherServiceFunction;
import app.quantun.springai.services.external.WeatherServiceFunctionImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiServicesSImpl implements OpenAiServices {

    private final ChatClient chatClient;


    @Value("${sfg.aiapp.apiNinjas.url}")
    public String apiNinjaWeatherUrl;

    @Value("${sfg.aiapp.apiNinjasKey}")
    private String apiNinjasKey;



    @Override
    public Answer getWeatherInformation(Question question) {
        var promptOptions = OpenAiChatOptions.builder()
                .withFunctionCallbacks(List.of(FunctionCallbackWrapper.builder(new WeatherServiceFunctionImpl(this.apiNinjaWeatherUrl,this.apiNinjasKey))
                        .withName("CurrentWeather")
                        .withDescription("Get the current weather for a location")
                        .withResponseConverter((response) -> {
                            String schema = ModelOptionsUtils.getJsonSchema(WeatherResponse.class, false);
                            String json = ModelOptionsUtils.toJsonString(response);
                            return schema + "\n" + json;
                        })
                        .build()))
                .build();


        Message userMessage = new PromptTemplate(question.question()).createMessage();

        Message systemMessage = new SystemPromptTemplate("You are a weather service. You receive weather information from a service which gives you the information based on the metrics system." +
                " When answering the weather in an imperial system country, you should convert the temperature to Fahrenheit and the wind speed to miles per hour. ").createMessage();



        var response = chatClient.call(new Prompt(List.of(userMessage,systemMessage), promptOptions));

        return new Answer(response.getResult().getOutput().getContent());
    }
}
