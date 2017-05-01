package com.mst.sentenceprocessing;

import java.util.List;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.sentenceProcessing.WordToken;

public abstract class RelationshipProcessorBase {

	protected TokenRelationshipFactory tokenRelationshipFactory; 
	protected final String wildcard = "*";
	protected List<WordToken> wordTokens; 

	public RelationshipProcessorBase(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	protected int getEndIndex(int index, int distance){
		return Math.min(index+distance,wordTokens.size()-1);
	}

	protected boolean isWordTokenMatchToRelationship(boolean isSemanticType,boolean isPosType,String relationshipToToken, WordToken toToken){
		String tokenCompareVlaue = toToken.getToken();
		if(isSemanticType)
			tokenCompareVlaue = toToken.getSemanticType();
	
		else if(isPosType)
			tokenCompareVlaue = toToken.getPos();
		
		if(tokenCompareVlaue==null) return false;
		return tokenCompareVlaue.equals(relationshipToToken);
	}
 }
