package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.WordToken;

public abstract class RelationshipProcessorBase {

	protected TokenRelationshipFactory tokenRelationshipFactory; 
	protected final String wildcard = "*";
	protected List<WordToken> wordTokens; 

	protected Map<String, List<RelationshipMapping>> relationshipMap;
	protected Map<String, List<RelationshipMapping>> semanticTypeRelationshipMap; 
	
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
	
	protected void setrelationshipMaps(List<RelationshipMapping> relationshipMappings){
		relationshipMap  = new HashMap<>();
		semanticTypeRelationshipMap = new HashMap<>();
		for(RelationshipMapping nounRelationship : relationshipMappings){
			if(nounRelationship.getIsFromSemanticType())
				setRelationshipMap(semanticTypeRelationshipMap,nounRelationship);
			else 
				setRelationshipMap(relationshipMap,nounRelationship);
		}
	}
	
	protected void setRelationshipMap(Map<String, List<RelationshipMapping>> map,RelationshipMapping nounRelationship){
		if(!map.containsKey(nounRelationship.getFromToken()))
			map.put(nounRelationship.getFromToken().toLowerCase(), new ArrayList<RelationshipMapping>());
		map.get(nounRelationship.getFromToken()).add(nounRelationship);
	}
	

 }
