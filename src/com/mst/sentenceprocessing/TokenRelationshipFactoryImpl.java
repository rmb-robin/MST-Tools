package com.mst.sentenceprocessing;

import org.joda.time.DateTime;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class TokenRelationshipFactoryImpl implements TokenRelationshipFactory {

	@Override
	public TokenRelationship create(String edgeName, String frameName, WordToken fromToken,WordToken toToken) {
		TokenRelationship tokenRelationship = new TokenRelationship();
		tokenRelationship.setEdgeName(edgeName);
		tokenRelationship.setFrameName(frameName);
		tokenRelationship.setFromToken(fromToken);
		tokenRelationship.setToToken(toToken);
		//tokenRelationship.setCreatedTime(DateTime.now());
		return tokenRelationship;
	}
}
