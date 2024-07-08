package app.quantun.springai.services.impl;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
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
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiServicesSImpl implements OpenAiServices {


    public static final int TOP_FIVE_RECORDS_IN_VECTOR_DB = 5;
    @Value("classpath:/templates/rag-prompt-template.st")
    private Resource ressourceRagPromptTemplate;


    @Value("classpath:/templates/system-message.st")
    private Resource resourceSystemMessageTemplate;

    @Value("classpath:/templates/rag-prompt-without-metadata-template.st")
    private Resource resourceRagPromptWithoutMedataTemplate;

    @Value("classpath:templates/get-capital-prompt.st")
    private Resource resourceCapitalPromptTemplate;


    @Value("classpath:templates/get-capital-with-info.st")
    private Resource resourceCapitalWithInfoPromptTemplate;

    private  final ChatClient chatClient;

    private final ObjectMapper objectMapper;

    final VectorStore vectorStore;

    @Override
    public String getResponse(String message) {
        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create();

        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public Answer getSimpleAnswerFromRandomQuestionString(String message) {
        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create();
        return new Answer(chatClient.call(prompt).getResult().getOutput().getContent());

    }


    @Override
    public Answer getCapital(String stateOrCountry) {
        PromptTemplate promptTemplate = new PromptTemplate(resourceCapitalPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", stateOrCountry));
        ChatResponse response = chatClient.call(prompt);

        String responseString;

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response.getResult().getOutput().getContent());
        } catch (JsonProcessingException e) {
            log.error("Error parsing response for {}: {}", stateOrCountry, e.getMessage(), e);


        }

        responseString = jsonNode.get("answer").asText();

        return new Answer(responseString);
    }


    @Override
    public Answer getCapitalWithInfo(String stateOrCountry) {
        PromptTemplate promptTemplate = new PromptTemplate(this.resourceCapitalWithInfoPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", stateOrCountry));
        ChatResponse response = chatClient.call(prompt);

        return new Answer(response.getResult().getOutput().getContent());
    }


    @Override
    public Answer getAnswerFromDatabaseMovies(Question question) {
        // We need to search for the question in the vector store to get the most similar or related data
        List<Document> documents = vectorStore.similaritySearch(SearchRequest
                .query(question.question()).withTopK(TOP_FIVE_RECORDS_IN_VECTOR_DB));
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        // using it, we can send this information to our prompt.
        PromptTemplate promptTemplate = new PromptTemplate(this.ressourceRagPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of("input", question.question(), "documents",
                String.join("\n", contentList)));


        // thankks to this promt, we can get the answer from the chat client, but using our dataset as a source of information
        // Chat gpt is not allucinating, it is using the data we provided to give us the answer

        ChatResponse response = chatClient.call(prompt);

        return new Answer(response.getResult().getOutput().getContent());
    }


    @Override
    public Answer getAdviceToBuyATruck(Question question) {
        PromptTemplate systemMessagePromptTemplate = new SystemPromptTemplate(resourceSystemMessageTemplate);
        Message systemMessage = systemMessagePromptTemplate.createMessage();

        List<Document> documents = vectorStore.similaritySearch(SearchRequest
                .query(question.question()).withTopK(TOP_FIVE_RECORDS_IN_VECTOR_DB));
        List<String> contentList = documents.stream().map(Document::getContent).toList();

        PromptTemplate promptTemplate = new PromptTemplate(resourceRagPromptWithoutMedataTemplate);
        Message userMessage = promptTemplate.createMessage(Map.of("input", question.question(), "documents",
                String.join("\n", contentList)));



        ChatResponse response = chatClient.call(new Prompt(List.of(systemMessage, userMessage)));

        return new Answer(response.getResult().getOutput().getContent());
    }

}
