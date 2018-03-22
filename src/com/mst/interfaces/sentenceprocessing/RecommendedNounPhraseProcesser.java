package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RelationshipInput;

public interface RecommendedNounPhraseProcesser {

	List<RecommendedTokenRelationship> setNamedEdges(List<RecommendedTokenRelationship> edges, RelationshipInput input);
	RecommandedNounPhraseResult process(List<RecommendedTokenRelationship> embeddedwords);
	List<RecommendedTokenRelationship> addEdges(List<RecommendedTokenRelationship> edges, RelationshipInput input);  
}
