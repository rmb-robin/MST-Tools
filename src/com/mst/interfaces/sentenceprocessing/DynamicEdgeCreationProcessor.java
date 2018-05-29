package com.mst.interfaces.sentenceprocessing;

import java.util.List;
import java.util.Map;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface DynamicEdgeCreationProcessor {

	List<TokenRelationship> process(List<DynamicEdgeCreationRule> rules, Map<String,List<TokenRelationship>> map,List<WordToken> modifiedWords);
	List<RecommendedTokenRelationship> processDiscovery(List<DynamicEdgeCreationRule> rules, SentenceDiscovery sentence);
}
