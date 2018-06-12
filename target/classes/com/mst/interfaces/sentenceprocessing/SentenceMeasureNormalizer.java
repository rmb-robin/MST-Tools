package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.WordToken;

public interface SentenceMeasureNormalizer {
	List<WordToken> Normalize(List<WordToken> wordTokens, boolean convertMeasurements);
}
