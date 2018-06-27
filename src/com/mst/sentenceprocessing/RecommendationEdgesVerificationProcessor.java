package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;



public class RecommendationEdgesVerificationProcessor {

	public List<RecommendedTokenRelationship> process(SentenceDiscovery sentenceDiscovery, List<RecommendedTokenRelationship> existing){
		Map<String, RecommendedTokenRelationship> existingMap =  convertExistingToMap(existing); 
		for(Entry<Integer, Integer> entry: sentenceDiscovery.getNounPhraseIndexes().entrySet()){
			List<RecommendedTokenRelationship> matched = setVerifiedAndFindExistingMatches(entry.getKey(), entry.getValue(), sentenceDiscovery.getWordEmbeddings(),existingMap);
			if(matched!=null){
				
				updateIndexesOnExisting(matched, sentenceDiscovery);
				sentenceDiscovery.getWordEmbeddings().addAll(matched); // the list matched is added into the list returned by getWordEmbeddings
		
			}
		}
		setVerifiedOnEdgeValue(sentenceDiscovery.getWordEmbeddings());
		setTokenRankings(sentenceDiscovery.getWordEmbeddings(), sentenceDiscovery.getModifiedWordList());
		return sentenceDiscovery.getWordEmbeddings();
	}
	
	public static void updateIndexesOnExisting(List<RecommendedTokenRelationship> matched, SentenceDiscovery discovery){
		Map<String, Integer> tokensByIndex = new HashMap<>();
		
		for(WordToken token: discovery.getModifiedWordList()){
			tokensByIndex.put(token.getToken(), token.getPosition());
		}
		
		for(RecommendedTokenRelationship r: matched){
			TokenRelationship relationship = r.getTokenRelationship();
			if(tokensByIndex.containsKey(relationship.getFromToken().getToken())){
				relationship.getFromToken().setPosition(tokensByIndex.get(relationship.getFromToken().getToken()));
			}
			
			if(tokensByIndex.containsKey(relationship.getToToken().getToken())){
				relationship.getToToken().setPosition(tokensByIndex.get(relationship.getToToken().getToken()));
			}
		}
	}
	
	/**
	 * Method setTokenRankings is used to check various conditions and assign token ranking when the conditions meet. 
	 * Method get's the edgeName from the embeddedWords and compares it with WordEmbeddingTypes. When the required condition is matched
	 * the statements inside the if condition assigns the tokenRanking as required.
	 * @param embeddedWords
	 * @param modifiedWordList
	 */

	private void setTokenRankings(List<RecommendedTokenRelationship> embeddedWords, List<WordToken> modifiedWordList){
		//WordToken wordtoken = new WordToken();
		for(int i =0; i<embeddedWords.size();i++) {
			RecommendedTokenRelationship recommendedTokenRelationship = embeddedWords.get(i);
			TokenRelationship relationship = recommendedTokenRelationship.getTokenRelationship();
			String edgeName = relationship.getEdgeName();
			WordToken wordToken = relationship.getFromToken();
			int tokenRanking;
			//checking the condition for a single word sentence
			if(edgeName==null) {
			    /*
				//if(embeddedWords.size()==1) {
				relationship.setEdgeName(WordEmbeddingTypes.tokenToken);
				//wordToken word=modifiedWord
				relationship.setToToken(wordToken);
				relationship.setFromToken(wordToken);
				//}
				*/
			    tokenRanking = 0;
			    wordToken.setTokenRanking(tokenRanking);
			}
			if(edgeName.equals(WordEmbeddingTypes.prepMinus)) {
				tokenRanking = relationship.getFromToken().getTokenRanking()+2;
				wordToken.setTokenRanking(tokenRanking);
			}
			if(edgeName.equals(WordEmbeddingTypes.tokenToken)) {
				RecommendedTokenRelationship nextTokenToken = findNextTokenToken(i+1, embeddedWords);
				if(nextTokenToken==null) {
					relationship.getToToken().setTokenRanking(relationship.getToToken().getTokenRanking()+2);
						continue;
			}
			else {
				nextTokenToken.getTokenRelationship().getToToken().setTokenRanking(nextTokenToken.getTokenRelationship().getToToken().getTokenRanking()+2);
				
				}
			}
			if(edgeName.equals(WordEmbeddingTypes.commaMinus)) {
				tokenRanking = relationship.getFromToken().getTokenRanking()+1;
				relationship.getToToken().setTokenRanking(tokenRanking);
				wordToken.setTokenRanking(tokenRanking);
			}

		}
	}

	private Map<String, RecommendedTokenRelationship> convertExistingToMap(List<RecommendedTokenRelationship> existing){
		Map<String, RecommendedTokenRelationship> result = new HashMap<>();
		for(RecommendedTokenRelationship recommandedTokenRelationship: existing){
			if(result.containsKey(recommandedTokenRelationship.getKey()))
				continue;
			result.put(recommandedTokenRelationship.getKey(), recommandedTokenRelationship);
		}
		return result;
	}
	
	/**
	 * setVerifiedAndFindExistingMatches creates begin and end index, checks the existing matches on it and avoids duplication.
	 * @param beginIndex
	 * @param endIndex
	 * @param embeddedwords
	 * @param existingMap
	 * @return result (existing Matches)
	 */
	private List<RecommendedTokenRelationship> setVerifiedAndFindExistingMatches(int beginIndex,int endIndex, List<RecommendedTokenRelationship> embeddedwords, Map<String, RecommendedTokenRelationship> existingMap){
		if(beginIndex>endIndex)
			return null;
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		List<Integer> consecutiveTokensToken = new ArrayList<>();
		for(int i = beginIndex;i<=endIndex;i++){
			RecommendedTokenRelationship current = embeddedwords.get(i);
			if(current.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.tokenToken)){
				consecutiveTokensToken.add(i);
				if(i==endIndex) {
					List<RecommendedTokenRelationship> existingMatches =  findMatchesFromExistingOnConsequtives(consecutiveTokensToken,embeddedwords,existingMap);		
					if(existingMatches!=null)
						result.addAll(existingMatches);
				}
				continue;
			}
			current.setIsVerified(true);
			List<RecommendedTokenRelationship> existingMatches =  findMatchesFromExistingOnConsequtives(consecutiveTokensToken,embeddedwords,existingMap);		
			if(existingMatches!=null)
				result.addAll(existingMatches);
			consecutiveTokensToken.clear();
		}
		return result;
	}

	
	/*
	 * verifying the wordEmbeddings based on Preposition Phrases.
	 *  
	 */
	private void setVerifiedOnEdgeValue(List<RecommendedTokenRelationship> embeddedwords){
	
		for(int i =0; i<embeddedwords.size();i++) {
			RecommendedTokenRelationship recommandedTokenRelationship = embeddedwords.get(i);
			TokenRelationship relationship = recommandedTokenRelationship.getTokenRelationship();
			String edgeName = relationship.getEdgeName();
			
			//do it here.. 
			/*
			 * need to confirm the "if statement" here
			 * 
			 */
			if(edgeName.equals(EdgeNames.existence))
				recommandedTokenRelationship.setIsVerified(true);
	
			if(edgeName.equals(WordEmbeddingTypes.prepPlus) || edgeName.equals(WordEmbeddingTypes.prepMinus)) {
				recommandedTokenRelationship.setIsVerified(true);
				continue;
			}
			
			if(edgeName.equals(WordEmbeddingTypes.verbPlus) || edgeName.equals(WordEmbeddingTypes.verbMinus)){
				if(i+1 >=embeddedwords.size()){
					recommandedTokenRelationship.setIsVerified(true);
					return; 
				}
				/*
				 * Only applies to a single instance of token-token edge
				 * 
				 */
				RecommendedTokenRelationship nextTokenToken = findNextTokenToken(i+1, embeddedwords);
				if(nextTokenToken==null)
					continue;
				if(!relationship.getFromToken().getToken().equals(nextTokenToken.getTokenRelationship().getToToken().getToken()))
					recommandedTokenRelationship.setIsVerified(true);
			}		
		}
	}
		
	private RecommendedTokenRelationship findNextTokenToken(int index,List<RecommendedTokenRelationship> embeddedwords){
		for(int i = index; i<embeddedwords.size();i++){
			RecommendedTokenRelationship recommandedTokenRelationship = embeddedwords.get(i);
			if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.tokenToken))return recommandedTokenRelationship;
		}
		return null;
	}
	
	private List<RecommendedTokenRelationship> findMatchesFromExistingOnConsequtives(List<Integer> consecutiveTokensToken,List<RecommendedTokenRelationship> embeddedwords, Map<String,RecommendedTokenRelationship> existingMap){
		if(consecutiveTokensToken.size()==0)
			return null;
		int startIndex = consecutiveTokensToken.get(0);
		int endIndex = consecutiveTokensToken.get(consecutiveTokensToken.size()-1);
		
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		if(consecutiveTokensToken.size()==1){
			TokenRelationship relationship = embeddedwords.get(startIndex).getTokenRelationship();
			RecommendedTokenRelationship matched = findOffofSingleMatch(relationship,existingMap);
			if(matched!=null)
				result.add(matched);
			return result;
		}
	
		for(int i = startIndex;i<endIndex;i++){
			String from = embeddedwords.get(i).getTokenRelationship().getFromToken().getToken();
			for(int j = i+1;j<=endIndex;j++){
				String to = embeddedwords.get(j).getTokenRelationship().getToToken().getToken();
				String key = from+to;
				
				/*
				 * if this part has a low score 
				 * 
				 */
				if(existingMap.containsKey(key)){
					RecommendedTokenRelationship matchedRecommandedTokenRelationship = existingMap.get(key);
					matchedRecommandedTokenRelationship.setIsVerified(true);
					result.add(matchedRecommandedTokenRelationship);
				}
			}
		}
		
		TokenRelationship relationship = embeddedwords.get(endIndex).getTokenRelationship();
		RecommendedTokenRelationship matched = findOffofSingleMatch(relationship,existingMap);
		if(matched!=null)
			result.add(matched);
		
		return result; 
	}
	
	private RecommendedTokenRelationship findOffofSingleMatch(TokenRelationship tokenRelationship, Map<String,RecommendedTokenRelationship> existingMap ){
		String key = tokenRelationship.getFromTokenToTokenString();
		if(!existingMap.containsKey(key))
			return null;
		RecommendedTokenRelationship matchedRecommandedTokenRelationship = existingMap.get(key);
		matchedRecommandedTokenRelationship.setIsVerified(true);
		return matchedRecommandedTokenRelationship;
	}
	
	/*
	private void setScoreOnTokenValue(List<RecommendedTokenRelationship> embeddedwords) {
        for (int i = 0; i < embeddedwords.size(); i++) {
            RecommendedTokenRelationship recommandedTokenRelationship = embeddedwords.get(i);
            TokenRelationship relationship = recommandedTokenRelationship.getTokenRelationship();
            String edgeName = relationship.getEdgeName();
            if (edgeName.equals(WordEmbeddingTypes.prepMinus)) {
                relationship.setTokenValue(2);
            }
            if (edgeName.equals(WordEmbeddingTypes.tokenToken)) {
                if (i + 1 >= embeddedwords.size()) {
                    relationship.setTokenValue(2);
                } else {
                    relationship.setTokenValue(1);
                }
            }
        }
    }
    */
}
