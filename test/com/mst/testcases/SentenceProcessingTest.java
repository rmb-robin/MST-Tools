package com.mst.testcases;

import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class SentenceProcessingTest {
    private SentenceProcessingController controller;

    public SentenceProcessingTest() {
        controller = new SentenceProcessingControllerImpl();
        controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
    }

    @Test
    public void processRequest() {
        final String SOURCE = "SENTENCE_PROCESSING_TEST";
        final String SENTENCE1 = "cyst in left ovary measures 3cm";
        final String SENTENCE2 = "cyst in right ovary measures 4x5x3cm";
        List<SentenceTextRequest> requests = null; //TODO use BaseUtility
        assertNotNull(requests);
        assertEquals(2, requests.size());

        SentenceTextRequest request1 = requests.get(0);
        assertEquals(SOURCE, request1.getSource());
        SentenceProcessingResult result = null;
        try {
            result = controller.processText(request1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        List<Sentence> sentences = result.getSentences();
        assertNotNull(sentences);
        assertTrue(!sentences.isEmpty());
        assertEquals(1, sentences.size());
        assertEquals(SENTENCE1, sentences.get(0).getOrigSentence());

        SentenceTextRequest request2 = requests.get(1);
        assertEquals(SOURCE, request1.getSource());
        result = null;
        try {
            result = controller.processText(request2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        sentences = result.getSentences();
        assertNotNull(sentences);
        assertTrue(!sentences.isEmpty());
        assertEquals(1, sentences.size());
        assertEquals(SENTENCE2, sentences.get(0).getOrigSentence());
    }
}
