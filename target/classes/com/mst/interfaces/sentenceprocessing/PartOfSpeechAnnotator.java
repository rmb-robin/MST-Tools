package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.PartOfSpeechAnnotatorEntity;
import com.mst.model.sentenceProcessing.WordToken;

public interface PartOfSpeechAnnotator {

	List<WordToken> annotate(List<WordToken> tokens,PartOfSpeechAnnotatorEntity entity);
}
