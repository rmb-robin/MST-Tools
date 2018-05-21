package com.mst.testcases;

import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.util.MongoDatastoreProviderDefault;
import org.junit.Test;
import test.SentenceTextRequestDao;

import java.util.List;

import static org.junit.Assert.*;

public class SentenceProcessingTest {
    private final String SOURCE = "SENTENCE_PROCESSING_TEST";
    private final String TEXT = "cyst in right ovary measures 4x5x3cm";
    private SentenceTextRequestDao dao;
    private SentenceProcessingController controller;

    public SentenceProcessingTest() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        dao = new SentenceTextRequestDao(SentenceTextRequest.class);
        dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault(SERVER, DATABASE));
        controller = new SentenceProcessingControllerImpl();
    }

    @Test
    public void processRequest() {
        SentenceTextRequest request = dao.getRequest(SOURCE);
        assertNotNull(request);
        assertEquals(SOURCE, request.getSource());
        SentenceProcessingResult result = null;
        try {
            result = controller.processText(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        List<Sentence> sentences = result.getSentences();
        assertNotNull(sentences);
        assertTrue(!sentences.isEmpty());
        assertEquals(TEXT, sentences.get(0).getOrigSentence());
    }
}
