package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.RelationshipInput;

public interface NounRelationshipProcesserSentenceDiscovery {
	void process(List<RecommendedTokenRelationship> edges, RelationshipInput input);
}
