package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class RecommendationEdgesVerificationProcesser {

	public List<RecommandedTokenRelationship> process(SentenceDiscovery sentenceDiscovery, List<RecommandedTokenRelationship> existing){
		Map<String, RecommandedTokenRelationship> existingMap =  convertExistingToMap(existing); 
		for(Entry<Integer, Integer> entry: sentenceDiscovery.getNounPhraseIndexes().entrySet()){
			List<RecommandedTokenRelationship> matched = setVerifiedAndFindExistingMatches(entry.getKey(), entry.getValue(), sentenceDiscovery.getWordEmbeddings(),existingMap);
			if(matched!=null) 
				sentenceDiscovery.getWordEmbeddings().addAll(matched);
		}
		setverifiedOnPrepPhrases(sentenceDiscovery.getWordEmbeddings());
		return sentenceDiscovery.getWordEmbeddings();
	}

	private void setverifiedOnPrepPhrases(List<RecommandedTokenRelationship> embeddedwords){
	
		for(int i =0; i<embeddedwords.size();i++) {
			RecommandedTokenRelationship recommandedTokenRelationship = embeddedwords.get(i);
			TokenRelationship relationship = recommandedTokenRelationship.getTokenRelationship();
			String edgeName = relationship.getEdgeName();
			if(edgeName.equals(WordEmbeddingTypes.firstPrep) || edgeName.equals(WordEmbeddingTypes.secondPrep)) {
				recommandedTokenRelationship.setIsVerified(true);
				continue;
			}
			
			if(edgeName.equals(WordEmbeddingTypes.firstVerb) || edgeName.equals(WordEmbeddingTypes.secondVerb)){
				if(i+1 >=embeddedwords.size()){
					recommandedTokenRelationship.setIsVerified(true);
					return; 
				}
				RecommandedTokenRelationship nextTokenToken = findNextTokenToken(i+1, embeddedwords);
				if(nextTokenToken==null) continue; 
				if(!relationship.getFromToken().getToken().equals(nextTokenToken.getTokenRelationship().getToToken()))
					recommandedTokenRelationship.setIsVerified(true);
			}		
		}
	}
		
	private RecommandedTokenRelationship findNextTokenToken(int index,List<RecommandedTokenRelationship> embeddedwords){
		for(int i = index; i<embeddedwords.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship = embeddedwords.get(i);
			if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))return recommandedTokenRelationship;
		}
		return null;
	}
	
	private List<RecommandedTokenRelationship> findMatchesFromExistingOnConsequtives(List<Integer> consecutiveTokensToken,List<RecommandedTokenRelationship> embeddedwords, Map<String,RecommandedTokenRelationship> existingMap){
		if(consecutiveTokensToken.size()==0) return null;
		int startIndex = consecutiveTokensToken.get(0);
		int endIndex = consecutiveTokensToken.get(consecutiveTokensToken.size()-1);
		
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		if(consecutiveTokensToken.size()==1){
			TokenRelationship relationship = embeddedwords.get(startIndex).getTokenRelationship();
			RecommandedTokenRelationship matched = findOffofSingleMatch(relationship,existingMap);
			if(matched!=null)
				result.add(matched);
			return result;
		}
	
		for(int i = startIndex;i<endIndex;i++){
			String from = embeddedwords.get(i).getTokenRelationship().getFromToken().getToken();
			for(int j = i+1;j<=endIndex;j++){
				String to = embeddedwords.get(j).getTokenRelationship().getToToken().getToken();
				String key = from+to;
				if(existingMap.containsKey(key)){
					RecommandedTokenRelationship matchedRecommandedTokenRelationship = existingMap.get(key);
					matchedRecommandedTokenRelationship.setIsVerified(true);
					result.add(matchedRecommandedTokenRelationship);
				}
			}
		}
		
		TokenRelationship relationship = embeddedwords.get(endIndex).getTokenRelationship();
		RecommandedTokenRelationship matched = findOffofSingleMatch(relationship,existingMap);
		if(matched!=null)
			result.add(matched);
		
		return result; 
	}
	
	private RecommandedTokenRelationship findOffofSingleMatch(TokenRelationship tokenRelationship, Map<String,RecommandedTokenRelationship> existingMap ){
		String key = tokenRelationship.getFromTokenToTokenString();
		if(!existingMap.containsKey(key))return null;
		
		RecommandedTokenRelationship matchedRecommandedTokenRelationship = existingMap.get(key);
		matchedRecommandedTokenRelationship.setIsVerified(true);
		return matchedRecommandedTokenRelationship;
	}
	
	
	private Map<String, RecommandedTokenRelationship> convertExistingToMap(List<RecommandedTokenRelationship> existing){
		Map<String, RecommandedTokenRelationship> result = new HashMap<>();
		for(RecommandedTokenRelationship recommandedTokenRelationship: existing){
			if(result.containsKey(recommandedTokenRelationship.getKey())) continue;
			result.put(recommandedTokenRelationship.getKey(), recommandedTokenRelationship);
		}
		return result;
	}
	
	private List<RecommandedTokenRelationship> setVerifiedAndFindExistingMatches(int beginIndex,int endIndex, List<RecommandedTokenRelationship> embeddedwords, Map<String, RecommandedTokenRelationship> existingMap){
		if(beginIndex>endIndex) return null; 
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		List<Integer> consecutiveTokensToken = new ArrayList<>();
		for(int i = beginIndex;i<=endIndex;i++){
			RecommandedTokenRelationship current = embeddedwords.get(i);
			if(current.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge)){
				consecutiveTokensToken.add(i);
				if(i==endIndex) {
					List<RecommandedTokenRelationship> existingMatches =  findMatchesFromExistingOnConsequtives(consecutiveTokensToken,embeddedwords,existingMap);		
					if(existingMatches!=null)
						result.addAll(existingMatches);
				}
				continue;
			}
			current.setIsVerified(true);
			List<RecommandedTokenRelationship> existingMatches =  findMatchesFromExistingOnConsequtives(consecutiveTokensToken,embeddedwords,existingMap);		
			if(existingMatches!=null)
				result.addAll(existingMatches);
			consecutiveTokensToken.clear();
		}
		return result;
	}	
}
