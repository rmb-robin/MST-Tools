package com.mst.testcases;

import org.junit.Test;

import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;

import static com.mst.model.metadataTypes.MeasurementAnnotations.*;
import static org.junit.Assert.*;
import static com.mst.model.metadataTypes.Descriptor.*;

import java.util.ArrayList;
import java.util.List;

public class MeasurementProcessorTest {
    @Test
    public void testMMtoCM() {
        List<WordToken> words = getWordTokens("measuring 11 x 32 x 23 mm");
        assertEquals("1.1", words.get(1).getToken());
        assertEquals("3.2", words.get(2).getToken());
        assertEquals("2.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());
    }

    @Test
    public void testMMtoCM2() {
        List<WordToken> words = getWordTokens("measuring 21x32x63mm");
        assertEquals("2.1", words.get(1).getToken());
        assertEquals("3.2", words.get(2).getToken());
        assertEquals("6.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());
    }

	@Test
	public void testMMtoCM3() {
        List<WordToken> words = getWordTokens("measures 31 x 32 mm");
		assertEquals("3.1", words.get(1).getToken());
        assertEquals("3.2", words.get(2).getToken());
		assertEquals("cm", words.get(3).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
	}
	
	@Test
	public void testMMtoCM4() {
        List<WordToken> words = getWordTokens("measures 41mm");
		assertEquals("4.1", words.get(1).getToken());
		assertEquals("cm", words.get(2).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
	}

	@Test
	public void testTokenizeMeasurements() {
        List<WordToken> words = getWordTokens("measuring 5.1x.2x8.3cm");
        assertEquals("5.1", words.get(1).getToken());
        assertEquals(".2", words.get(2).getToken());
        assertEquals("8.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());

        words = getWordTokens("measuring 6.1 x 7.2 x 1.3 cm");
        assertEquals("6.1", words.get(1).getToken());
        assertEquals("7.2", words.get(2).getToken());
        assertEquals("1.3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());

        words = getWordTokens("measuring 7.1 x 2 x 3cm");
        assertEquals("7.1", words.get(1).getToken());
        assertEquals("2", words.get(2).getToken());
        assertEquals("3", words.get(3).getToken());
        assertEquals("cm", words.get(4).getToken());
        assertEquals(X_AXIS, words.get(1).getDescriptor());
        assertEquals(Y_AXIS, words.get(2).getDescriptor());
        assertEquals(Z_AXIS, words.get(3).getDescriptor());
	}

    @Test
    public void testMeasurementAnnotations() {
        List<WordToken> words = getWordTokens("measuring 1.1 cm in length x 3.2 cm width x 2.3 cm depth");
        assertEquals("1.1", words.get(1).getToken());
        assertEquals("3.2", words.get(6).getToken());
        assertEquals("2.3", words.get(10).getToken());
        assertEquals(LENGTH, words.get(1).getDescriptor());
        assertEquals(TRANSVERSE, words.get(6).getDescriptor());
        assertEquals(AP, words.get(10).getDescriptor());

        words = getWordTokens("Short axis measures 1.1 cm and long axis measures 3.2 cm");
        assertEquals("1.1", words.get(3).getToken());
        assertEquals("3.2", words.get(9).getToken());
        assertEquals(SHORT_AXIS, words.get(3).getDescriptor());
        assertEquals(LONG_AXIS, words.get(9).getDescriptor());

        words = getWordTokens("measuring 5.2x5 by approximately 11 cm in transverse, ap and length dimensions respectively");
        assertEquals("5.2", words.get(1).getToken());
        assertEquals("5", words.get(2).getToken());
        assertEquals("11", words.get(5).getToken());
        assertEquals(TRANSVERSE, words.get(1).getDescriptor());
        assertEquals(AP, words.get(2).getDescriptor());
        assertEquals(LENGTH, words.get(5).getDescriptor());
    }

    private List<WordToken> getWordTokens(String text) {
        List<WordToken> words = new ArrayList<>();
		try {
			SentenceProcessingControllerImpl controller = new  SentenceProcessingControllerImpl();
			controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
			SentenceRequest request = new SentenceRequest();
			request.setConvertMeasurements(true);
			List<String> input = new ArrayList<>();
			input.add(text);
			request.setSentenceTexts(input);
			List<Sentence> sentences = controller.processSentences(request);
            Sentence result = sentences.get(0);
            words = result.getModifiedWordList();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return words;
	}
}

		