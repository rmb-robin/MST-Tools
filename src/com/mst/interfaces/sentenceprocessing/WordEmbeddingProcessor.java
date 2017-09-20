package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface WordEmbeddingProcessor {

	List<RecommandedTokenRelationship> process(List<WordToken> tokens);
	
}
