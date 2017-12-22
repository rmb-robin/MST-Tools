package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface WordEmbeddingProcessor {

	List<RecommendedTokenRelationship> process(List<WordToken> tokens);
	
}
