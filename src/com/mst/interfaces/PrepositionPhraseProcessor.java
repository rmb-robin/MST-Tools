package com.mst.interfaces;

import java.util.List;

import com.mst.model.gentwo.PrepositionPhraseProcessingInput;
import com.mst.model.gentwo.WordToken;


public interface PrepositionPhraseProcessor {

	List<WordToken> process(List<WordToken> tokens, PrepositionPhraseProcessingInput input);
}