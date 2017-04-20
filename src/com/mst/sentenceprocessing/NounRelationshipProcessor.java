package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.mst.interfaces.RelationshipProcessor;
import com.mst.interfaces.TokenRelationshipFactory;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.sentenceProcessing.RelationshipInput;
import com.mst.model.sentenceProcessing.RelationshipMapping;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

import jdk.nashorn.internal.parser.TokenKind;

public class NounRelationshipProcessor extends RelationshipProcessorBase implements RelationshipProcessor {

	protected Map<String, List<RelationshipMapping>> relationshipMap;
	protected Map<String, List<RelationshipMapping>> semanticTypeRelationshipMap; 
	
	
	public List<TokenRelationship> process(List<WordToken> tokens, RelationshipInput input) {
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		
		this.wordTokens = tokens;
		setrelationshipMaps(input.getRelationshipMappings());
	
		for(WordToken wordToken: wordTokens){	
			List<TokenRelationship> singleTokenResult = processSingleToken(wordToken);
			if(!singleTokenResult.isEmpty())
				result.addAll(singleTokenResult);
		}
		return result;
	}
	
	private void setrelationshipMaps(List<RelationshipMapping> relationshipMappings){
		relationshipMap  = new HashMap<>();
		semanticTypeRelationshipMap = new HashMap<>();
		for(RelationshipMapping nounRelationship : relationshipMappings){
			if(nounRelationship.getIsFromSemanticType())
				setRelationshipMap(semanticTypeRelationshipMap,nounRelationship);
			else 
				setRelationshipMap(relationshipMap,nounRelationship);
		}
	}
	
	private RelationshipMapping findMapping(Map<String, List<RelationshipMapping>> map, WordToken fromWordToken, WordToken toWordToken){
		String key = fromWordToken.getToken().toLowerCase();
		if(!map.containsKey(key)) return null;
		
		for(RelationshipMapping relationshipMapping : map.get(key)){
			if(isWordTokenMatchToRelationship(relationshipMapping.getIsToSemanticType(),false, relationshipMapping.getToToken(), toWordToken))
				return relationshipMapping;
		}
		return null;
	}
 
	private boolean shouldGetRelationshipsForFromToken(WordToken FromToken){
		if(FromToken.getSemanticType()!=null){
			if(semanticTypeRelationshipMap.containsKey(FromToken.getSemanticType())){
				return true;
			}
		}

		if(relationshipMap.containsKey(FromToken.getToken().toLowerCase())) 
			return true;
		return false;
	}
	
	private void setRelationshipMap(Map<String, List<RelationshipMapping>> map,RelationshipMapping nounRelationship){
		if(!map.containsKey(nounRelationship.getFromToken()))
			map.put(nounRelationship.getFromToken().toLowerCase(), new ArrayList<RelationshipMapping>());
		map.get(nounRelationship.getFromToken()).add(nounRelationship);
	}

	private List<TokenRelationship> processSingleToken(WordToken wordToken){
		List<TokenRelationship> tokenRelationships = new ArrayList<>();
		int index = wordTokens.indexOf(wordToken);
		if(index>0)
		{
		 	RelationshipMapping nounRelationship =  findRelationWildcardFrom(wordToken);
			if(nounRelationship!=null)
				tokenRelationships.add(createRelationshipAndAnnotateWordTokens(nounRelationship.getEdgeName(), wordTokens.get(index-1),wordToken));
		}

		if(shouldGetRelationshipsForFromToken(wordToken))
			tokenRelationships.addAll(getRelationshipsForToken(wordToken));
		
		return tokenRelationships;
	}
	
	
	private List<TokenRelationship> getRelationshipsForToken(WordToken wordToken){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>(); 
		String key = wordToken.getToken().toLowerCase();
		Map<String,List<RelationshipMapping>> map = relationshipMap;
		result.addAll(iterateMap(map,key,wordToken));
		if(wordToken.getSemanticType()!=null)
		{
			map = semanticTypeRelationshipMap;
			key = wordToken.getSemanticType();
			result.addAll(iterateMap(map,key,wordToken));
		}
		return result;
	}
	
	private List<TokenRelationship> iterateMap(Map<String,List<RelationshipMapping>> map, String key, WordToken wordToken){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>(); 
		if(!map.containsKey(key)) return result;
		int startIndex = wordTokens.indexOf(wordToken);
				
		for(RelationshipMapping relationship: map.get(key)){
			List<TokenRelationship> collection = processSingleNounRelationship(relationship,startIndex);
			result.addAll(collection);
		}
		return result;
	}

	
	private List<TokenRelationship> processSingleNounRelationship(RelationshipMapping relationshipMapping, int startIndex){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		if(relationshipMapping.getIsToWildcard())
		{
			result.add(createRelationshipAndAnnotateWordTokens(relationshipMapping.getEdgeName(), wordTokens.get(startIndex),wordTokens.get(startIndex+1)));
			return result;
		}
		
		int endIndex =  getEndIndex(startIndex,relationshipMapping.getMaxDistance());
		for(int i = startIndex+1; i<endIndex;i++){
			WordToken toToken = wordTokens.get(i);
			if(isWordTokenMatchToRelationship(relationshipMapping.getIsFromSemanticType(), false,relationshipMapping.getFromToken(), toToken))
				result.add(createRelationshipAndAnnotateWordTokens(relationshipMapping.getEdgeName(), wordTokens.get(startIndex),wordTokens.get(i)));
		}		
		return result;
	}

	private TokenRelationship createRelationshipAndAnnotateWordTokens(String edgeName,WordToken fromToken,WordToken toToken){
		fromToken.setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
		toToken.setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
		return tokenRelationshipFactory.create(edgeName, EdgeTypes.related, fromToken,toToken);
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
