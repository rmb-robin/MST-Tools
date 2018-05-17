package com.mst.testcases;

import org.junit.Test;

import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.SentenceMeasureNormalizerImpl;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;

import static org.junit.Assert.*;
import static com.mst.model.metadataTypes.Descriptor.*;

import java.util.ArrayList;
import java.util.List;

public class SentenceMeasureNormalizerTests {

    @Test
    public void testMMtoCM() {
        SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measuring 11 x 32 x 23 mm");
        normalizer.Normalize(words, true);
        assertEquals(words.get(1).getToken(), "1.1");
        assertEquals(words.get(2).getToken(), "3.2");
        assertEquals(words.get(3).getToken(), "2.3");
        assertEquals(words.get(4).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(3).getPosition(), 4);
        assertEquals(words.get(4).getPosition(), 5);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
        assertEquals(words.get(2).getDescriptor(), Y_AXIS);
        assertEquals(words.get(3).getDescriptor(), Z_AXIS);
    }

    @Test
    public void testMMtoCM2() {
        SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measuring 21x32x63mm");
        normalizer.Normalize(words, true);
        assertEquals(words.get(1).getToken(), "2.1");
        assertEquals(words.get(2).getToken(), "3.2");
        assertEquals(words.get(3).getToken(), "6.3");
        assertEquals(words.get(4).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(3).getPosition(), 4);
        assertEquals(words.get(4).getPosition(), 5);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
        assertEquals(words.get(2).getDescriptor(), Y_AXIS);
        assertEquals(words.get(3).getDescriptor(), Z_AXIS);
    }

	@Test
	public void testMMtoCM3() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measures 31 x 32 mm");
		normalizer.Normalize(words, true);
		assertEquals(words.get(1).getToken(), "3.1");
        assertEquals(words.get(2).getToken(), "3.2");
		assertEquals(words.get(3).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(3).getPosition(), 4);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
        assertEquals(words.get(2).getDescriptor(), Y_AXIS);
	}
	
	@Test
	public void testMMtoCM4() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measures 41mm");
		normalizer.Normalize(words, true);
		assertEquals(words.get(1).getToken(), "4.1");
		assertEquals(words.get(2).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
	}

	@Test
	public void testTokenizeMeasurements() {
        SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measuring 5.1x.2x8.3cm");
		normalizer.Normalize(words, false);
        assertEquals(words.get(1).getToken(), "5.1");
        assertEquals(words.get(2).getToken(), ".2");
        assertEquals(words.get(3).getToken(), "8.3");
        assertEquals(words.get(4).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(3).getPosition(), 4);
        assertEquals(words.get(4).getPosition(), 5);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
        assertEquals(words.get(2).getDescriptor(), Y_AXIS);
        assertEquals(words.get(3).getDescriptor(), Z_AXIS);

        words = getWordTokens("measuring 6.1 x 7.2 x 1.3 cm");
        normalizer.Normalize(words, false);
        assertEquals(words.get(1).getToken(), "6.1");
        assertEquals(words.get(2).getToken(), "7.2");
        assertEquals(words.get(3).getToken(), "1.3");
        assertEquals(words.get(4).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(3).getPosition(), 4);
        assertEquals(words.get(4).getPosition(), 5);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
        assertEquals(words.get(2).getDescriptor(), Y_AXIS);
        assertEquals(words.get(3).getDescriptor(), Z_AXIS);

        words = getWordTokens("measuring 7.1 x 2 x 3cm");
        normalizer.Normalize(words, false);
        assertEquals(words.get(1).getToken(), "7.1");
        assertEquals(words.get(2).getToken(), "2");
        assertEquals(words.get(3).getToken(), "3");
        assertEquals(words.get(4).getToken(), "cm");
        assertEquals(words.get(1).getPosition(), 2);
        assertEquals(words.get(2).getPosition(), 3);
        assertEquals(words.get(3).getPosition(), 4);
        assertEquals(words.get(4).getPosition(), 5);
        assertEquals(words.get(1).getDescriptor(), X_AXIS);
        assertEquals(words.get(2).getDescriptor(), Y_AXIS);
        assertEquals(words.get(3).getDescriptor(), Z_AXIS);
	}

    private List<WordToken> getWordTokens(String text) {
        List<WordToken> words = new ArrayList<>();
		try {
			SentenceProcessingControllerImpl controller = new  SentenceProcessingControllerImpl();
			controller.setMetadata(new SentenceProcessingHardcodedMetaDataInputFactory().create());
			SentenceRequest request = new SentenceRequest();
			List<String> input = new ArrayList<>();
			input.add(text);
			request.setSenteceTexts(input);
			List<Sentence> sentences = controller.processSentences(request);
            Sentence result = sentences.get(0);
            words = result.getModifiedWordList();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return words;
	}
}

		