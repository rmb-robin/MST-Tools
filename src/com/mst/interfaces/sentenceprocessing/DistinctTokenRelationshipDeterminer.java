package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;

public interface DistinctTokenRelationshipDeterminer {

	List<TokenRelationship> getDistinctTokenRelationships(Sentence sentence);	
	List<RecommendedTokenRelationship> getDistinctRecommendedRelationships(List<RecommendedTokenRelationship> relationships);
}
