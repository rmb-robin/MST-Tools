package com.mst.sentenceprocessing;

import java.util.List;

import com.mst.interfaces.sentenceprocessing.SentenceMeasureNormalizer;
import com.mst.model.sentenceProcessing.WordToken;

public class SentenceMeasureNormalizerImpl implements SentenceMeasureNormalizer {

	public List<WordToken> Normalize(List<WordToken> wordTokens, boolean convertMeasurements, boolean convertLargest) {
		if(!convertMeasurements && !convertLargest) return wordTokens;
		
		//to do apply logic here..
		return wordTokens;
	}

}
