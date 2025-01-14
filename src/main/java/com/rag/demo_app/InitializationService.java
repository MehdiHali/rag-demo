package com.rag.demo_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;


/** LEARNING
 * This annotation tells spring boot this is a custom bean
 * that we want you to automatically instantiate and manage
  */
@Component
public class InitializationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger ( InitializationService.class );
    private final VectorStore vectorStore;

    public InitializationService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /** LEARNING
     *
     */
    @Value("classpath:/docs/java-interview-questions.pdf")
    private Resource questionsPDF;

    @Override
    public void run(String... args) throws Exception {
        TokenTextSplitter textSplitter = new TokenTextSplitter();

        // EXTRACT (READ)
        DocumentReader pdfReader = new PagePdfDocumentReader( questionsPDF);
        List<Document> documents = pdfReader.read();
        // TRANSFORM (SPLIT)
        List<Document>  chunks = textSplitter.split(pdfReader.get());
        // LOAD (WRITE)
        vectorStore.write(chunks);
        log.info("VectorStore loaded with data");
    }
}
