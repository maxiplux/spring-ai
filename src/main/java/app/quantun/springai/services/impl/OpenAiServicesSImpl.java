package app.quantun.springai.services.impl;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.ai.chat.messages.Media;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.openai.audio.transcription.AudioTranscription;

import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;





import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiServicesSImpl implements OpenAiServices {


    public static final int TOP_FIVE_RECORDS_IN_VECTOR_DB = 5;
    @Value("classpath:/templates/rag-prompt-template.st")
    private Resource ressourceRagPromptTemplate;

    @Value("classpath:templates/get-capital-prompt.st")
    private Resource resourceCapitalPromptTemplate;


    @Value("classpath:templates/get-capital-with-info.st")
    private Resource resourceCapitalWithInfoPromptTemplate;


    private final OpenAiAudioSpeechModel speechClient;
    private final OpenAiAudioTranscriptionModel openAiTranscriptionClient;


    private  final ChatClient chatClient;

    private final ObjectMapper objectMapper;

    private final SimpleVectorStore vectorStore;

    private final OpenAiImageModel openaiImageModel;

    @Override
    public String getResponse(String message) {
        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create();

        return chatClient.prompt(prompt).call().content();
    }

    @Override
    public Answer getSimpleAnswerFromRandomQuestionString(String message) {
        PromptTemplate promptTemplate = new PromptTemplate(message);
        Prompt prompt = promptTemplate.create();
        return new Answer(chatClient.prompt(prompt).call().content()) ;

    }


    @Override
    public Answer getCapital(String stateOrCountry) {
        PromptTemplate promptTemplate = new PromptTemplate(resourceCapitalPromptTemplate);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", stateOrCountry));
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

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
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

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

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

        return new Answer(response.getResult().getOutput().getContent());
    }

    @Override
    public String getImageDescrition(MultipartFile file) throws IOException {
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .withModel(OpenAiApi.ChatModel.GPT_4_O.getValue())
                .build();

        var userMessage = new UserMessage(
                "Explain what do you see in this picture?", // content
                List.of(new Media(MimeTypeUtils.IMAGE_JPEG, file.getBytes()))); // media

        return chatClient.prompt(new Prompt(List.of(userMessage), chatOptions)).call().content();
    }

    @Override
    public byte[] getImageFromQuestion(Question question) {
        var options = OpenAiImageOptions.builder()
                .withHeight(1024).withWidth(1792)
                .withResponseFormat("b64_json")
                .withModel("dall-e-3")
                .withQuality("hd") //default standard
                //.withStyle("natural") //default vivid
                .build();

        ImagePrompt imagePrompt = new ImagePrompt(question.question(), options);


        ImageResponse imageResponse = openaiImageModel.call(imagePrompt);




        return Base64.getDecoder().decode(imageResponse.getResult().getOutput().getB64Json());
    }

    @Override
    public String getTranscript(MultipartFile file) {
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.JSON)
                .withLanguage("en")
                .withTemperature(0f)
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource(), transcriptionOptions);

        AudioTranscriptionResponse response =  this.openAiTranscriptionClient.call(prompt);


        return response.getResult().getOutput();
    }

    @Override
    public byte[] getSpeech(Question question) {
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
                .withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                .withSpeed(1.0f)
                .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .withModel(OpenAiAudioApi.TtsModel.TTS_1.value)
                .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(question.question(),
                speechOptions);

        SpeechResponse response = speechClient.call(speechPrompt);

        return response.getResult().getOutput();
    }


}
