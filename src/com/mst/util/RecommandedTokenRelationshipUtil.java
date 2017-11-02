package com.mst.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class RecommandedTokenRelationshipUtil {

	public static Map<String, List<RecommandedTokenRelationship>> getMapByDistinctToFrom(List<RecommandedTokenRelationship> tokenRelationships){
		Map<String, List<RecommandedTokenRelationship>> result = new HashMap<>();
		
		for(RecommandedTokenRelationship relationship : tokenRelationships){
			String key = relationship.getTokenRelationship().getFromToken().getToken();
			addToMapByKey(key, result, relationship);
			key = relationship.getTokenRelationship().getToToken().getToken();
			addToMapByKey(key, result, relationship);
		}
		return result;
	}
	
    private static void addToMapByKey(String key, Map<String, List<RecommandedTokenRelationship>> result, RecommandedTokenRelationship relationship){
    	if(!result.containsKey(key)){
			result.put(key, new ArrayList<>());
		}
		result.get(key).add(relationship);
    }
	
    public static List<TokenRelationship> getTokenRelationshipsFromRecommendedTokenRelationships(List<RecommandedTokenRelationship> recommandedTokenRelationships){
    	List<TokenRelationship> tokenRelationships = new ArrayList<>();
    	
    	for(RecommandedTokenRelationship recommandedTokenRelationship: recommandedTokenRelationships){
    		tokenRelationships.add(recommandedTokenRelationship.getTokenRelationship());
    	}
    	return tokenRelationships;
    }
    
    public static Map<String, RecommandedTokenRelationship> getByUniqueKey(List<RecommandedTokenRelationship> recommandedTokenRelationships){
    	Map<String, RecommandedTokenRelationship> result = new HashMap<>();
    	
    	for(RecommandedTokenRelationship recommandedTokenRelationship: recommandedTokenRelationships){
    		result.put(recommandedTokenRelationship.getKey(),recommandedTokenRelationship);
    	}
    	return result;
    }
}
