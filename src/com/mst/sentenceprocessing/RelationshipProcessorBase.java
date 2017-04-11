package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.TokenRelationshipFactory;
import com.mst.model.WordToken;
import com.mst.model.gentwo.RelationshipMapping;

public abstract class RelationshipProcessorBase {

	protected TokenRelationshipFactory tokenRelationshipFactory; 
	protected String frameName;
	protected final String wildcard = "*";
	protected List<WordToken> wordTokens; 

	public RelationshipProcessorBase(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	protected int getEndIndex(int index, int distance){
		return Math.min(index+distance,wordTokens.size()-1);
	}

	protected boolean isWordTokenMatchToRelationship(boolean isSemanticType,boolean isPosType,String relationshipToToken, WordToken wordToken){
		String tokenCompareVlaue = wordToken.getToken();
		if(isSemanticType)
			tokenCompareVlaue = wordToken.getSemanticType();
	
		else if(isPosType)
			tokenCompareVlaue = wordToken.getPos();
		
		if(tokenCompareVlaue==null) return false;
		return tokenCompareVlaue.equals(relationshipToToken);
	}
 }
