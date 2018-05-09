package com.mst.testcases;

import org.junit.Test;

import com.mst.model.requests.SentenceRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.SentenceMeasureNormalizerImpl;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.SentenceProcessingHardcodedMetaDataInputFactory;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class SentenceMeasureNormalizerTests {
	
	@Test
	public void testMMtoCM() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
		List<WordToken> words = getWordTokens("measuring 90 x 32 x 62 mm");
		normalizer.Normalize(words, true, false);
		assertEquals(words.get(1).getToken(), "9.0");
        assertEquals(words.get(2).getToken(), "3.2");
        assertEquals(words.get(3).getToken(), "6.2");
		assertEquals(words.get(4).getToken(), "cm");
	}

	@Test
	public void testMMtoCM2() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measures 91 x 32 mm");
		normalizer.Normalize(words, true, false);
		assertEquals(words.get(1).getToken(), "9.1");
        assertEquals(words.get(2).getToken(), "3.2");
		assertEquals(words.get(3).getToken(), "cm");
	}
	
	@Test
	public void testMMtoCM3() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words = getWordTokens("measures 21mm");
		normalizer.Normalize(words, true, false);
		assertEquals(words.get(1).getToken(), "2.1");
		assertEquals(words.get(2).getToken(), "cm");
	}

	@Test
	public void testTokenizeMeasurements() {
        SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();
        List<WordToken> words;
        words = getWordTokens("measuring .7x.3x1.2cm");
		normalizer.Normalize(words, false, false);
        assertEquals(words.get(1).getToken(), ".7");
        assertEquals(words.get(2).getToken(), ".3");
        assertEquals(words.get(3).getToken(), "1.2");
        assertEquals(words.get(4).getToken(), "cm");
        words = getWordTokens("measuring .7 x .3 x 1.2 cm");
        normalizer.Normalize(words, false, false);
        assertEquals(words.get(1).getToken(), ".7");
        assertEquals(words.get(2).getToken(), ".3");
        assertEquals(words.get(3).getToken(), "1.2");
        assertEquals(words.get(4).getToken(), "cm");
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

		