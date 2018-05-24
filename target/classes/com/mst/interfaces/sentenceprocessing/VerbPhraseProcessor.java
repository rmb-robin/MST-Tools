package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.VerbPhraseInput;
import com.mst.model.sentenceProcessing.WordToken;

public interface VerbPhraseProcessor {
	List<WordToken> process(List<WordToken> tokens, VerbPhraseInput input);
}
