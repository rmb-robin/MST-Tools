package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.mst.interfaces.sentenceprocessing.DistinctTokenRelationshipDeterminer;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class DistinctTokenRelationshipDeterminerImpl implements DistinctTokenRelationshipDeterminer {

	@Override
	public List<TokenRelationship> getDistinctTokenRelationships(Sentence sentence) {
		
		List<TokenRelationship> result = new ArrayList<>();
		Map<String,List<TokenRelationship>> tokenRelationsByEdgeName = sentence.getTokenRelationsByNameMap();
		for(Entry<String,List<TokenRelationship>> entry: tokenRelationsByEdgeName.entrySet()){
			result.addAll(getDistinctRelationships(entry.getValue()));
		}

		return result;
	}
	
	private List<TokenRelationship> getDistinctRelationships(List<TokenRelationship> tokenRelationships){
		
		Map<String, TokenRelationship> distinctFromTo = new HashMap<String, TokenRelationship>();
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			String key = getToken(tokenRelationship.getFromToken()) + getToken(tokenRelationship.getToToken());
			if(distinctFromTo.containsKey(key))continue;
			distinctFromTo.put(key, tokenRelationship);
		}
		return new ArrayList<TokenRelationship>(distinctFromTo.values()); 
	}
	
	private String getToken(WordToken wordtoken){
		if(wordtoken==null) return "";
		return wordtoken.getToken().trim();
	}

	
	
}
