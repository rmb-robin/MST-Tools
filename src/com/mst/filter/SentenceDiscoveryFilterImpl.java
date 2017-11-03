package com.mst.filter;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.filter.SentenceDiscoveryFilter;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommandedTokenRelationship;
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
	
		boolean isLeftMatch = false;
		boolean isRightMatch = false;
		for(int i = 0;i<sentenceDiscovery.getWordEmbeddings().size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship = sentenceDiscovery.getWordEmbeddings().get(i);
			String fromToken = recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken();
			String toToken = recommandedTokenRelationship.getTokenRelationship().getToToken().getToken();
			String edgeName = getEdgeName(recommandedTokenRelationship);
			//left.
			if(fromToken.equals(left))
			{
				if(!isLeftMatch && edgeName.equals(WordEmbeddingTypes.firstVerb) 
						&& isLeftMatchVerb(fromToken,sentenceDiscovery.getWordEmbeddings(), i+1, true)){
					if(!includeOnTokenTokenRight(sentenceDiscovery.getWordEmbeddings(),i+1)) return false;
					isLeftMatch = true;
				}
				
				if(!isLeftMatch && edgeName.equals(WordEmbeddingTypes.defaultEdge) && 
			       isLeftMatchVerb(fromToken,sentenceDiscovery.getWordEmbeddings(), i+1, true)){
					if(!includeOnTokenTokenLeft(sentenceDiscovery.getWordEmbeddings(),i-1)) return false;
					isLeftMatch = true;
				}
			}
		
			//right.
			if(!isRightMatch && toToken.equals(right) && edgeName.equals(WordEmbeddingTypes.defaultEdge) 
					&& isRightMatchVerb(toToken,sentenceDiscovery.getWordEmbeddings(), i+1)){
				
				if(!includeOnTokenTokenRight(sentenceDiscovery.getWordEmbeddings(),i+1)) return false;
				isRightMatch = true;
				
			}
			
			if(isRightMatchOnToAndFromToken(edgeName, toToken, fromToken, right)){
				if(!includeOnTokenTokenRight(sentenceDiscovery.getWordEmbeddings(),i+1)) return false;
				isRightMatch = true;
				
			}
			if(isLeftMatch && isRightMatch) return true;
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

	
	
	private String getEdgeName(RecommandedTokenRelationship recommandedTokenRelationship){
		return recommandedTokenRelationship.getTokenRelationship().getEdgeName();
	}
	
	private boolean isLeftMatchVerb(String fromToken, List<RecommandedTokenRelationship> wordEmbeddings, int index, boolean isFirst){
		if(index<=wordEmbeddings.size()) return false;
		for(int i = index;i<wordEmbeddings.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
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
	

	
	private boolean isRightMatchVerb(String toToken, List<RecommandedTokenRelationship> wordEmbeddings, int index){
		if(index<=wordEmbeddings.size()) return false;
		for(int i = index;i<wordEmbeddings.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!recommandedTokenRelationship.getTokenRelationship().getFromToken().getToken().equals(toToken))continue;
			if(edgeName.equals(WordEmbeddingTypes.secondVerb)) return true;
		}
		return false;
	}
	
	
	private boolean includeOnTokenTokenRight(List<RecommandedTokenRelationship> wordEmbeddings, int index){
		if(index<=wordEmbeddings.size()) return false;
		for(int i = index;i<wordEmbeddings.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!edgeName.equals(WordEmbeddingTypes.defaultEdge))continue;
			if(recommandedTokenRelationship.getIsVerified()) return true;
		}
		return false;
	}

	
	private boolean includeOnTokenTokenLeft(List<RecommandedTokenRelationship> wordEmbeddings, int index){
		if(index>0) return false;
		for(int i = index;i>=0;i--){
			RecommandedTokenRelationship recommandedTokenRelationship = wordEmbeddings.get(i);
			String edgeName = getEdgeName(recommandedTokenRelationship);
			if(!edgeName.equals(WordEmbeddingTypes.defaultEdge))continue;
			if(recommandedTokenRelationship.getIsVerified()) return true;
		}
		return false;
	}
	
}
