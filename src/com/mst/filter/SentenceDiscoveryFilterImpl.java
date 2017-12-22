package com.mst.filter;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.filter.SentenceDiscoveryFilter;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;

public class SentenceDiscoveryFilterImpl implements SentenceDiscoveryFilter {

	public List<SentenceDiscovery> filter(List<SentenceDiscovery> sentenceDiscoveries, String text) {
		String[] tokens = text.split(" ");
		String left = tokens[0];
		String right  = tokens[tokens.length-1];
		
		List<SentenceDiscovery> result = new ArrayList<>();
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			if(include(sentenceDiscovery, left, right))
				result.add(sentenceDiscovery);
		}
		return result;
	}
	
	
	private boolean include(SentenceDiscovery sentenceDiscovery,String left, String right){
		if(sentenceDiscovery.getWordEmbeddings()==null || sentenceDiscovery.getWordEmbeddings().isEmpty())return false;
	
		for(int i = 0;i<sentenceDiscovery.getWordEmbeddings().size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = sentenceDiscovery.getWordEmbeddings().get(i);
			String fromToken = recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken();
			String toToken = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
			String edgeName = getEdgeName(recommandedTokenRelationship);
			//left.
			if(fromToken.equals(left))
			{
				if(edgeName.equals(WordEmbeddingTypes.firstVerb) 
						&& isLeftMatchVerb(fromToken,sentenceDiscovery.getWordEmbeddings(), i+1, true)){
					if(!includeOnTokenTokenRight(sentenceDiscovery.getWordEmbeddings(),i+1)) return false;
					return true;
				}
				
				if(edgeName.equals(WordEmbeddingTypes.defaultEdge) && 
			       isLeftMatchVerb(fromToken,sentenceDiscovery.getWordEmbeddings(), i+1, false)){
					if(!includeOnTokenTokenLeft(sentenceDiscovery.getWordEmbeddings(),i-1)) return false;
					return true;
				}
			}
		
			//right.
			if(toToken.equals(right) && edgeName.equals(WordEmbeddingTypes.defaultEdge) 
					&& isRightMatchVerb(toToken,sentenceDiscovery.getWordEmbeddings(), i+1)){
				
				if(!includeOnTokenTokenRight(sentenceDiscovery.getWordEmbeddings(),i+1)) return false;
				return true;
				
			}
			
			if(isRightMatchOnToAndFromToken(edgeName, toToken, fromToken, right)){
				if(!includeOnTokenTokenRight(sentenceDiscovery.getWordEmbeddings(),i+1)) return false;
				return true;
				
			}
		}
	
		return false;
	}
	

	private boolean isRightMatchOnToAndFromToken(String edgeName, String toToken, String fromToken, String right){
		if(right.equals(toToken)){
			if(edgeName.equals(WordEmbeddingTypes.firstVerb)) return true;
			if(edgeName.equals(WordEmbeddingTypes.bothVerbs)) return true;
		}
		
		if(right.equals(fromToken)){
			if(edgeName.equals(WordEmbeddingTypes.bothVerbs))return true;
		}
		return false;
	}

	
	
	private String getEdgeName(RecommendedTokenRelationship recommandedTokenRelationship){
		return recommandedTokenRelationship.getTokenRelationship().getEdgeName();
	}
	
	private boolean isLeftMatchVerb(String fromToken, List<RecommendedTokenRelationship> wordEmbeddings, int index, boolean isFirst){
	//	if(index>=wordEmbeddings.size()) return false;
		for(int i = 0;i<wordEmbeddings.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!recommandedTokenRelationship.getTokenRelationship().getToToken().getToken().equals(fromToken))continue;
			
			if(isFirst){
				
				if(edgeName.equals(WordEmbeddingTypes.secondVerb) || edgeName.equals(WordEmbeddingTypes.bothVerbs)) return true;
			}
			else 
			{
				if(edgeName.equals(WordEmbeddingTypes.firstVerb)) return true;
				
			}
	
		}
		return false;
	}
	
	private boolean isRightMatchVerb(String toToken, List<RecommendedTokenRelationship> wordEmbeddings, int index){
	//	if(index>=wordEmbeddings.size()) return false;
		for(int i = 0;i<wordEmbeddings.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken().equals(toToken))continue;
			if(edgeName.equals(WordEmbeddingTypes.secondVerb)) return true;
		}
		return false;
	}
	
	
	private boolean includeOnTokenTokenRight(List<RecommendedTokenRelationship> wordEmbeddings, int index){
		for(int i = 0;i<wordEmbeddings.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!edgeName.equals(WordEmbeddingTypes.defaultEdge))continue;
			return true;
		}
		return false;
	}

	
	private boolean includeOnTokenTokenLeft(List<RecommendedTokenRelationship> wordEmbeddings, int index){
		for(int i = wordEmbeddings.size()-1;i>=0;i--){
			RecommendedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!edgeName.equals(WordEmbeddingTypes.defaultEdge))continue;
			return true;
		}
		return false;
	}
	
}
