package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.mst.interfaces.NounRelationshipProcessor;
import com.mst.interfaces.TokenRelationshipFactory;
import com.mst.model.WordToken;
import com.mst.model.gentwo.NounRelationshipInput;
import com.mst.model.gentwo.TokenRelationship;

public class NounRelationshipProcessorImpl implements NounRelationshipProcessor {

	private TokenRelationshipFactory tokenRelationshipFactory; 
	private final String frameName = "f_related"; //this should be part of input....
	
	public NounRelationshipProcessorImpl(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	private Map<String, List<NounRelationshipInput>> nounRelationshipInputMap;
	private Map<String, List<NounRelationshipInput>> semanticTypeNounRelationshipInputMap; 
	private List<WordToken> wordTokens; 
	
	public List<TokenRelationship> process(List<WordToken> tokens, List<NounRelationshipInput> inputs) {
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		this.wordTokens = tokens;
		setrelationshipMaps(inputs);
	
		for(WordToken wordToken: wordTokens){
			List<TokenRelationship> singleTokenResult = processSingleToken(wordToken);
			if(singleTokenResult!=null)
				result.addAll(singleTokenResult);
		}
		return result;
	}

	private List<TokenRelationship> processSingleToken(WordToken wordToken){
		if(wordToken.getSemanticType()!=null){
			if(semanticTypeNounRelationshipInputMap.containsKey(wordToken.getSemanticType()))
					return getRelationshipsForToken(wordToken);
		}
		
		if(nounRelationshipInputMap.containsKey(wordToken.getToken())) {
			return getRelationshipsForToken(wordToken);
		}
		return null;
	}
	
	
	private List<TokenRelationship> getRelationshipsForToken(WordToken wordToken){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		int startIndex = wordTokens.indexOf(wordToken);
		for(NounRelationshipInput input: nounRelationshipInputMap.get(wordToken.getToken())){
			List<TokenRelationship> collection = processSingleNounRelationshipInput(input,startIndex);
			result.addAll(collection);
		}
		return result;
	}
	
	private List<TokenRelationship> processSingleNounRelationshipInput(NounRelationshipInput nounRelationshipInput, int index){
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		int endIndex = index+nounRelationshipInput.getMaxDistance();
		for(int i = index+1; index<endIndex;i++){
			if(!wordTokens.get(i).getToken().equals(nounRelationshipInput.getToToken())) continue;
			result.add(tokenRelationshipFactory.create(nounRelationshipInput.getEdgeName(), this.frameName, wordTokens.get(index),wordTokens.get(i)));
		}		
		return result;
	}
	
	private void setrelationshipMaps(List<NounRelationshipInput> inputs){
		nounRelationshipInputMap  = new HashMap<>();
		semanticTypeNounRelationshipInputMap = new HashMap<>();
		for(NounRelationshipInput nounRelationshipInput : inputs){
			if(nounRelationshipInput.getIsFromSemanticType())
				setRelationshipMap(semanticTypeNounRelationshipInputMap,nounRelationshipInput);
			else 
				setRelationshipMap(nounRelationshipInputMap,nounRelationshipInput);
		}
	}
	
	private void setRelationshipMap(Map<String, List<NounRelationshipInput>> map,NounRelationshipInput nounRelationshipInput){
		if(!map.containsKey(nounRelationshipInput.getFromToken()))
			map.put(nounRelationshipInput.getFromToken(), new ArrayList<NounRelationshipInput>());
		map.get(nounRelationshipInput.getFromToken()).add(nounRelationshipInput);
	}
}
