package app.quantun.springai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Configuration
@Slf4j
public class VectorStoreConfig {
    @Bean
    SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingClient, VectorStoreProperties vectorStoreProperties) {
        // Initialize the SimpleVectorStore with the provided EmbeddingModel
        var store =  new SimpleVectorStore(embeddingClient);
        // Create a File object pointing to the path where the vector store is or will be saved

        File vectorStoreFile = new File(vectorStoreProperties.getVectorStorePath());
        // If the file exists, load the vector store from the file
        if (vectorStoreFile.exists()) {
            log.debug("Loading vector store from file: " + vectorStoreFile.getAbsolutePath());
            store.load(vectorStoreFile);
            return store;
        }
            log.debug("Loading documents into vector store");
            vectorStoreProperties.getDocumentsToLoad().forEach(document -> {
                log.debug("Loading document: " + document.getFilename());
                TikaDocumentReader documentReader = new TikaDocumentReader(document);
                List<Document> docs = documentReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocs = textSplitter.apply(docs);
                store.add(splitDocs);
            });
            store.save(vectorStoreFile);


        return store;
    }
}
