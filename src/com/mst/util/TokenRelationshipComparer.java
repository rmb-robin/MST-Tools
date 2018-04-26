package com.mst.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.model.sentenceProcessing.TokenRelationship;

public class TokenRelationshipComparer {

	public static boolean areCollectionsSame(List<TokenRelationship> firstCollection, 
			List<TokenRelationship> secondCollection, boolean useSecondAsDiscovery) {
		
		if(!areSizesSame(firstCollection, secondCollection)) return false;
		Map<String, List<TokenRelationship>> firstCollectionMap = TokenRelationshipUtil.getMapByEdgeName(firstCollection, false);
		Map<String, List<TokenRelationship>> secondCollectionMap = TokenRelationshipUtil.getMapByEdgeName(secondCollection, useSecondAsDiscovery);
		return compareCollections(firstCollectionMap, secondCollectionMap);
	}
	
	private static boolean areSizesSame(List<TokenRelationship> firstCollection, 
			List<TokenRelationship> secondCollection){
		return firstCollection.size()== secondCollection.size();
	}
	  
	
	private static boolean compareCollections(Map<String, List<TokenRelationship>> firstCollectionMap, Map<String, List<TokenRelationship>> secondCollection){
		for(Entry<String, List<TokenRelationship>> entry: firstCollectionMap.entrySet()){			
			String key = entry.getKey();
			if(!secondCollection.containsKey(key)) return false; 
			int firstCollectionCountForEdge = entry.getValue().size();
			List<TokenRelationship> secondCollectionRelationships= secondCollection.get(key);
			int secondCollectionCountForEdge = secondCollectionRelationships.size();
			if(firstCollectionCountForEdge!=secondCollectionCountForEdge) return false; 
			if(!compareCollectionWithSameEdgeName(entry.getValue(), secondCollectionRelationships))
					return false; 
		}
		return true; 
	}
	
	
	private static boolean compareCollectionWithSameEdgeName(List<TokenRelationship> firstCollcetion, List<TokenRelationship> secondCollection){
		Map<String, List<TokenRelationship>> firstCollectionMap = TokenRelationshipUtil.getMapByToFrom(firstCollcetion);
		Map<String, List<TokenRelationship>> secondCollectionMap  = TokenRelationshipUtil.getMapByToFrom(secondCollection);
		
		for(Entry<String, List<TokenRelationship>> entry: firstCollectionMap.entrySet()){			
			
			String key = entry.getKey();
			if(!secondCollectionMap.containsKey(key)) return false;	
		}
		return true;
	}
	
	
	
}
