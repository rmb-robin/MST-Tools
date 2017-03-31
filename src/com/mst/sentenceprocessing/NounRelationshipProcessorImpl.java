package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.mst.interfaces.NounRelationshipProcessor;
import com.mst.interfaces.TokenRelationshipFactory;
import com.mst.model.WordToken;
import com.mst.model.gentwo.NounRelationship;
import com.mst.model.gentwo.NounRelationshipInput;
import com.mst.model.gentwo.TokenRelationship;

import jdk.nashorn.internal.parser.TokenKind;

public class NounRelationshipProcessorImpl implements NounRelationshipProcessor {

	private TokenRelationshipFactory tokenRelationshipFactory; 
	private String frameName;
	private final String wildcard = "*";
	
	
	private Map<String, List<NounRelationship>> nounRelationshipMap;
	private Map<String, List<NounRelationship>> semanticTypeNounRelationshipMap; 
	private List<WordToken> wordTokens; 
	
	
	public NounRelationshipProcessorImpl(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}

	public List<TokenRelationship> process(List<WordToken> tokens, NounRelationshipInput input) {
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		this.wordTokens = tokens;
		this.frameName = input.getFrameName();
		setrelationshipMaps(input.getNounRelationships());
	
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
		 	NounRelationship nounRelationship =  checkForWildcardsFrom(wordToken);
			if(nounRelationship!=null)
				tokenRelationships.add(tokenRelationshipFactory.create(nounRelationship.getEdgeName(),this.frameName, wordTokens.get(index-1),wordToken));
		}

		if(shouldGetRelationshipsForToken(wordToken))
			tokenRelationships.addAll(getRelationshipsForToken(wordToken));
		
		return tokenRelationships;
	}
	
	private boolean shouldGetRelationshipsForToken(WordToken wordToken){
		if(wordToken.getSemanticType()!=null){
			if(semanticTypeNounRelationshipMap.containsKey(wordToken.getSemanticType())){
				return true;
			}
		}

		if(nounRelationshipMap.containsKey(wordToken.getToken().toLowerCase())) 
			return true;
		return false;
	}
	
	private NounRelationship checkForWildcardsFrom(WordToken wordToken){
		List<NounRelationship> nounRelationships = nounRelationshipMap.get(wildcard);
		if(nounRelationships==null) return null;
		for(NounRelationship relationship: nounRelationships){
			if(relationship.getToToken().equals(wordToken.getToken()))
					return relationship;
		}
		return null;
	}
	
	
	private List<TokenRelationship> getRelationshipsForToken(WordToken wordToken){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>(); 
		String key = wordToken.getToken().toLowerCase();
		Map<String,List<NounRelationship>> map = nounRelationshipMap;
		result.addAll(iterateMap(map,key,wordToken));
		if(wordToken.getSemanticType()!=null)
		{
			map = semanticTypeNounRelationshipMap;
			key = wordToken.getSemanticType();
			result.addAll(iterateMap(map,key,wordToken));
		}
		return result;
	}
	
	private List<TokenRelationship> iterateMap(Map<String,List<NounRelationship>> map, String key, WordToken wordToken){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>(); 
		if(!map.containsKey(key)) return result;
		int startIndex = wordTokens.indexOf(wordToken);
				
		for(NounRelationship relationship: map.get(key)){
			List<TokenRelationship> collection = processSingleNounRelationship(relationship,startIndex);
			result.addAll(collection);
		}
		return result;
	}

	
	private List<TokenRelationship> processSingleNounRelationship(NounRelationship nounRelationships, int index){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		if(nounRelationships.getIsToWildcard())
		{
			result.add(tokenRelationshipFactory.create(nounRelationships.getEdgeName(), this.frameName, wordTokens.get(index),wordTokens.get(index+1)));
			return result;
		}
		
		int endIndex =  Math.min(index+nounRelationships.getMaxDistance(),wordTokens.size()-1); //modified max of list or distance...
		boolean isToSemanticType = nounRelationships.getIsToSemanticType();
		for(int i = index+1; i<endIndex;i++){
			String tokenCompareVlaue = wordTokens.get(i).getToken();
			if(isToSemanticType)
				tokenCompareVlaue = wordTokens.get(i).getSemanticType();
			
			if(tokenCompareVlaue==null) continue;
			if(!tokenCompareVlaue.equals(nounRelationships.getToToken())) continue;
			result.add(tokenRelationshipFactory.create(nounRelationships.getEdgeName(), this.frameName, wordTokens.get(index),wordTokens.get(i)));
		}		
		return result;
	}
	
	private void setrelationshipMaps(List<NounRelationship> nounRelationships){
		nounRelationshipMap  = new HashMap<>();
		semanticTypeNounRelationshipMap = new HashMap<>();
		for(NounRelationship nounRelationship : nounRelationships){
			if(nounRelationship.getIsFromSemanticType())
				setRelationshipMap(semanticTypeNounRelationshipMap,nounRelationship);
			else 
				setRelationshipMap(nounRelationshipMap,nounRelationship);
		}
	}
	
	private void setRelationshipMap(Map<String, List<NounRelationship>> map,NounRelationship nounRelationship){
		if(!map.containsKey(nounRelationship.getFromToken()))
			map.put(nounRelationship.getFromToken().toLowerCase(), new ArrayList<NounRelationship>());
		map.get(nounRelationship.getFromToken()).add(nounRelationship);
	}
}
