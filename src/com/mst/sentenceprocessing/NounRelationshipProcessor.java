package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.mst.interfaces.RelationshipProcessor;
import com.mst.interfaces.TokenRelationshipFactory;
import com.mst.model.WordToken;
import com.mst.model.gentwo.RelationshipMapping;
import com.mst.model.gentwo.RelationshipInput;
import com.mst.model.gentwo.PropertyValueTypes;
import com.mst.model.gentwo.TokenRelationship;

import jdk.nashorn.internal.parser.TokenKind;

public class NounRelationshipProcessor extends RelationshipProcessorBase implements RelationshipProcessor {

	public List<TokenRelationship> process(List<WordToken> tokens, RelationshipInput input) {
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		
		this.wordTokens = tokens;
		this.frameName = input.getFrameName();
		setrelationshipMaps(input.getRelationshipMappings());
	
		for(WordToken wordToken: wordTokens){	
			List<TokenRelationship> singleTokenResult = processSingleToken(wordToken);
			if(!singleTokenResult.isEmpty())
				result.addAll(singleTokenResult);
		}
		return result;
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
			if(mappingsContainToToken(relationshipMapping, toToken))
				result.add(createRelationshipAndAnnotateWordTokens(relationshipMapping.getEdgeName(), wordTokens.get(startIndex),wordTokens.get(i)));
		}		
		return result;
	}

	private TokenRelationship createRelationshipAndAnnotateWordTokens(String edgeName,WordToken fromToken,WordToken toToken){
		fromToken.setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
		toToken.setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
		return tokenRelationshipFactory.create(edgeName, this.frameName, fromToken,toToken);
	}
	

}
