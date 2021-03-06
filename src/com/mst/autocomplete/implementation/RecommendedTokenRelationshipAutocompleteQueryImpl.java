package com.mst.autocomplete.implementation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mst.autocomplete.interfaces.RecommendedTokenRelationshipAutocompleteQuery;
import com.mst.cache.implementation.RecommendedTokenRelationshipCacheManagerImpl;
import com.mst.cache.interfaces.RecommendedTokenRelationshipCacheManager;
import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class RecommendedTokenRelationshipAutocompleteQueryImpl implements RecommendedTokenRelationshipAutocompleteQuery {

	private RecommendedTokenRelationshipCacheManager cacheManager; 
	
	public RecommendedTokenRelationshipAutocompleteQueryImpl(){
		cacheManager = new RecommendedTokenRelationshipCacheManagerImpl();
	}
	public List<String> getNextWord(AutoCompleteRequest request) {
		List<String> result = new ArrayList<>();
		if(request.getTokens().size()==0) return result;
		if(request.getTokens().size()==1)
			return processSingleToken(request.getTokens().get(0));
		return processMultipleTokens(request.getTokens());
	}

	private List<String> getNextToken(RecommendedTokenRelationship recommandedTokenRelationship, List<String> searchTokens, boolean isLast) {
		String joined = String.join(" ", searchTokens);
		List<String> result = new ArrayList<String>();
		for(String link: recommandedTokenRelationship.getTokenRelationship().getLinks()){
			RecommendedTokenRelationship retrieved = cacheManager.getItem(link);
			if(retrieved==null) continue; 
			result.add(joined + " " + retrieved.getTokenRelationship().getToToken().getToken() + getNextTokensValuesForFinalToken(retrieved));	
		}
		return result;
	}
	
	private String getNextTokensValuesForFinalToken(RecommendedTokenRelationship finalToken){
		String edgeName = finalToken.getTokenRelationship().getEdgeName(); 
		if(!edgeName.equals(WordEmbeddingTypes.tokenToken) && !edgeName.equals(WordEmbeddingTypes.verbPlus)) return "";

		// doing just one link for now...
		 String result = "";
	     while(true){
	    	 if(finalToken.getTokenRelationship().getLinks().isEmpty()) return result;
	    	 RecommendedTokenRelationship retrieved = cacheManager.getItem(finalToken.getTokenRelationship().getLinks().get(0));
	    	 result += " " + retrieved.getTokenRelationship().getToToken().getToken();
	    	 if(endTextTokenCylce(retrieved)) return result; 
	    	 finalToken = retrieved;
	     }
	}
	
	private boolean endTextTokenCylce(RecommendedTokenRelationship recommandedTokenRelationship){
	
		PropertyValueTypes toTokenPropertyValuesType = recommandedTokenRelationship.getTokenRelationship().getToToken().getPropertyValueType();
		if(toTokenPropertyValuesType !=null && toTokenPropertyValuesType.equals(PropertyValueTypes.NounPhraseEnd)) 
			return true; 
	
		if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.verbMinus) ||
				recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.prepMinus))
			return true; 
		return false;
	}
	
	private List<String> processMultipleTokens(List<String> tokens){
		HashSet<String> result = new HashSet<>();
		
		List<RecommendedTokenRelationship> relationships = cacheManager.getListByKey(tokens.get(0));
		List<RecommendedTokenRelationship> filtered = new ArrayList<>();
		for(RecommendedTokenRelationship recommandedTokenRelationship: relationships){
			TokenRelationship tokenRelationship = recommandedTokenRelationship.getTokenRelationship();
			if(!tokenRelationship.getFromToken().getToken().equals(tokens.get(0))) continue; 
			if(!tokenRelationship.getToToken().getToken().equals(tokens.get(1))) continue; 
			if(tokenRelationship.getLinks().isEmpty()) continue;
			result.addAll(getNextToken(recommandedTokenRelationship, tokens, true));	
		}		
		return new ArrayList<String>(result);
	}
	
	private List<String> processSingleToken(String token){
		HashSet<String> result = new HashSet<>();
		List<RecommendedTokenRelationship> relationships = cacheManager.getListByKey(token);
		for(RecommendedTokenRelationship recommandedTokenRelationship: relationships){
			result.add(token + " " + recommandedTokenRelationship.getTokenRelationship().getOppositeToken(token));
		}
		return new ArrayList<String>(result);
	}
	
	
}
