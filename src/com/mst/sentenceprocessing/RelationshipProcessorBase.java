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
	
	
	protected Map<String, List<RelationshipMapping>> relationshipMap;
	protected Map<String, List<RelationshipMapping>> semanticTypeRelationshipMap; 
	protected List<WordToken> wordTokens; 
	
	
	public RelationshipProcessorBase(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
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
 
	private void setRelationshipMap(Map<String, List<RelationshipMapping>> map,RelationshipMapping nounRelationship){
		if(!map.containsKey(nounRelationship.getFromToken()))
			map.put(nounRelationship.getFromToken().toLowerCase(), new ArrayList<RelationshipMapping>());
		map.get(nounRelationship.getFromToken()).add(nounRelationship);
	}
	
 
	protected int getEndIndex(int index, int distance){
		return Math.min(index+distance,wordTokens.size()-1);
	}
	
	protected boolean shouldGetRelationshipsForFromToken(WordToken FromToken){
		if(FromToken.getSemanticType()!=null){
			if(semanticTypeRelationshipMap.containsKey(FromToken.getSemanticType())){
				return true;
			}
		}

		if(relationshipMap.containsKey(FromToken.getToken().toLowerCase())) 
			return true;
		return false;
	}
	
	protected RelationshipMapping findMapping(Map<String, List<RelationshipMapping>> map, WordToken fromWordToken, WordToken toWordToken){
		String key = fromWordToken.getToken().toLowerCase();
		if(!map.containsKey(key)) return null;
		
		for(RelationshipMapping relationshipMapping : map.get(key)){
			if(mappingsContainToToken(relationshipMapping, toWordToken))
				return relationshipMapping;
		}
		return null;
	}
	

	protected boolean mappingsContainToToken(RelationshipMapping relationshipMapping, WordToken toToken){
		String tokenCompareVlaue = toToken.getToken();
		if(relationshipMapping.getIsToSemanticType())
			tokenCompareVlaue = toToken.getSemanticType();
		
		if(tokenCompareVlaue==null) return false;
		return tokenCompareVlaue.equals(relationshipMapping.getToToken());
	}
 
	protected RelationshipMapping findRelationWildcardFrom(WordToken wordToken){
		List<RelationshipMapping> nounRelationships = relationshipMap.get(wildcard);
		if(nounRelationships==null) return null;
		for(RelationshipMapping relationship: nounRelationships){
			if(relationship.getToToken().equals(wordToken.getToken()))
					return relationship;
		}
		return null;
	}
}
