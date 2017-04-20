package com.mst.interfaces;

import java.util.List;

import com.mst.model.sentenceProcessing.PrepositionPhraseProcessingInput;
import com.mst.model.sentenceProcessing.WordToken;


public interface PrepositionPhraseProcessor {

	List<WordToken> process(List<WordToken> tokens, PrepositionPhraseProcessingInput input);
}