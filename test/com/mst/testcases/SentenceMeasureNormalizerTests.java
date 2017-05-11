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

		List<WordToken> words = getWordTokens("The right kidney measures 90 x 32 x 62 mm, volume 93 mL.");
		
		System.out.println(words);
		normalizer.Normalize(words, true, false);
		System.out.println(words);
		
		assertEquals(words.get(4).getToken(), "9.0x3.2x6.2");
		assertEquals(words.get(5).getToken(), "cm");
	}

	@Test
	public void testMMtoCM2() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();

		List<WordToken> words = getWordTokens("The left kidney measures 9.1 x 3.2 cm.");
		
		System.out.println(words);
		normalizer.Normalize(words, true, false);
		System.out.println(words);
		
		assertEquals(words.get(4).getToken(), "9.1x3.2");
		assertEquals(words.get(5).getToken(), "cm");
	}
	
	@Test
	public void testMMtoCM3() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();

		List<WordToken> words = getWordTokens("The laceration is 21 mm.");
		
		System.out.println(words);
		normalizer.Normalize(words, true, false);
		System.out.println(words);
		
		assertEquals(words.get(3).getToken(), "2.1");
		assertEquals(words.get(4).getToken(), "cm");
	}
	
	@Test
	public void testGetLargestValue() {
		SentenceMeasureNormalizerImpl normalizer = new SentenceMeasureNormalizerImpl();

		List<WordToken> words = getWordTokens("The right kidney measures 90 x 32 x 62 mm, volume 93 mL.");
		
		System.out.println(words);
		normalizer.Normalize(words, true, true);
		System.out.println(words);
		
		assertEquals(words.get(4).getToken(), "9.0");
		assertEquals(words.get(5).getToken(), "cm");
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

		