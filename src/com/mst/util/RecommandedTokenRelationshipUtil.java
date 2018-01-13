package com.mst.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class RecommandedTokenRelationshipUtil {

	public static Map<String, List<RecommendedTokenRelationship>> getMapByDistinctToFrom(List<RecommendedTokenRelationship> tokenRelationships){
		Map<String, List<RecommendedTokenRelationship>> result = new HashMap<>();
		
		for(RecommendedTokenRelationship relationship : tokenRelationships){
			String key = relationship.getTokenRelationship().getFromToken().getToken();
			addToMapByKey(key, result, relationship);
			key = relationship.getTokenRelationship().getToToken().getToken();
			addToMapByKey(key, result, relationship);
		}
		return result;
	}
	
    private static void addToMapByKey(String key, Map<String, List<RecommendedTokenRelationship>> result, RecommendedTokenRelationship relationship){
    	if(!result.containsKey(key)){
			result.put(key, new ArrayList<>());
		}
		result.get(key).add(relationship);
    }
	
    public static List<TokenRelationship> getTokenRelationshipsFromRecommendedTokenRelationships(List<RecommendedTokenRelationship> recommandedTokenRelationships){
    	List<TokenRelationship> tokenRelationships = new ArrayList<>();
    	
    	for(RecommendedTokenRelationship recommandedTokenRelationship: recommandedTokenRelationships){
    		tokenRelationships.add(recommandedTokenRelationship.getTokenRelationship());
    	}
    	return tokenRelationships;
    }
    
    public static Map<String, RecommendedTokenRelationship> getByUniqueKey(List<RecommendedTokenRelationship> recommandedTokenRelationships){
    	Map<String, RecommendedTokenRelationship> result = new HashMap<>();
    	
    	for(RecommendedTokenRelationship recommandedTokenRelationship: recommandedTokenRelationships){
    		result.put(recommandedTokenRelationship.getKey(),recommandedTokenRelationship);
    	}
    	return result;
    }
    
    public static RecommendedTokenRelationship getByEdgeName(List<RecommendedTokenRelationship> recommandedTokenRelationships, String edgeName){
    	for(RecommendedTokenRelationship recommandedTokenRelationship: recommandedTokenRelationships){
    		if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(edgeName))return recommandedTokenRelationship;
    	}
    	return null;
    }
    
    public static HashSet<String> getKeyForSentenceDiscovery(SentenceDiscovery discovery){
		HashSet<String> result = new HashSet<>();
		for(int i = 0; i < discovery.getModifiedWordList().size()-1;i++){
			String from = discovery.getModifiedWordList().get(i).getToken();
			for(int j = i+1; j<discovery.getModifiedWordList().size();j++){
				String key = from + discovery.getModifiedWordList().get(j).getToken(); 
				result.add(key);
			}
		}
		return result;
	}
    
    public static List<RecommendedTokenRelationship> filterByEdgeNames(HashSet<String> edgeNames, List<RecommendedTokenRelationship> recommendedTokenRelationships){
    	List<RecommendedTokenRelationship> result = new ArrayList<>();
    	
    	for(RecommendedTokenRelationship relationship: recommendedTokenRelationships){
    		if(edgeNames.contains(relationship.getTokenRelationship().getEdgeName()))
    				result.add(relationship);
    	}
    	return result;
    }
    
    public static boolean doRelationshipsContainVerb(List<RecommendedTokenRelationship> relationships){
    	for(RecommendedTokenRelationship relationship: relationships){
    		String edgeName = relationship.getTokenRelationship().getEdgeName();
    		if(edgeName.equals(WordEmbeddingTypes.bothVerbs)) return true;
    		if(edgeName.equals(WordEmbeddingTypes.firstVerb)) return true;
    		if(edgeName.equals(WordEmbeddingTypes.secondVerb)) return true;  		
    	}
    	return false;  
    }
	
    public static boolean isDefault(RecommendedTokenRelationship edge){
		return edge.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge);
	}
}
