package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.PrepPhraseRelationshipMapping;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface PrepPhraseRelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens, List<PrepPhraseRelationshipMapping> prepPhraseRelationshipMappings);
}