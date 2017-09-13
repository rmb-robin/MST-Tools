package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface WordEmbeddingProcessor {

	List<TokenRelationship> process(List<WordToken> tokens);
	
}
