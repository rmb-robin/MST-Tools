package com.mst.autocomplete.implementation;

import java.util.ArrayList;
import java.util.List;

import com.mst.autocomplete.interfaces.RecommendedTokenRelationshipAutocompleteQuery;
import com.mst.cache.implementation.RecommendedTokenRelationshipCacheManagerImpl;
import com.mst.cache.interfaces.RecommendedTokenRelationshipCacheManager;
import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.recommandation.RecommandedTokenRelationship;

public class RecommendedTokenRelationshipAutocompleteQueryImpl implements RecommendedTokenRelationshipAutocompleteQuery {

	private RecommendedTokenRelationshipCacheManager cacheManager; 
	
	public RecommendedTokenRelationshipAutocompleteQueryImpl(){
		cacheManager = new RecommendedTokenRelationshipCacheManagerImpl();
	}
	public List<String> getNextWord(AutoCompleteRequest request) {
		List<String> result = new ArrayList<>();
		if(request.getTokens().size()==0) return result;
		return processSingleToken(request.getTokens().get(0));
	}

	private List<String> processSingleToken(String token){
		List<String> result = new ArrayList<>();
		List<RecommandedTokenRelationship> relationships = cacheManager.getListByKey(token);
		for(RecommandedTokenRelationship recommandedTokenRelationship: relationships){
			result.add(recommandedTokenRelationship.getTokenRelationship().getOppositeToken(token));
		}
		return result;
	}
	
	
}
