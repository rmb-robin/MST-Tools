package com.mst.interfaces.sentenceprocessing;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;

public interface AdditionalExistenceEdgeProcessor {
	TokenRelationship process(Sentence sentence);
	RecommendedTokenRelationship processDiscovery(SentenceDiscovery discovery);
}
