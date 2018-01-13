package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class TokenRelationshipFactoryImpl implements TokenRelationshipFactory {

	@Override
	public TokenRelationship create(String edgeName, String frameName, WordToken fromToken,WordToken toToken) {
		TokenRelationship tokenRelationship = new TokenRelationship();
		tokenRelationship.setUniqueIdentifier(UUID.randomUUID().toString());
		tokenRelationship.setEdgeName(edgeName);
		tokenRelationship.setFrameName(frameName);
	
		if(fromToken.getPosition()<toToken.getPosition())
		{
			tokenRelationship.setFromToken(fromToken);
			tokenRelationship.setToToken(toToken);
		}
		else 
		{
			tokenRelationship.setFromToken(toToken);
			tokenRelationship.setToToken(fromToken);
		}
		return tokenRelationship;
	}
	
	public RecommendedTokenRelationship createRecommendedRelationship(String edgeName, String frameName, WordToken fromToken,WordToken toToken){
		RecommendedTokenRelationship recommandedTokenRelationship = new RecommendedTokenRelationship();
		recommandedTokenRelationship.setTokenRelationship(create(edgeName,frameName,fromToken,toToken));
		String key = recommandedTokenRelationship.getTokenRelationship().getFromTokenToTokenString();
		recommandedTokenRelationship.setKey(key);
		return recommandedTokenRelationship;
	}
	
	public RecommendedTokenRelationship createRecommendedRelationshipFromTokenRelationship(TokenRelationship tokenRelationship){
		RecommendedTokenRelationship recommandedTokenRelationship = new RecommendedTokenRelationship();
		recommandedTokenRelationship.setTokenRelationship(tokenRelationship);
		String key = recommandedTokenRelationship.getTokenRelationship().getFromTokenToTokenString();
		recommandedTokenRelationship.setKey(key);
		return recommandedTokenRelationship;
	}
	
	public List<RecommendedTokenRelationship> createRecommendedRelationshipsFromTokenRelationships(List<TokenRelationship> tokenRelationships){
		if(tokenRelationships==null) return null;
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(TokenRelationship tokenRelationship: tokenRelationships){
			result.add(createRecommendedRelationshipFromTokenRelationship(tokenRelationship));
		}
		return result;
	}
	
	

}
