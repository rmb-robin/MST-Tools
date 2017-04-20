package com.mst.interfaces;

import java.util.List;

import com.mst.model.gentwo.TokenRelationship;
import com.mst.model.gentwo.VerbPhraseInput;
import com.mst.model.gentwo.WordToken;

public interface VerbPhraseProcessor {
	List<WordToken> process(List<WordToken> tokens, VerbPhraseInput input);
}
