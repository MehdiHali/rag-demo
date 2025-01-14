package com.rag.demo_app;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final OllamaEmbeddingModel embeddingModel;

    @Value("classpath:/docs/java-interview-questions.pdf")
    private Resource questionsPDF;

    public ChatController(ChatClient.Builder chatClient, VectorStore vectorStore, OllamaEmbeddingModel embeddingModel) {
        this.chatClient = chatClient
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .build();
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/")
    public String chat() {
        String l_prompt = "What are the top 4 ideas?";
        System.out.println("CHAT CLIENT WILL ANSWER PROMPT: "+l_prompt);
        return chatClient.prompt().user(l_prompt)
                .call()
                .content();
    }

    @GetMapping("/test1")
    public float[] test1(){
        String text = "hello";
        float[] embeddings = embeddingModel.call(
            new EmbeddingRequest(List.of(text), OllamaOptions.builder()
              .withModel(OllamaModel.NOMIC_EMBED_TEXT) // why this is working and result doesn't change if i use all-minilm
              .build()))
          .getResult().getOutput();

        float[] embeddiings2 = embeddingModel.embed(text);

        return embeddings;
    }

    @GetMapping("/test2")
    public float[] test2(){
        String text = "hello";
        float[] embeddings = embeddingModel.embed(text);

        return embeddings;
    }

    @GetMapping("/split")
    public List<Document> readPDF(){
        TokenTextSplitter textSplitter = new TokenTextSplitter();

        DocumentReader pdfReader = new PagePdfDocumentReader( questionsPDF, PdfDocumentReaderConfig.builder().withPageTopMargin(10).build());
        List<Document> documents = pdfReader.read();
        System.out.println("NUMBER OF DOCUMENTS BEFORE AND AFTER SPLITTING: "+ documents.size() + " SPLITED -> "+ textSplitter.split(documents).size());
        return documents.subList(10,documents.size());
    }

    @GetMapping("/test3")
    public Map<String, String> test3(){
        Map<String,String> listOfEmbeddings = new HashMap<>();
        int counter = 0;
        List<Document> docs = readPDF();
        docs = docs.subList(0,3);
        docs.forEach(doc -> {
            System.out.println("... Calculating: "+doc.getMetadata());
            float[] l_embedding = embeddingModel.embed(doc.getContent());
            float[] first10Embeddings = new float[10];
            for(int i = 0 ; i < 10; i++){
                first10Embeddings[i] = l_embedding[i];
            }
            System.out.println("Embedding: "+ Arrays.toString(l_embedding));
            listOfEmbeddings.put(doc.getContent(), Arrays.toString(l_embedding));
        });

        return listOfEmbeddings;
    }

}
