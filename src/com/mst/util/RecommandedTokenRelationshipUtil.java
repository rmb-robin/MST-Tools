package com.mst.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.recommandation.RecommandedTokenRelationship;

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
	
}
