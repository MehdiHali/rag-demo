package com.rag.demo_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


/** LEARNING
 * This annotation tells spring boot this is a custom bean
 * that we want you to automatically instantiate and manage
  */
@Component
public class IngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger ( IngestionService.class );
    private final VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /** LEARNING
     *
     */
    //@Value("classpath:/docs/spring_boot_interview_questions.pdf")
    //@Value("classpath:/docs/java-interview-questions.pdf")
    @Value("classpath:/docs/article_thebeatoct2024.pdf")
    private Resource questionsPDF;

    @Override
    public void run(String... args) throws Exception {
        /** LEARNING
         * The below code does everything, from reading the document
         * to chuncking to embedding and loading to the vector store
         */

        var pdfReader = new ParagraphPdfDocumentReader( questionsPDF );
        TextSplitter textSplitter = new TokenTextSplitter();
        vectorStore.accept(textSplitter.apply(pdfReader.get()));
        log.info("VectorStore loaded with data");
    }
}
