package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;
import com.mst.model.gentwo.PrepositionPhraseProcessingInput;


public interface PrepositionPhraseProcessor {

	List<WordToken> process(List<WordToken> tokens, PrepositionPhraseProcessingInput input);
}