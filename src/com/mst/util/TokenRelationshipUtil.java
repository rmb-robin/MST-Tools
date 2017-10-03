package com.mst.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.sentenceProcessing.TokenRelationship;

public class TokenRelationshipUtil {

	public static Map<String,List<TokenRelationship>> getMapByEdgeName(List<TokenRelationship> relationships){
		Map<String,List<TokenRelationship>> result = new HashMap<>();
		for(TokenRelationship tokenRelationship: relationships){
		 if(!result.containsKey(tokenRelationship.getEdgeName()))
				 result.put(tokenRelationship.getEdgeName(), new ArrayList<TokenRelationship>());
		 
		 result.get(tokenRelationship.getEdgeName()).add(tokenRelationship); 
		}
		return result;
	}
	
	public static Map<String, List<TokenRelationship>> getMapByToFrom(List<TokenRelationship> tokenRelationships){
		Map<String, List<TokenRelationship>> result = new HashMap<>();
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			String key = tokenRelationship.getFromToken().toString() + tokenRelationship.getToToken().toString();
			if(!result.containsKey(key))
				result.put(key, new ArrayList<>());
			result.get(key).add(tokenRelationship);
		}
		
		return result; 
	}
}
