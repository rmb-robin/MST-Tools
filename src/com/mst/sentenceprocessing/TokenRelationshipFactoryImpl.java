package com.mst.sentenceprocessing;

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
	
	
}
