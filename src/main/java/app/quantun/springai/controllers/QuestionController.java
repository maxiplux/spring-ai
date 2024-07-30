package app.quantun.springai.controllers;

import app.quantun.springai.dto.Answer;
import app.quantun.springai.models.Question;
import app.quantun.springai.services.OpenAiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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

    @PostMapping(value = "/vision", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> upload(
            @Validated @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name
    ) throws IOException {

        return ResponseEntity.ok(openAiServices.getImageDescrition(file));
    }

    @PostMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImage(@RequestBody Question question) {
        return openAiServices.getImageFromQuestion(question);
    }








    @PostMapping(value = "/transcript-audio", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, String>> transcriptAudio(
            @Validated @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name) {

        Map<String, String> response = Map.of("response", this.openAiServices.getTranscript(file));


        return ResponseEntity.ok(response);
    }

    @PostMapping(value ="/talk", produces = "audio/mpeg")
    public byte[] talkTalk(@RequestBody Question question) {
        return this.openAiServices.getSpeech(question);
    }




}
