package com.mst.sentenceprocessing;

import com.mst.interfaces.TokenRelationshipFactory;
import com.mst.model.WordToken;
import com.mst.model.gentwo.TokenRelationship;

public class TokenRelationshipFactoryImpl implements TokenRelationshipFactory {

	@Override
	public TokenRelationship create(String edgeName, String frameName, WordToken fromToken,WordToken toToken) {
		TokenRelationship tokenRelationship = new TokenRelationship();
		tokenRelationship.setEdgeName(edgeName);
		tokenRelationship.setFrameName(frameName);
		tokenRelationship.setFromToken(fromToken);
		tokenRelationship.setToToken(toToken);
		return tokenRelationship;
	}
}
