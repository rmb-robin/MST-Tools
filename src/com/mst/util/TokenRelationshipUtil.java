package com.mst.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class TokenRelationshipUtil {

	public static Map<String,List<TokenRelationship>> getMapByEdgeName(List<TokenRelationship> relationships, boolean isNamed){
		Map<String,List<TokenRelationship>> result = new HashMap<>();
		for(TokenRelationship tokenRelationship: relationships){
		
			if(!isNamed){
			
				if(!result.containsKey(tokenRelationship.getEdgeName()))
					result.put(tokenRelationship.getEdgeName(), new ArrayList<TokenRelationship>());
		 
				result.get(tokenRelationship.getEdgeName()).add(tokenRelationship); 
			}
			else {
				if(!result.containsKey(tokenRelationship.getNamedEdge()))
					result.put(tokenRelationship.getNamedEdge(), new ArrayList<TokenRelationship>());
		 
				result.get(tokenRelationship.getNamedEdge()).add(tokenRelationship); 
			}	
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
	
	public static Map<String, List<TokenRelationship>> getMapByDistinctToFrom(List<TokenRelationship> tokenRelationships){
		Map<String, List<TokenRelationship>> result = new HashMap<>();
		
		for(TokenRelationship relationship : tokenRelationships){
			String key = relationship.getFromToken().getToken();
			addToMapByKey(key, result, relationship);
			key = relationship.getToToken().getToken();
			addToMapByKey(key, result, relationship);
		}
		return result;
	}
	
    private static void addToMapByKey(String key, Map<String, List<TokenRelationship>> result, TokenRelationship relationship){
    	if(!result.containsKey(key)){
			result.put(key, new ArrayList<>());
		}
		result.get(key).add(relationship);
    }
    
    public static List<WordToken> getDistinctWordTokens(List<TokenRelationship> relationships){
    	List<WordToken> result = new ArrayList<>();
		for(TokenRelationship relationship: relationships){
			if(!result.contains(relationship.getFromToken()))
					result.add(relationship.getFromToken());
	
			if(!result.contains(relationship.getToToken()))
					result.add(relationship.getToToken());
		}
		return result;
    }
    
	public static List<TokenRelationship> getTokenRelationshipsByEdgeName(String edgeName, List<TokenRelationship> tokenRelationships){
		List<TokenRelationship> result = new ArrayList<>();
		if(tokenRelationships==null) return result;
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(tokenRelationship.getEdgeName().equals(""))continue;
			if(tokenRelationship.getEdgeName().equals(edgeName))
				result.add(tokenRelationship);
		}
		return result;
	}
	
	public static boolean isEdgeMatchFromHas(TokenRelationship tokenRelationship, HashSet<String> edgeNameHash){
		if(tokenRelationship.getNamedEdge()!=null){
			if(edgeNameHash.contains(tokenRelationship.getNamedEdge()))
					return true; 
		}
		
		if(tokenRelationship.getEdgeName()!=null){
			if(edgeNameHash.contains(tokenRelationship.getEdgeName())) 
				return true;
		}
		return false;
	}
}
