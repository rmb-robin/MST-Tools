package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface TokenRelationshipFactory {

	TokenRelationship create(String edgeName,String frameName,WordToken fromToken,WordToken toToken, String source);
	RecommendedTokenRelationship createRecommendedRelationship(String edgeName, String frameName, WordToken fromToken,WordToken toToken);
	RecommendedTokenRelationship createRecommendedRelationshipFromTokenRelationship(TokenRelationship tokenRelationship);
	List<RecommendedTokenRelationship> createRecommendedRelationshipsFromTokenRelationships(List<TokenRelationship> tokenRelationships);
	RecommendedTokenRelationship deepCopy(RecommendedTokenRelationship original);
}
