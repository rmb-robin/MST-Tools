package com.mst.sentenceprocessing;

import java.util.List;

import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class TokenRankImpl {
	
	/*
	 * Tasks:

a) Add tokenValue to modifiedWordList tokens. 
b) Add logic to verification processor:

1) If wordEmbedding == prepMinus, increment fromToken tokenValue by 2
2) If wordEmbedding == tokentoken, increment toToken tokenValue by 1
3) If wordEmbedding == tokentoken and tokentoken == highest index tokentoken, increment toToken tokenValue by 2
4) If wordEmbedding == commaMinus, increment toToken tokenValue by 1 **
	 */
	
	/**
	 * Method setTokenRankings is used to check various conditions and assign token ranking when the conditions meet. 
	 * Method get's the edgeName from the embeddedWords and compares it with WordEmbeddingTypes. When the required condition is matched
	 * the statements inside the if condition assigns the tokenRanking as required.
	 * @param embeddedWords
	 * 
	 */
		
	void setTokenRankings(SentenceDiscovery sentenceDiscovery){
		//WordToken wordtoken = new WordToken();
		List<RecommendedTokenRelationship> embeddedWords = sentenceDiscovery.getWordEmbeddings();
		for(int i =0; i<embeddedWords.size();i++) {
			RecommendedTokenRelationship recommendedTokenRelationship = embeddedWords.get(i);
			TokenRelationship relationship = recommendedTokenRelationship.getTokenRelationship();
			String edgeName = relationship.getEdgeName(); //Is this returning measurement, existence, etc;
			WordToken toToken = relationship.getToToken();
			WordToken fromToken = relationship.getFromToken();
	
			int tokenRanking=0;
			
			
			if(edgeName.equals(WordEmbeddingTypes.prepMinus)) {
				tokenRanking =fromToken.getTokenRanking()+2;
				fromToken.setTokenRanking(tokenRanking);
				//updateModifiedWordList(sentenceDiscovery, fromToken.getToken(), tokenRanking);
				updateModifiedWordList(sentenceDiscovery, fromToken.getToken(), "prepMinus");
				continue;
			}
			if(edgeName.equals(WordEmbeddingTypes.tokenToken)) {
				if (i+1 < embeddedWords.size()) {
					RecommendedTokenRelationship nextRecommendedTokenRelationship = embeddedWords.get(i+1);
					TokenRelationship nextRelationship = nextRecommendedTokenRelationship.getTokenRelationship();
					String nextEdgeName = nextRelationship.getEdgeName();
					if(nextEdgeName.equals(WordEmbeddingTypes.tokenToken)) {
						//tokenRanking =toToken.getTokenRanking()+2;
						//toToken.setTokenRanking(tokenRanking);
						updateModifiedWordList(sentenceDiscovery, toToken.getToken(), "consecutiveTokenToken");
						continue;
					}
				}
				//tokenRanking = toToken.getTokenRanking()+1;
				//toToken.setTokenRanking(tokenRanking);
				updateModifiedWordList(sentenceDiscovery, toToken.getToken(), "tokenToken");
				
			}
				
			if(edgeName.equals(WordEmbeddingTypes.commaMinus)) {
				tokenRanking = relationship.getFromToken().getTokenRanking()+1;
				relationship.getToToken().setTokenRanking(tokenRanking);
				toToken.setTokenRanking(tokenRanking);
			}

		}
	}
	
	private void updateModifiedWordList(SentenceDiscovery sentenceDiscovery, String word, String edgeName) {
		
		for (WordToken wordToken : sentenceDiscovery.getModifiedWordList()) {
			if(wordToken.getToken().equals(word)) {
				switch (edgeName) {
					case "prepMinus":
						wordToken.setTokenRanking(+2);
						break;
					
					case "tokenToken":
						wordToken.setTokenRanking(+1);
						break;
						
					case "consecutiveTokenToken":
						wordToken.setTokenRanking(+2);
						break;


					
				}
				//wordToken.setTokenRanking(tokenRanking);
			}
		}
	}
}


